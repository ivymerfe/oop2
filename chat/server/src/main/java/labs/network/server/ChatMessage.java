package labs.network.server;

import java.io.Serial;
import java.io.Serializable;

public record ChatMessage(String fromName, String text) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
