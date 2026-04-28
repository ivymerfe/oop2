package labs.network.server;

import labs.network.protocol.*;
import labs.network.protocol.c2s.ChatMessageC2S;
import labs.network.protocol.c2s.ConnectC2S;
import labs.network.protocol.c2s.ListUsersC2S;
import labs.network.protocol.c2s.LogoutC2S;
import labs.network.protocol.s2c.ErrorS2C;
import labs.network.protocol.s2c.EventMessageS2C;
import labs.network.protocol.s2c.ListUsersS2C;
import labs.network.protocol.s2c.LoginResposeS2C;
import labs.network.protocol.s2c.LogoutResponseS2C;
import labs.network.protocol.s2c.MessageResponseS2C;
import labs.network.protocol.s2c.UserLoginEventS2C;
import labs.network.protocol.s2c.UserLogoutEventS2C;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);

    private final int port;
    private final int readTimeoutMillis;
    private final ExecutorService workers;
    private final Object stateLock = new Object();
    private final Map<String, UserState> usersByName = new LinkedHashMap<>();
    private final Map<String, UserState> usersBySession = new LinkedHashMap<>();

    public Server(int port, int readTimeoutMillis, boolean loggingEnabled) {
        this.port = port;
        this.readTimeoutMillis = readTimeoutMillis;
        this.workers = Executors.newCachedThreadPool();
        if (!loggingEnabled) {
            Configurator.setRootLevel(Level.OFF);
        }
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOG.info("Server started on port {}", port);
            while (true) {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(readTimeoutMillis);
                socket.setTcpNoDelay(true);
                workers.execute(() -> handleConnection(socket));
            }
        }
    }

    private void handleConnection(Socket socket) {
        try (ConnectionContext context = new ConnectionContext(socket)) {
            LOG.info("Client connected: {}", socket.getRemoteSocketAddress());
            while (true) {
                boolean ok = false;
                try {
                    processIncoming(context, context.receive());
                    ok = true;
                } catch (SocketTimeoutException e) {
                    LOG.info("Client timed out: {}", socket.getRemoteSocketAddress());
                } catch (EOFException e) {
                    LOG.info("Client closed connection: {}", socket.getRemoteSocketAddress());
                } catch (IOException e) {
                    LOG.info("Connection error with {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
                }
                if (!ok) {
                    disconnect(context, true);
                    break;
                }
            }
        } catch (IOException e) {
            LOG.info("Connection error with {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
        }
    }

    private void processIncoming(ConnectionContext context, Message message) throws IOException {
        if (message instanceof ConnectC2S connect) {
            handleLogin(context, connect);
            return;
        }
        if (message instanceof ListUsersC2S listUsers) {
            handleListUsers(context, listUsers);
            return;
        }
        if (message instanceof ChatMessageC2S chatMessage) {
            handleChatMessage(context, chatMessage);
            return;
        }
        if (message instanceof LogoutC2S logout) {
            handleLogout(context, logout);
            return;
        }
        send(context, new ErrorS2C("Unsupported command"));
    }

    private void handleLogin(ConnectionContext context, ConnectC2S command) throws IOException {
        String name = command.getName();
        String clientType = command.getClientType();
        if (name.isEmpty()) {
            send(context, new ErrorS2C("Name cannot be empty"));
            return;
        }
        if (clientType.isEmpty()) {
            send(context, new ErrorS2C("Client type cannot be empty"));
            return;
        }

        UserState user;
        synchronized (stateLock) {
            if (context.user != null) {
                send(context, new ErrorS2C("Already logged in"));
                return;
            }
            user = usersByName.get(name);
            if (user != null && user.online) {
                send(context, new ErrorS2C("Nickname is already in use"));
                return;
            }
            if (user == null) {
                user = new UserState(name, clientType, UUID.randomUUID().toString());
                usersByName.put(name, user);
                usersBySession.put(user.sessionId, user);
            } else {
                user.clientType = clientType;
            }
            user.online = true;
            user.connection = context;
            context.user = user;
        }

        send(context, new LoginResposeS2C(user.sessionId));
        LOG.info("User logged in: {} ({})", user.name, user.sessionId);
        broadcastEvent(new UserLoginEventS2C(user.name, user.clientType), user.name);
    }

    private void handleListUsers(ConnectionContext context, ListUsersC2S command) throws IOException {
        Optional<UserState> user = authorize(context, command.getSession());
        if (user.isEmpty()) {
            send(context, new ErrorS2C("Invalid session"));
            return;
        }

        List<UserInfo> users;
        synchronized (stateLock) {
            users = usersByName.values().stream()
                    .filter(u -> u.online)
                    .map(u -> new UserInfo(u.name, u.clientType))
                    .toList();
        }
        send(context, new ListUsersS2C(users));
    }

    private void handleChatMessage(ConnectionContext context, ChatMessageC2S command) throws IOException {
        Optional<UserState> user = authorize(context, command.getSession());
        if (user.isEmpty()) {
            send(context, new ErrorS2C("Invalid session"));
            return;
        }
        String text = command.getMessage();
        if (text.isEmpty()) {
            send(context, new ErrorS2C("Message cannot be empty"));
            return;
        }
        UserState sender = user.get();
        broadcastEvent(new EventMessageS2C(text, sender.name), null);
        send(context, new MessageResponseS2C());
        LOG.info("Message from {}: {}", sender.name, text);
    }

    private void handleLogout(ConnectionContext context, LogoutC2S command) throws IOException {
        Optional<UserState> user = authorize(context, command.getSession());
        if (user.isEmpty()) {
            send(context, new ErrorS2C("Invalid session"));
            return;
        }
        send(context, new LogoutResponseS2C());
        disconnect(context, true);
    }

    private Optional<UserState> authorize(ConnectionContext context, String session) {
        synchronized (stateLock) {
            if (context.user == null) {
                return Optional.empty();
            }
            UserState bySession = usersBySession.get(session);
            if (bySession == null) {
                return Optional.empty();
            }
            if (!Objects.equals(bySession, context.user) || !bySession.online) {
                return Optional.empty();
            }
            return Optional.of(bySession);
        }
    }

    private void broadcastEvent(Message event, String excludeUser) {
        List<UserState> recipients;
        synchronized (stateLock) {
            recipients = usersByName.values().stream()
                    .filter(user -> user.online)
                    .filter(user -> !Objects.equals(user.name, excludeUser))
                    .toList();
        }
        for (UserState recipient : recipients) {
            ConnectionContext connection = recipient.connection;
            if (connection == null) {
                continue;
            }
            try {
                send(connection, event);
            } catch (IOException e) {
                disconnect(connection, true);
            }
        }
    }

    private void send(ConnectionContext context, Message message) throws IOException {
        Serializer serializer = context.serializer;
        if (serializer == null) {
            serializer = new XMLSerializer();
            context.serializer = serializer;
        }
        byte[] payload = serializer.serialize(message);
        synchronized (context.out) {
            Payload.write(context.out, payload);
            context.out.flush();
        }
    }

    private void disconnect(ConnectionContext context, boolean announce) {
        UserState user;
        synchronized (stateLock) {
            user = context.user;
            if (user == null || !user.online || user.connection != context) {
                return;
            }
            user.online = false;
            user.connection = null;
        }
        LOG.info("User disconnected: {}", user.name);
        if (announce) {
            broadcastEvent(new UserLogoutEventS2C(user.name), user.name);
        }
    }
}
