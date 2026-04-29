package labs.network.server;

import labs.network.protocol.*;
import labs.network.protocol.c2s.ConnectC2S;
import labs.network.protocol.s2c.ErrorS2C;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

public class Connection {
    private static final int BUFFER_SIZE = 64 * 1024;

    final SocketChannel channel;
    final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    public Serializer serializer;
    public String sessionId;

    Connection(SocketChannel channel) {
        this.channel = channel;
    }

    void send(Message message) throws IOException {
        if (serializer == null) {
            serializer = new XMLSerializer();
        }
        byte[] payload = serializer.serialize(message);
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
        buf.putInt(payload.length);
        buf.put(payload);
        buf.flip();
        channel.write(buf);
    }

    public void sendError(String error) throws IOException {
        send(new ErrorS2C(error));
    }

    public Message read(ByteBuffer buffer) {
        if (buffer.remaining() < 4) {
            return null;
        }
        int messageSize = buffer.getInt();
        if (buffer.remaining() < messageSize) {
            return null;
        }
        byte[] data = new byte[messageSize];
        buffer.get(data);

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
}
