package labs.factory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import labs.factory.controller.Factory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppController {
    private static final Logger logger = LogManager.getLogger(AppController.class);

    @FXML
    public TextField carcaseSize;
    @FXML
    public TextField motorSize;
    @FXML
    public TextField accessorySize;
    @FXML
    public TextField autoSize;

    @FXML
    public TextArea logArea;
    @FXML
    public TextField bodySuppliers;
    @FXML
    public Slider bodySupplierSpeed;
    @FXML
    public TextField engineSuppliers;
    @FXML
    public Slider engineSupplierSpeed;
    @FXML
    public TextField accessorySuppliers;
    @FXML
    public Slider accessorySupplierSpeed;
    @FXML
    public TextField workerCount;
    @FXML
    public Slider workerSpeed;
    @FXML
    public TextField dealerCount;
    @FXML
    public Slider dealerSpeed;

    @FXML
    public Label carcaseCount;
    @FXML
    public Label carcaseFreeSpace;
    @FXML
    public Label engineCount;
    @FXML
    public Label engineFreeSpace;
    @FXML
    public Label accessoryCount;
    @FXML
    public Label accessoryFreeSpace;
    @FXML
    public Label autoCount;
    @FXML
    public Label autoFreeSpace;
    @FXML
    public Label autoProduced;
    @FXML
    public Label autoSold;
    @FXML
    public Label tasksInQueue;

    private Factory factory;
    private Timeline uiTimer;

    @FXML
    public void initialize() {
        updateStats();
    }


    @FXML
    public void startFactory() {
        stopFactory();

        int bodyStorage = parseOrDefault(carcaseSize.getText(), 100);
        int engineStorage = parseOrDefault(motorSize.getText(), 100);
        int accessoryStorage = parseOrDefault(accessorySize.getText(), 100);
        int autoStorage = parseOrDefault(autoSize.getText(), 100);

        int bodySuppliersCount = parseOrDefault(bodySuppliers.getText(), 2);
        int engineSuppliersCount = parseOrDefault(engineSuppliers.getText(), 2);
        int accessorySuppliersCount = parseOrDefault(accessorySuppliers.getText(), 2);
        int workers = parseOrDefault(workerCount.getText(), 5);
        int dealers = parseOrDefault(dealerCount.getText(), 4);

        factory = new Factory(
                bodyStorage,
                engineStorage,
                accessoryStorage,
                autoStorage,
                bodySuppliersCount,
                engineSuppliersCount,
                accessorySuppliersCount,
                workers,
                dealers,
                () -> (int) bodySupplierSpeed.getValue(),
                () -> (int) engineSupplierSpeed.getValue(),
                () -> (int) accessorySupplierSpeed.getValue(),
                () -> (int) workerSpeed.getValue(),
                () -> (int) dealerSpeed.getValue(),
                this::appendSaleLog
        );

        factory.start();

        uiTimer = new Timeline(new KeyFrame(Duration.millis(200), event -> updateStats()));
        uiTimer.setCycleCount(Timeline.INDEFINITE);
        uiTimer.play();
    }

    @FXML
    public void stopFactory() {
        if (uiTimer != null) {
            uiTimer.stop();
            uiTimer = null;
        }
        if (factory != null) {
            try {
                factory.stop();
            } catch (InterruptedException e) {
                logger.error(e);
            }
            factory = null;
        }
        updateStats();
    }

    private void appendSaleLog(String line) {
        logger.info(line);
        Platform.runLater(() -> {
            logArea.appendText(line + System.lineSeparator());
        });
    }

    private void updateStats() {
        if (factory == null) {
            return;
        }
        setLabel(carcaseCount, factory.getCarcaseStorage().getItemCount());
        setLabel(carcaseFreeSpace, factory.getCarcaseStorage().getFreeSpace());
        setLabel(engineCount, factory.getEngineStorage().getItemCount());
        setLabel(engineFreeSpace, factory.getEngineStorage().getFreeSpace());
        setLabel(accessoryCount, factory.getAccessoryStorage().getItemCount());
        setLabel(accessoryFreeSpace, factory.getAccessoryStorage().getFreeSpace());
        setLabel(autoCount, factory.getAutoStorage().getItemCount());
        setLabel(autoFreeSpace, factory.getAutoStorage().getFreeSpace());
        setLabel(autoProduced, factory.getBuiltAutos());
        setLabel(autoSold, factory.getSoldAutos());
        setLabel(tasksInQueue, factory.getTaskCount());
    }

    private void setLabel(Label label, int value) {
        label.setText(String.valueOf(value));
    }

    private int parseOrDefault(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
