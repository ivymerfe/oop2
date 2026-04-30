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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Logger LOGGER = LogManager.getLogger(Server.class);
    private static final int SAVE_TIMEOUT = 2;

    private final Path savePath;
    private final int port;
    private ChatState chat;
    private Thread saveThread;
    private final Map<String, UserState> sessions = new HashMap<>();

    private ServerSocketChannel serverChannel;
    private Selector selector;
    private final Map<SocketChannel, Connection> connections = new HashMap<>();
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
        this.serverChannel = ServerSocketChannel.open();
        this.selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

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
                int readyCount = selector.select(1000);
                if (readyCount == 0) {
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept();
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                        closeConnection(key);
                    }
                }
            }
        } finally {
            stop();
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);

            Connection context = new Connection(clientChannel);
            connections.put(clientChannel, context);
            clientChannel.register(selector, SelectionKey.OP_READ, context);

            LOGGER.info("Connected: {}", clientChannel.getRemoteAddress());
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Connection context = (Connection) key.attachment();

        ByteBuffer buffer = context.readBuffer;
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            LOGGER.info("Disconnected: {}", channel.getRemoteAddress());
            closeConnection(key);
            return;
        }

        if (bytesRead > 0) {
            buffer.flip();

            while (buffer.remaining() > 0) {
                try {
                    Message message = context.read(buffer);
                    if (message != null) {
                        processIncoming(context, message);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    context.sendError("Protocol error");
                    closeConnection(key);
                    return;
                }
            }

            buffer.compact();
        }
    }

    private void closeConnection(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        Connection context = (Connection) key.attachment();

        if (context != null) {
            UserState user = sessions.get(context.sessionId);
            if (user != null) {
                disconnect(context);
            }
        }

        connections.remove(channel);
        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void stop() {
        running = false;

        for (Connection conn : connections.values()) {
            try {
                conn.channel.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing server: {}", e.getMessage());
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

    private void processIncoming(Connection connection, Message message) throws IOException {
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

    private void handleLogin(Connection connection, ConnectC2S command) throws IOException {
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
                try {
                    user.connection.send(new EventMessageS2C(msg.text(), msg.fromName()));
                    user.lastReceivedMessage = msg.index();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    private List<UserInfo> getUserInfo() {
        return chat.getUsers().stream().map(u -> new UserInfo(u.name, u.clientType)).toList();
    }

    private void handleListUsers(Connection connection, ListUsersC2S command) throws IOException {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            connection.sendError("Неизвестная сессия");
            return;
        }
        connection.send(new ListUsersS2C(getUserInfo()));
    }

    private void handleChatMessage(Connection connection, ChatMessageC2S command) throws IOException {
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

    private void handleLogout(Connection connection, LogoutC2S command) throws IOException {
        UserState user = sessions.get(command.getSession());
        if (user == null) {
            connection.sendError("Неизвестная сессия");
            return;
        }
        connection.send(new LogoutResponseS2C());
        disconnect(connection);
    }

    private void broadcast(Message message, UserState excludeUser) {
        for (UserState user : sessions.values()) {
            if (user != excludeUser) {
                try {
                    user.connection.send(message);
                } catch (IOException e) {
                    LOGGER.error("Error broadcasting to {}: {}", user.name, e.getMessage());
                }
            }
        }
    }

    private void broadcastMessage(ChatMessage msg, UserState from) {
        Message event = new EventMessageS2C(msg.text(), msg.fromName());
        for (UserState user : sessions.values()) {
            if (user != from) {
                try {
                    user.connection.send(event);
                    user.lastReceivedMessage = msg.index();
                } catch (IOException e) {
                    LOGGER.error("Error broadcasting to {}: {}", user.name, e.getMessage());
                }
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
