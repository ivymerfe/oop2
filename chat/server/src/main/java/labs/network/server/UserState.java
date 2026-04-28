package labs.network.server;

public class UserState {
    public final String name;
    public final String sessionId;
    public String clientType;
    public String passwordHash;
    public boolean online;
    public ConnectionContext connection;

    public UserState(String name, String clientType, String sessionId) {
        this.name = name;
        this.clientType = clientType;
        this.sessionId = sessionId;
        this.online = false;
        this.connection = null;
    }
}
