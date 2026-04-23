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

import java.util.List;

public class AppController {
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField nameField;
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
    private Client client;

    @FXML
    private void initialize() {
        serializerChoice.setItems(FXCollections.observableArrayList("xml", "object"));
        serializerChoice.setValue("xml");
        hostField.setText("localhost");
        portField.setText("5000");
        clientTypeField.setText("javafx-client");
        messagesList.setItems(messages);
        usersList.setItems(users);
        updateButtons(false);
    }

    public void init(List<String> args) {
        for (String arg : args) {
            if (arg.startsWith("--host=")) {
                hostField.setText(arg.substring("--host=".length()));
            } else if (arg.startsWith("--port=")) {
                portField.setText(arg.substring("--port=".length()));
            } else if (arg.startsWith("--name=")) {
                nameField.setText(arg.substring("--name=".length()));
            } else if (arg.startsWith("--serializer=")) {
                String value = arg.substring("--serializer=".length()).toLowerCase();
                if ("xml".equals(value) || "object".equals(value)) {
                    serializerChoice.setValue(value);
                }
            } else if (arg.startsWith("--type=")) {
                clientTypeField.setText(arg.substring("--type=".length()));
            }
        }
    }

    @FXML
    public void connect() {
        if (client != null) {
            return;
        }
        String host = hostField.getText().strip();
        String portValue = portField.getText().strip();
        String name = nameField.getText().strip();
        String clientType = clientTypeField.getText().strip();
        if (host.isEmpty() || portValue.isEmpty() || name.isEmpty() || clientType.isEmpty()) {
            appendMessage("[error] Fill all connection fields");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            appendMessage("[error] Invalid port");
            return;
        }

        Client.SerializerMode mode = "object".equalsIgnoreCase(serializerChoice.getValue())
                ? Client.SerializerMode.OBJECT
                : Client.SerializerMode.XML;

        client = new Client(host, port, name, clientType, mode, new UiListener());
        client.start();
        updateButtons(true);
    }

    @FXML
    public void disconnect() {
        Client current = client;
        client = null;
        if (current != null) {
            current.stop();
        }
        updateButtons(false);
    }

    @FXML
    public void sendMessage() {
        String text = messageField.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        Client current = client;
        if (current == null) {
            appendMessage("[error] Not connected");
            return;
        }
        current.sendChatMessage(text);
        messageField.clear();
    }

    @FXML
    public void refreshUsers() {
        Client current = client;
        if (current == null) {
            appendMessage("[error] Not connected");
            return;
        }
        current.requestUsers();
    }

    private void appendMessage(String text) {
        Platform.runLater(() -> {
            messages.add(text);
            messagesList.scrollTo(messages.size() - 1);
        });
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void updateUsers(List<String> newUsers) {
        Platform.runLater(() -> users.setAll(newUsers));
    }

    private void updateButtons(boolean connected) {
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
    }

    private class UiListener implements Client.Listener {
        @Override
        public void onConnecting(String status) {
            updateStatus(status);
            appendMessage("[system] " + status);
        }

        @Override
        public void onConnected(String status) {
            updateStatus(status);
            appendMessage("[system] " + status);
        }

        @Override
        public void onDisconnected(String status) {
            updateStatus(status);
            appendMessage("[system] " + status);
        }

        @Override
        public void onChatMessage(String from, String text) {
            appendMessage(from + ": " + text);
        }

        @Override
        public void onSystemEvent(String text) {
            appendMessage("[system] " + text);
        }

        @Override
        public void onUsers(List<String> users) {
            updateUsers(users);
        }

        @Override
        public void onError(String text) {
            appendMessage("[error] " + text);
        }
    }
}
