package me.ivy.calc_app;

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
import me.ivy.calc.Calculator;
import me.ivy.calc.CommandFactory;
import me.ivy.calc.StateSerializer;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalculatorApp extends Application {
    private static final Logger logger = LogManager.getLogger(CalculatorApp.class);
    private static final Calculator calculator = new Calculator(new CommandFactory());

    private Stage primaryStage;

    @FXML
    private TextArea inputArea;

    @FXML
    private TextArea historyArea;

    @FXML
    private ListView<String> stackView;

    @FXML
    private ListView<String> paramView;

    private final ObservableList<String> stackItems = FXCollections.observableArrayList();
    private final ObservableList<String> paramItems = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
        logger.info("Calculator application started");
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
                try {
                    calculator.execute(userInput);
                    history.append("ok");
                } catch (Exception e) {
                    logger.warn("Command execution error: {}", e.getMessage());
                    history.append("Error: ").append(e.getMessage());
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
    public void handleSave() {
        logger.info("Save state initiated by user");

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
                logger.info("State saved successfully to {}", file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Error saving state: {}", e.getMessage());
                showError("Ошибка", "Ошибка при сохранении: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleLoad() {
        logger.info("Load state initiated by user");
        
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
                logger.info("State loaded from {}", file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Error loading state: {}", e.getMessage());
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

    public static void main(String[] args) {
        launch(args);
    }
}
