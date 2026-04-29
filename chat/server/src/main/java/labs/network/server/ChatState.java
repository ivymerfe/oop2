package labs.network.server;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class ChatState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, UserState> users = new HashMap<>();
    private List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public Collection<UserState> getUsers() {
        return users.values();
    }

    public UserState findUser(String name) {
        return users.get(name);
    }

    public UserState newUser(String name, String password) {
        UserState user = new UserState(name, password);
        users.put(name, user);
        return user;
    }
}
