package labs.network.client;

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
import labs.network.protocol.s2c.UserLoginEventS2C;
import labs.network.protocol.s2c.UserLogoutEventS2C;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static final int RECONNECT_TIMEOUT = 2;
    private static final int BUFFER_SIZE = 64 * 1024;

    private final Listener listener;

    private String host;
    private int port;

    private String userName;
    private String password;
    private String clientType;
    private Serializer serializer;

    private String session;
    private SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Thread workerThread;
    private boolean running;

    public Client(Listener listener) {
        this.listener = listener;
        this.running = false;
        this.session = null;
    }

    public void start(String host, int port, String userName, String password, String clientType, SerializerMode serializerMode) {
        if (running) {
            return;
        }
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.clientType = clientType;
        this.serializer = serializerMode == SerializerMode.OBJECT ? new ObjectSerializer() : new XMLSerializer();

        workerThread = Thread.ofVirtual().start(this::runLoop);
        running = true;
    }

    public void stop() {
        running = false;
        requestLogout();
        closeSocket();
        if (workerThread != null) {
            workerThread.interrupt();
        }
        listener.onStopped();
    }

    private void runLoop() {
        int attempt = 0;
        while (running) {
            listener.onConnecting(attempt);
            attempt += 1;

            try {
                channel = SocketChannel.open(new InetSocketAddress(host, port));
                channel.configureBlocking(true);
                if (!tryLogin()) {
                    stop();
                    return;
                }
                readMessages();
            } catch (IOException e) {
                if (running) {
                    listener.onDisconnected(e.getMessage());
                }
            } finally {
                session = null;
                closeSocket();
            }
            if (!running) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(RECONNECT_TIMEOUT);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void readMessages() throws IOException {
        while (running) {
            Message message = receive();
            switch (message) {
                case EventMessageS2C eventMessage ->
                        listener.onChatMessage(eventMessage.getFromName(), eventMessage.getMessage());
                case UserLoginEventS2C userLoginEvent ->
                        listener.onUserJoined(userLoginEvent.getName(), userLoginEvent.getClientType());
                case UserLogoutEventS2C userLogoutEvent -> listener.onUserLeft(userLogoutEvent.getName());
                case ListUsersS2C listUsers -> listener.onUsers(listUsers.getUsers());
                case ErrorS2C error -> listener.onError(error.getMessage());
                case LogoutResponseS2C logout -> {
                    return;
                }
                default -> {
                }
            }
        }
    }

    private boolean tryLogin() throws IOException {
        if (!send(new ConnectC2S(userName, clientType, password))) {
            return false;
        }
        Message response = receive();
        if (response instanceof LoginResposeS2C loginResposeS2C) {
            session = loginResposeS2C.getSession();
            listener.onConnected(userName);
            return true;
        }
        if (response instanceof ErrorS2C error) {
            listener.onError(error.getMessage());
        }
        return false;
    }

    public void sendChatMessage(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String currentSession = session;
        if (currentSession == null) {
            listener.onError("Not connected");
            return;
        }
        send(new ChatMessageC2S(text.strip(), currentSession));
    }

    public void requestUsers() {
        String currentSession = session;
        if (currentSession == null) {
            listener.onError("Not connected");
            return;
        }
        send(new ListUsersC2S(currentSession));
    }

    private void requestLogout() {
        send(new LogoutC2S(session));
    }

    private boolean send(Message message) {
        if (serializer == null) {
            return false;
        }
        try {
            byte[] payload = serializer.serialize(message);
            ByteBuffer buf = ByteBuffer.allocate(4 + payload.length);
            buf.putInt(payload.length);
            buf.put(payload);
            buf.flip();
            channel.write(buf);
            return true;
        } catch (IOException e) {
            listener.onError(e.getMessage());
        }
        return false;
    }

    private Message receive() throws IOException {
        if (channel == null || !channel.isOpen()) {
            throw new IOException("Channel is closed");
        }
        while (readBuffer.position() < 4) {
            if (channel.read(readBuffer) == -1) throw new IOException("Disconnected");
        }
        int length = readBuffer.getInt(0);
        if (length <= 0 || length > BUFFER_SIZE - 4) {
            throw new IOException("Invalid message length: " + length);
        }
        while (readBuffer.position() < 4 + length) {
            if (channel.read(readBuffer) == -1) throw new IOException("Disconnected");
        }
        readBuffer.flip();
        readBuffer.getInt();
        byte[] payload = new byte[length];
        readBuffer.get(payload);
        readBuffer.compact();

        return serializer.deserialize(payload);
    }

    private void closeSocket() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }

    public enum SerializerMode {
        XML,
        OBJECT
    }

    public interface Listener {
        void onConnecting(int attempt);

        void onConnected(String username);

        void onDisconnected(String reason);

        void onStopped();

        void onChatMessage(String from, String text);

        void onError(String text);

        void onUserJoined(String username, String clientType);

        void onUserLeft(String username);

        void onUsers(List<UserInfo> users);
    }
}
