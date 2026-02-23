package me.ivy.calc;

import atlantafx.base.theme.PrimerDark;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class CalculatorApp extends Application {
    private final Calculator calculator = new Calculator();
    private Stage primaryStage;

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
        this.primaryStage = stage;
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
        if (event.getCode() == KeyCode.ENTER && event.isAltDown()) {
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

    @FXML
    public void handleRunProgram() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить программу");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Program Files (*.calp)", "*.calp")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                String output = calculator.execute(content);
                
                StringBuilder history = new StringBuilder("$file: " + file.getAbsolutePath());
                history.append("\n......\n");
                history.append(output.isEmpty() ? "ok" : output);
                history.append("\n-----\n");
                
                historyArea.appendText(history.toString());
                historyArea.setScrollTop(Double.MAX_VALUE);
                refreshViews();
            } catch (Exception e) {
                showError("Ошибка", "Ошибка при выполнении программы: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить состояние");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("State Files (*.calc)", "*.calc")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName("calculator.calc");

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                StateSerializer.saveState(calculator.getStack(), calculator.getVariables(), file.toPath());
            } catch (Exception e) {
                showError("Ошибка", "Ошибка при сохранении: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Загрузить состояние");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("State Files (*.calc)", "*.calc")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                StateSerializer.CalcState state = StateSerializer.loadState(file.toPath());
                calculator.getStack().clear();
                calculator.getVariables().clear();
                StateSerializer.restoreState(calculator.getStack(), calculator.getVariables(), state);
                refreshViews();
            } catch (Exception e) {
                showError("Ошибка", "Ошибка при загрузке: " + e.getMessage());
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
