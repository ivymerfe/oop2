package labs.network.server;

import labs.network.protocol.*;
import labs.network.protocol.c2s.ConnectC2S;

import java.io.*;
import java.net.Socket;
import java.util.List;

public final class ConnectionContext implements AutoCloseable {
    public final Socket socket;
    public final DataInputStream in;
    public final DataOutputStream out;
    public Serializer serializer;
    public UserState user;

    public ConnectionContext(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())));
    }

    public void send(Message message) throws IOException {
        if (serializer == null) {
            serializer = new XMLSerializer();
        }
        byte[] payload = serializer.serialize(message);
        Payload.write(out, payload);
    }

    public Message receive() throws IOException {
        byte[] payload = Payload.read(in);
        if (serializer != null) {
            return serializer.deserialize(payload);
        }
        List<Serializer> candidates = List.of(new XMLSerializer(), new ObjectSerializer());
        for (Serializer candidate : candidates) {
            try {
                Message message = candidate.deserialize(payload);
                if (message instanceof ConnectC2S) {
                    serializer = candidate;
                    return message;
                }
            } catch (SerializationException ignored) {
            }
        }
        throw new IllegalArgumentException("Failed to detect serializer");
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}

