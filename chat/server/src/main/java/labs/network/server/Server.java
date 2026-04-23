package labs.network.server;

import labs.network.protocol.Message;
import labs.network.protocol.ObjectSerializer;
import labs.network.protocol.Serializer;
import labs.network.protocol.XMLSerializer;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final Logger LOG = LogManager.getLogger(Server.class);
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 120000;
    private static final int MAX_FRAME_SIZE = 1024 * 1024;

    private final int port;
    private final int readTimeoutMillis;
    private final ExecutorService workers;
    private final Object stateLock = new Object();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<ChatEvent> events = new ArrayList<>();
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
        ConnectionContext context = null;
        try (socket;
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            context = new ConnectionContext(socket, in, out);
            LOG.info("Client connected: {}", socket.getRemoteSocketAddress());
            while (true) {
                byte[] frame;
                try {
                    frame = readFrame(in);
                } catch (SocketTimeoutException e) {
                    LOG.info("Client timed out: {}", socket.getRemoteSocketAddress());
                    break;
                }
                IncomingMessage incoming = decodeMessage(context, frame);
                processIncoming(context, incoming.message());
            }
        } catch (EOFException e) {
            LOG.info("Client closed connection: {}", socket.getRemoteSocketAddress());
        } catch (IOException e) {
            LOG.info("Connection error with {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Unexpected error for {}", socket.getRemoteSocketAddress(), e);
        } finally {
            if (context != null) {
                disconnect(context, true);
            }
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
        String name = normalize(command.getName());
        String clientType = normalize(command.getClientType());
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
        sendPendingEvents(context, user);
        LOG.info("User logged in: {} ({})", user.name, user.sessionId);
        broadcastEvent(new UserLoginEventS2C(user.name), user.name);
    }

    private void handleListUsers(ConnectionContext context, ListUsersC2S command) throws IOException {
        Optional<UserState> user = authorize(context, command.getSession());
        if (user.isEmpty()) {
            send(context, new ErrorS2C("Invalid session"));
            return;
        }

        List<ListUsersS2C.UserInfo> users;
        synchronized (stateLock) {
            users = usersByName.values().stream()
                    .filter(u -> u.online)
                    .map(u -> new ListUsersS2C.UserInfo(u.name, u.clientType))
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
        String text = normalize(command.getMessage());
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

    private void sendPendingEvents(ConnectionContext context, UserState user) throws IOException {
        List<ChatEvent> pending;
        synchronized (stateLock) {
            pending = events.stream().filter(event -> event.sequence > user.lastDeliveredSequence).toList();
        }
        for (ChatEvent event : pending) {
            send(context, event.message);
            synchronized (stateLock) {
                if (user.lastDeliveredSequence < event.sequence) {
                    user.lastDeliveredSequence = event.sequence;
                }
            }
        }
    }

    private void broadcastEvent(Message event, String excludeUser) {
        long seq;
        List<UserState> recipients;
        synchronized (stateLock) {
            seq = sequence.incrementAndGet();
            events.add(new ChatEvent(seq, event));
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
                synchronized (stateLock) {
                    if (recipient.lastDeliveredSequence < seq) {
                        recipient.lastDeliveredSequence = seq;
                    }
                }
            } catch (IOException e) {
                disconnect(connection, true);
            }
        }
    }

    private IncomingMessage decodeMessage(ConnectionContext context, byte[] payload) {
        if (context.serializer != null) {
            return new IncomingMessage(context.serializer.deserialize(payload), context.serializer);
        }
        List<Serializer> candidates = List.of(new XMLSerializer(), new ObjectSerializer());
        for (Serializer candidate : candidates) {
            try {
                Message message = candidate.deserialize(payload);
                if (message instanceof ConnectC2S) {
                    context.serializer = candidate;
                    return new IncomingMessage(message, candidate);
                }
            } catch (RuntimeException ignored) {
            }
        }
        throw new IllegalArgumentException("Unable to detect serializer from first client message");
    }

    private void send(ConnectionContext context, Message message) throws IOException {
        Serializer serializer = context.serializer;
        if (serializer == null) {
            serializer = new XMLSerializer();
            context.serializer = serializer;
        }
        byte[] payload = serializer.serialize(message);
        synchronized (context.out) {
            writeFrame(context.out, payload);
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

    private byte[] readFrame(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length <= 0 || length > MAX_FRAME_SIZE) {
            throw new IOException("Invalid frame length: " + length);
        }
        byte[] payload = new byte[length];
        in.readFully(payload);
        return payload;
    }

    private void writeFrame(DataOutputStream out, byte[] payload) throws IOException {
        out.writeInt(payload.length);
        out.write(payload);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip();
    }

    public static void main(String[] args) {
        Properties properties = loadProperties();
        int port = parseIntProperty(properties, "port", DEFAULT_PORT);
        int readTimeout = parseIntProperty(properties, "readTimeoutMillis", DEFAULT_READ_TIMEOUT_MS);
        boolean loggingEnabled = Boolean.parseBoolean(properties.getProperty("loggingEnabled", "true"));

        Server server = new Server(port, readTimeout, loggingEnabled);
        try {
            server.start();
        } catch (IOException e) {
            LOG.error("Server failed", e);
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path path = Path.of("server", "src", "main", "resources", "server.properties");
        if (Files.exists(path)) {
            try {
                properties.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
                return properties;
            } catch (IOException ignored) {
            }
        }
        try (var stream = Server.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }

    private static int parseIntProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private record IncomingMessage(Message message, Serializer serializer) {
    }

    private record ChatEvent(long sequence, Message message) {
    }

    private static final class UserState {
        private final String name;
        private final String sessionId;
        private String clientType;
        private boolean online;
        private long lastDeliveredSequence;
        private ConnectionContext connection;

        private UserState(String name, String clientType, String sessionId) {
            this.name = name;
            this.clientType = clientType;
            this.sessionId = sessionId;
            this.online = false;
            this.lastDeliveredSequence = 0;
            this.connection = null;
        }
    }

    private static final class ConnectionContext {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private Serializer serializer;
        private UserState user;

        private ConnectionContext(Socket socket, DataInputStream in, DataOutputStream out) {
            this.socket = socket;
            this.in = in;
            this.out = out;
        }
    }
}
