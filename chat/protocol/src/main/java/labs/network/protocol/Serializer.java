package labs.network.protocol;

public interface Serializer {
    byte[] serialize(Message message);

    Message deserialize(byte[] data);
}
