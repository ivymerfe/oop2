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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;
    private static final int RECONNECT_TIMEOUT = 2;

    private final String host;
    private final int port;
    private final String userName;
    private final String clientType;
    private final Serializer serializer;
    private final Listener listener;
    private final AtomicBoolean running;
    private final BlockingQueue<Message> outbound;

    private volatile String session;
    private volatile Socket activeSocket;
    private volatile DataOutputStream activeOut;
    private volatile Thread workerThread;
    private volatile Thread writerThread;

    public Client(String host, int port, String userName, String clientType, SerializerMode serializerMode, Listener listener) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.userName = Objects.requireNonNull(userName);
        this.clientType = Objects.requireNonNull(clientType);
        this.serializer = serializerMode == SerializerMode.OBJECT ? new ObjectSerializer() : new XMLSerializer();
        this.listener = Objects.requireNonNull(listener);
        this.running = new AtomicBoolean(false);
        this.outbound = new LinkedBlockingQueue<>();
        this.session = null;
    }

    public synchronized void start() {
        if (running.get()) {
            return;
        }
        running.set(true);
        workerThread = Thread.ofVirtual().start(this::runLoop);
    }

    public synchronized void stop() {
        running.set(false);
        requestLogout();
        closeActiveSocket();
        Thread writer = writerThread;
        if (writer != null) {
            writer.interrupt();
        }
        Thread worker = workerThread;
        if (worker != null) {
            worker.interrupt();
        }
        listener.onDisconnected("Client stopped");
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
        outbound.offer(new ChatMessageC2S(text.strip(), currentSession));
    }

    public void requestUsers() {
        String currentSession = session;
        if (currentSession == null) {
            listener.onError("Not connected");
            return;
        }
        outbound.offer(new ListUsersC2S(currentSession));
    }

    private void runLoop() {
        int attempt = 0;
        while (running.get()) {
            listener.onConnecting(attempt);
            attempt += 1;

            try (Socket socket = new Socket(host, port);
                 DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
                socket.setTcpNoDelay(true);
                activeSocket = socket;
                activeOut = out;
                performLogin(in, out);
                startWriter(out);
                readMessages(in);
            } catch (IOException e) {
                if (running.get()) {
                    listener.onDisconnected(e.getMessage());
                }
            } finally {
                session = null;
                activeOut = null;
                activeSocket = null;
                Thread writer = writerThread;
                if (writer != null) {
                    writer.interrupt();
                }
            }

            if (!running.get()) {
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

    private void performLogin(DataInputStream in, DataOutputStream out) throws IOException {
        sendNow(out, new ConnectC2S(userName, clientType));
        Message response = readSingleMessage(in);
        if (response instanceof LoginResposeS2C loginResposeS2C) {
            session = loginResposeS2C.getSession();
            listener.onConnected(userName);
            return;
        }
        if (response instanceof ErrorS2C error) {
            throw new IOException("Login failed: " + error.getMessage());
        }
        throw new IOException("Unexpected login response: " + response.getClass().getSimpleName());
    }

    private void startWriter(DataOutputStream out) {
        writerThread = Thread.ofVirtual().start(() -> {
            while (running.get()) {
                try {
                    Message next = outbound.poll(1, TimeUnit.SECONDS);
                    if (next == null) {
                        continue;
                    }
                    synchronized (out) {
                        writePayload(out, serializer.serialize(next));
                        out.flush();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    return;
                }
            }
        });
    }

    private void readMessages(DataInputStream in) throws IOException {
        while (running.get()) {
            Message message = readSingleMessage(in);
            if (message instanceof EventMessageS2C eventMessage) {
                listener.onChatMessage(eventMessage.getFromName(), eventMessage.getMessage());
                continue;
            }
            if (message instanceof UserLoginEventS2C userLoginEvent) {
                listener.onUserJoined(userLoginEvent.getName(), userLoginEvent.getClientType());
                continue;
            }
            if (message instanceof UserLogoutEventS2C userLogoutEvent) {
                listener.onUserLeft(userLogoutEvent.getName());
                continue;
            }
            if (message instanceof ListUsersS2C listUsers) {
                listener.onUsers(listUsers.getUsers());
                continue;
            }
            if (message instanceof ErrorS2C error) {
                listener.onError(error.getMessage());
                continue;
            }
            if (message instanceof LogoutResponseS2C) {
                return;
            }
        }
    }

    private Message readSingleMessage(DataInputStream in) throws IOException {
        byte[] payload = readPayload(in);
        return serializer.deserialize(payload);
    }

    private void sendNow(DataOutputStream out, Message message) throws IOException {
        writePayload(out, serializer.serialize(message));
        out.flush();
    }

    private void requestLogout() {
        DataOutputStream out = activeOut;
        String currentSession = session;
        if (out == null || currentSession == null) {
            return;
        }
        try {
            sendNow(out, new LogoutC2S(currentSession));
        } catch (IOException ignored) {
        }
    }

    private void closeActiveSocket() {
        Socket socket = activeSocket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private byte[] readPayload(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length <= 0 || length > MAX_PAYLOAD_SIZE) {
            throw new IOException("Invalid payload size: " + length);
        }
        byte[] payload = new byte[length];
        try {
            in.readFully(payload);
        } catch (EOFException e) {
            throw new IOException("Connection closed", e);
        }
        return payload;
    }

    private void writePayload(DataOutputStream out, byte[] payload) throws IOException {
        out.writeInt(payload.length);
        out.write(payload);
    }

    public enum SerializerMode {
        XML,
        OBJECT
    }

    public interface Listener {
        void onConnecting(int attempt);

        void onConnected(String username);

        void onDisconnected(String reason);

        void onChatMessage(String from, String text);

        void onError(String text);

        void onUserJoined(String username, String clientType);

        void onUserLeft(String username);

        void onUsers(List<UserInfo> users);
    }
}
