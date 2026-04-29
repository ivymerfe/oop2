package labs.network.server;

import labs.network.protocol.*;
import labs.network.protocol.c2s.ChatMessageC2S;
import labs.network.protocol.c2s.ConnectC2S;
import labs.network.protocol.c2s.ListUsersC2S;
import labs.network.protocol.c2s.LogoutC2S;
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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static final int SAVE_TIMEOUT = 2;

    private final Path savePath;
    private final int port;
    private final int readTimeoutMillis;
    private final ExecutorService workers;
    private ChatState chat;
    private Thread saveThread;
    private final Map<String, UserState> sessions = new HashMap<>();

    public Server(Path savePath, int port, int readTimeoutMillis, boolean loggingEnabled) {
        this.savePath = savePath;
        this.port = port;
        this.readTimeoutMillis = readTimeoutMillis;
        this.workers = Executors.newCachedThreadPool();
        if (!loggingEnabled) {
            Configurator.setRootLevel(Level.OFF);
        }
        loadChat();
    }

    public void loadChat() {
        if (Files.exists(savePath)) {
            try (InputStream stream = Files.newInputStream(savePath)) {
                ObjectInputStream in = new ObjectInputStream(stream);
                chat = (ChatState) in.readObject();
                return;
            } catch (IOException e) {
                LOGGER.error(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        chat = new ChatState();
    }

    public void saveChat() {
        try (OutputStream stream = Files.newOutputStream(savePath)) {
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(chat);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public void start() throws IOException {
        this.saveThread = Thread.ofVirtual().start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                saveChat();
                try {
                    TimeUnit.SECONDS.sleep(SAVE_TIMEOUT);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("Server started on port {}", port);
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
            LOGGER.info("Client connected: {}", socket.getRemoteSocketAddress());
            while (true) {
                boolean ok = false;
                try {
                    processIncoming(context, context.receive());
                    ok = true;
                } catch (SocketTimeoutException e) {
                    LOGGER.info("Client timed out: {}", socket.getRemoteSocketAddress());
                } catch (EOFException e) {
                    LOGGER.info("Client closed connection: {}", socket.getRemoteSocketAddress());
                } catch (IOException e) {
                    LOGGER.info("Connection error with {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
                }
                if (!ok) {
                    disconnect(context, true);
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.info("Connection error with {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
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
        context.sendError("Unsupported command");
    }

    private void handleLogin(ConnectionContext connection, ConnectC2S command) throws IOException {
        String name = command.getName().trim();
        String clientType = command.getClientType().trim();
        String password = command.getPassword();
        if (name.isEmpty()) {
            connection.sendError("Name cannot be empty");
            return;
        }
        if (clientType.isEmpty()) {
            connection.sendError("Client type cannot be empty");
            return;
        }
        UserState user = chat.findUser(name);
        if (user == null) {
            user = chat.newUser(name, password);
        } else {
            if (!user.testPassword(password)) {
                connection.sendError("Incorrect password");
                return;
            }
        }
        user.clientType = clientType;
        user.connection = connection;
        String sessionId = UUID.randomUUID().toString();
        connection.sessionId = sessionId;
        sessions.put(sessionId, user);
        connection.send(new LoginResposeS2C(sessionId));
        LOGGER.info("User logged in: {} ({})", name, sessionId);
        broadcast(new UserLoginEventS2C(name, clientType), user);
    }

    private void handleListUsers(ConnectionContext context, ListUsersC2S command) throws IOException {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            context.sendError("Invalid session");
            return;
        }
        List<UserInfo> infos = chat.getUsers().stream().map(u -> new UserInfo(u.name, u.clientType)).toList();
        context.send(new ListUsersS2C(infos));
    }

    private void handleChatMessage(ConnectionContext context, ChatMessageC2S command) throws IOException {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            context.sendError("Invalid session");
            return;
        }
        String text = command.getMessage().trim();
        if (text.isEmpty()) {
            context.sendError("Message cannot be empty");
            return;
        }
        chat.addMessage(new ChatMessage(user.name, text));
        broadcast(new EventMessageS2C(text, user.name), user);
        context.send(new MessageResponseS2C());
        LOGGER.info("Message from {}: {}", user.name, text);
    }

    private void handleLogout(ConnectionContext context, LogoutC2S command) throws IOException {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            context.sendError("Invalid session");
            return;
        }
        context.send(new LogoutResponseS2C());
        disconnect(context, true);
    }

    private void broadcast(Message message, UserState excludeUser) {
        for (UserState user : sessions.values()) {
            if (user != excludeUser) {
                ConnectionContext connection = user.connection;
                if (connection == null) {
                    continue;
                }
                try {
                    connection.send(message);
                } catch (IOException e) {
                    disconnect(connection, true);
                }
            }
        }
    }

    private void disconnect(ConnectionContext context, boolean announce) {
        UserState user = sessions.get(context.sessionId);
        if (user == null) {
            return;
        }
        sessions.remove(context.sessionId);

        LOGGER.info("User disconnected: {}", user.name);
        if (announce) {
            broadcast(new UserLogoutEventS2C(user.name), user);
        }
    }
}
