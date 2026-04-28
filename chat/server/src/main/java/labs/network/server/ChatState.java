package labs.network.server;

import java.io.Serial;
import java.io.Serializable;

public class ChatState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<UserState> users;

}
