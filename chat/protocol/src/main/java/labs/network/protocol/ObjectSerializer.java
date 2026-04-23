package labs.network.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Message message) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(message);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize object message", e);
        }
    }

    @Override
    public Message deserialize(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object value = ois.readObject();
            if (!(value instanceof Message message)) {
                throw new SerializationException("Deserialized value is not a Message");
            }
            return message;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("Failed to deserialize object message", e);
        }
    }
}
