package labs.network.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Payload {
    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    public static byte[] read(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length <= 0 || length > MAX_PAYLOAD_SIZE) {
            throw new IOException("Invalid payload size: " + length);
        }
        byte[] payload = new byte[length];
        in.readFully(payload);
        return payload;
    }

    public static void write(DataOutputStream out, byte[] payload) throws IOException {
        out.writeInt(payload.length);
        out.write(payload);
    }
}
