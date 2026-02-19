package me.ivy.calc;

import atlantafx.base.theme.PrimerDark;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class CalculatorApp extends Application {
    private final Calculator calculator = new Calculator();

    private final ObservableList<String> stackItems = FXCollections.observableArrayList();
    private final ObservableList<String> paramItems = FXCollections.observableArrayList();

    @FXML
    private TextArea inputArea;

    @FXML
    private TextArea historyArea;

    @FXML
    private ListView<String> stackView;

    @FXML
    private ListView<String> paramView;

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        stage.setTitle("Calculator");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void initialize() {
        stackView.setItems(stackItems);
        paramView.setItems(paramItems);

        inputArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleInputKey);
    }

    private void handleInputKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
            event.consume();
            String userInput = inputArea.getText().trim();

            if (!userInput.isEmpty()) {
                StringBuilder history = new StringBuilder(userInput);
                history.append("\n......\n");
                String output = calculator.execute(userInput);
                if (!output.isEmpty()) {
                    history.append(output);
                } else {
                    history.append("ok");
                }
                history.append("\n-----\n");

                historyArea.appendText(history.toString());
                historyArea.setScrollTop(Double.MAX_VALUE);
                inputArea.clear();
                refreshViews();
            }
        }
    }

    private void refreshViews() {
        stackItems.setAll(calculator.getStackItems());
        paramItems.setAll(calculator.getVariableItems());
    }
}
