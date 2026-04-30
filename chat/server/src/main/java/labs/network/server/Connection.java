package labs.network.server;

import labs.network.protocol.*;
import labs.network.protocol.c2s.ConnectC2S;
import labs.network.protocol.s2c.ErrorS2C;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final ByteBuffer SIZE_BUFFER_TEMPLATE = ByteBuffer.allocate(Integer.BYTES);

    final Socket socket;
    final InputStream inputStream;
    final OutputStream outputStream;
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private Thread writerThread;
    public Serializer serializer;
    public String sessionId;
    private final ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES);

    Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
        this.outputStream = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
    }

    void startWriter() {
        assert writerThread == null;
        writerThread = Thread.ofVirtual().start(this::writerLoop);
    }

    void send(Message message) {
        sendQueue.offer(message);
    }

    public void sendError(String error) {
        send(new ErrorS2C(error));
    }

    void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        Thread thread = writerThread;
        if (thread != null) {
            thread.interrupt();
        }
        sendQueue.clear();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void writerLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = sendQueue.take();
                write(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            close();
        }
    }

    private void write(Message message) throws IOException {
        if (serializer == null) {
            serializer = new XMLSerializer();
        }
        byte[] payload = serializer.serialize(message);
        sizeBuffer.clear();
        sizeBuffer.putInt(payload.length);
        sizeBuffer.flip();

        outputStream.write(sizeBuffer.array(), 0, Integer.BYTES);
        outputStream.write(payload);
        outputStream.flush();
    }

    public Message readMessage() throws IOException {
        byte[] sizeBytes = readFully(Integer.BYTES);
        if (sizeBytes == null) {
            return null;
        }
        int messageSize = ByteBuffer.wrap(sizeBytes).getInt();
        byte[] data = readFully(messageSize);
        if (data == null) {
            return null;
        }

        if (serializer != null) {
            return serializer.deserialize(data);
        }
        List<Serializer> candidates = List.of(new XMLSerializer(), new ObjectSerializer());
        for (Serializer candidate : candidates) {
            try {
                Message message = candidate.deserialize(data);
                if (message instanceof ConnectC2S) {
                    serializer = candidate;
                    return message;
                }
            } catch (SerializationException ignored) {
            }
        }
        throw new IllegalArgumentException("Failed to detect serializer");
    }

    private byte[] readFully(int size) throws IOException {
        byte[] data = new byte[size];
        int offset = 0;
        while (offset < size) {
            int read = inputStream.read(data, offset, size - offset);
            if (read == -1) {
                return offset == 0 ? null : throwUnexpectedEof(size, offset);
            }
            offset += read;
        }
        return data;
    }

    private byte[] throwUnexpectedEof(int expected, int actual) throws EOFException {
        throw new EOFException("Unexpected end of stream: expected " + expected + " bytes, got " + actual);
    }
}
