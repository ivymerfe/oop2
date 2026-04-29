package labs.network.server;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class UserState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public final String name;
    public String clientType;
    public boolean hasPassword;
    public byte[] passwordHash;
    public transient ConnectionContext connection;

    public UserState(String name, String password) {
        this.name = name;
        this.connection = null;
        setPassword(password);
    }

    public byte[] hash(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            return digest.digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPassword(String pass) {
        if (!pass.isEmpty()) {
            hasPassword = true;
            passwordHash = hash(pass);
        }
    }

    public boolean testPassword(String password) {
        if (!hasPassword) {
            setPassword(password);
            return true;
        }
        byte[] h = hash(password);
        return Arrays.equals(h, passwordHash);
    }
}
