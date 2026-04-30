package labs.network.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import labs.network.protocol.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class AppController {
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField nameField;
    @FXML
    public TextField passField;
    @FXML
    private TextField clientTypeField;
    @FXML
    private ChoiceBox<String> serializerChoice;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<String> messagesList;
    @FXML
    private ListView<String> usersList;
    @FXML
    private TextField messageField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    public Button refreshUsersButton;

    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final ObservableList<String> users = FXCollections.observableArrayList();
    private List<UserInfo> userInfos = new ArrayList<>();
    private Client client;

    @FXML
    private void initialize() {
        client = new Client(new UiListener());
        serializerChoice.setItems(FXCollections.observableArrayList("xml", "object"));
        serializerChoice.setValue("xml");
        hostField.setText("localhost");
        portField.setText("6666");
        clientTypeField.setText("lol");
        messagesList.setItems(messages);
        usersList.setItems(users);
        updateButtons(false);
    }

    @FXML
    public void connect() {
        String host = hostField.getText().strip();
        String portValue = portField.getText().strip();
        String name = nameField.getText().strip();
        String password = passField.getText().strip();
        String clientType = clientTypeField.getText().strip();
        if (host.isEmpty() || portValue.isEmpty() || name.isEmpty() || clientType.isEmpty()) {
            appendError("Заполни все поля");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            appendError("Плохой порт");
            return;
        }
        Client.SerializerMode mode = "object".equalsIgnoreCase(serializerChoice.getValue())
                ? Client.SerializerMode.OBJECT
                : Client.SerializerMode.XML;

        client.start(host, port, name, password, clientType, mode);
        updateButtons(true);
    }

    @FXML
    public void disconnect() {
        client.stop();
    }

    @FXML
    public void sendMessage() {
        String text = messageField.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        Client current = client;
        if (current == null) {
            appendError("Not connected");
            return;
        }
        current.sendChatMessage(text);
        appendMessage("Я", text);
        messageField.clear();
    }

    @FXML
    public void refreshUsers() {
        Client current = client;
        if (current == null) {
            appendError("Not connected");
            return;
        }
        current.requestUsers();
    }

    private void appendText(String text) {
        Platform.runLater(() -> {
            messages.add(text);
            messagesList.scrollTo(messages.size() - 1);
        });
    }

    private void appendError(String error) {
        appendText("[error] " + error);
    }

    private void appendInfo(String info) {
        appendText("[info] " + info);
    }

    private void appendMessage(String from, String message) {
        appendText("("+ from + ") > " + message);
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void updateUsers() {
        List<String> newUsers = userInfos.stream().map(info -> info.name() + " (" + info.clientType() + ")").toList();
        Platform.runLater(() -> users.setAll(newUsers));
    }

    private void updateButtons(boolean running) {
        connectButton.setDisable(running);
        disconnectButton.setDisable(!running);
    }

    private class UiListener implements Client.Listener {
        @Override
        public void onConnecting(int attempt) {
            String status = attempt == 0 ? "Подключаюсь" : "Реконнект";
            updateStatus(status);
            appendInfo(status);
        }

        @Override
        public void onConnected(String username) {
            String status = "Подключился как: " + username;
            updateStatus(status);
            appendInfo(status);
        }

        @Override
        public void onDisconnected(String reason) {
            String status = "Отключился: " + reason;
            updateStatus(status);
            appendInfo(status);
        }

        @Override
        public void onStopped() {
            updateStatus("Отключен");
            updateButtons(false);
        }

        @Override
        public void onChatMessage(String from, String text) {
            appendMessage(from, text);
        }

        @Override
        public void onUserJoined(String username, String clientType) {
            userInfos.add(new UserInfo(username, clientType));
            updateUsers();
            appendInfo(username + " подключился через клиент " + clientType);
        }

        @Override
        public void onUserLeft(String username) {
            userInfos.removeIf(u -> u.name().equals(username));
            updateUsers();
            appendInfo(username + " отключился");
        }

        @Override
        public void onError(String text) {
            appendError(text);
        }

        @Override
        public void onUsers(List<UserInfo> newUsers) {
            userInfos = newUsers;
            updateUsers();
        }
    }
}
