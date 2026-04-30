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
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static final int SAVE_TIMEOUT = 2;

    private final Path savePath;
    private final int port;
    private ChatState chat;
    private Thread saveThread;
    private final Map<String, UserState> sessions = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;
    private final Map<Socket, Connection> connections = new ConcurrentHashMap<>();
    private boolean running = false;

    public Server(Path savePath, int port, boolean loggingEnabled) {
        this.savePath = savePath;
        this.port = port;
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
        this.serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));

        LOGGER.info("Server started on port {}", port);

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

        running = true;
        eventLoop();
    }

    private void eventLoop() throws IOException {
        try {
            while (running) {
                handleAccept();
            }
        } finally {
            stop();
        }
    }

    private void handleAccept() throws IOException {
        Socket clientSocket = serverSocket.accept();
        clientSocket.setTcpNoDelay(true);

        Connection conn = new Connection(clientSocket);
        connections.put(clientSocket, conn);
        conn.startWriter();

        LOGGER.info("Connected: {}", clientSocket.getRemoteSocketAddress());
        Thread.ofVirtual().start(() -> {
            try {
                handleRead(conn);
            } catch (IOException e) {
                closeConnection(conn);
            }
        });
    }

    private void handleRead(Connection conn) throws IOException {
        while (running) {
            Message message = conn.readMessage();
            if (message == null) {
                LOGGER.info("Disconnected: {}", conn.socket.getRemoteSocketAddress());
                closeConnection(conn);
                return;
            }
            try {
                processIncoming(conn, message);
            } catch (Exception e) {
                LOGGER.error(e);
                conn.sendError("Protocol error");
                closeConnection(conn);
                return;
            }
        }
    }

    private void closeConnection(Connection context) {
        UserState user = sessions.get(context.sessionId);
        if (user != null) {
            disconnect(context);
        }
        connections.remove(context.socket);
        context.close();
    }

    private void stop() {
        running = false;

        for (Connection conn : connections.values()) {
            conn.close();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing server", e);
        }

        if (saveThread != null) {
            saveThread.interrupt();
            try {
                saveThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processIncoming(Connection connection, Message message) {
        if (message instanceof ConnectC2S connect) {
            handleLogin(connection, connect);
            return;
        }
        if (message instanceof ListUsersC2S listUsers) {
            handleListUsers(connection, listUsers);
            return;
        }
        if (message instanceof ChatMessageC2S chatMessage) {
            handleChatMessage(connection, chatMessage);
            return;
        }
        if (message instanceof LogoutC2S logout) {
            handleLogout(connection, logout);
            return;
        }
        connection.sendError("Неизвестная команда");
    }

    private void handleLogin(Connection connection, ConnectC2S command) {
        String name = command.getName().trim();
        String clientType = command.getClientType().trim();
        String password = command.getPassword();
        if (name.isEmpty()) {
            connection.sendError("Плохое имя");
            return;
        }
        if (clientType.isEmpty()) {
            connection.sendError("Надо имя клиента");
            return;
        }
        UserState user = chat.findUser(name);
        if (user == null) {
            user = chat.newUser(name, password);
        } else {
            if (!user.testPassword(password)) {
                connection.sendError("Неверный пароль");
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

        connection.send(new ListUsersS2C(getUserInfo()));
        for (ChatMessage msg : chat.getMessages()) {
            if (msg.index() > user.lastReceivedMessage) {
                user.connection.send(new EventMessageS2C(msg.text(), msg.fromName()));
                user.lastReceivedMessage = msg.index();
            }
        }
    }

    private List<UserInfo> getUserInfo() {
        return chat.getUsers().stream().map(u -> new UserInfo(u.name, u.clientType)).toList();
    }

    private void handleListUsers(Connection connection, ListUsersC2S command) {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            connection.sendError("Неизвестная сессия");
            return;
        }
        connection.send(new ListUsersS2C(getUserInfo()));
    }

    private void handleChatMessage(Connection connection, ChatMessageC2S command) {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            connection.sendError("Неизвестная сессия");
            return;
        }
        String text = command.getMessage().trim();
        if (text.isEmpty()) {
            connection.sendError("Незя пустое сообщение");
            return;
        }
        ChatMessage msg = chat.addMessage(user.name, text);
        broadcastMessage(msg, user);
        user.lastReceivedMessage = msg.index();
        connection.send(new MessageResponseS2C());
        LOGGER.info("Message from {}: {}", user.name, text);
    }

    private void handleLogout(Connection connection, LogoutC2S command) {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            connection.sendError("Неизвестная сессия");
            return;
        }
        connection.send(new LogoutResponseS2C());
        disconnect(connection);
    }

    private void broadcast(Message message, UserState excludeUser) {
        List<UserState> usersCopy = sessions.values().stream().toList();
        for (UserState user : usersCopy) {
            if (user != excludeUser) {
                user.connection.send(message);
            }
        }
    }

    private void broadcastMessage(ChatMessage msg, UserState from) {
        Message event = new EventMessageS2C(msg.text(), msg.fromName());
        List<UserState> usersCopy = sessions.values().stream().toList();
        for (UserState user : usersCopy) {
            if (user != from) {
                user.connection.send(event);
                user.lastReceivedMessage = msg.index();
            }
        }
    }

    private void disconnect(Connection connection) {
        UserState user = sessions.get(connection.sessionId);
        if (user == null) {
            return;
        }
        sessions.remove(connection.sessionId);

        LOGGER.info("User disconnected: {}", user.name);
        broadcast(new UserLogoutEventS2C(user.name), user);
    }
}
