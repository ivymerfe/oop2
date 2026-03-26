package labs.factory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import labs.factory.controller.Factory;
import labs.factory.model.FactoryConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppController {
    private static final Logger logger = LogManager.getLogger(AppController.class);

    public TextField carcaseStorageSize;
    public TextField motorStorageSize;
    public TextField accessoryStorageSize;
    public TextField autoStorageSize;

    public TextArea logArea;
    public TextField carcaseSupplierCount;
    public Slider carcaseSupplierDelay;
    public TextField engineSuppliers;
    public Slider engineSupplierDelay;
    public TextField accessorySuppliers;
    public Slider accessorySupplierDelay;
    public TextField workerCount;
    public Slider workerDelay;
    public TextField dealerCount;
    public Slider dealerDelay;

    public Label carcaseCount;
    public Label carcaseFreeSpace;
    public Label engineCount;
    public Label engineFreeSpace;
    public Label accessoryCount;
    public Label accessoryFreeSpace;
    public Label autoCount;
    public Label autoFreeSpace;
    public Label autoProduced;
    public Label autoSold;
    public Label tasksInQueue;

    private final FactoryConfig config = new FactoryConfig();
    private Factory factory;
    private Timeline uiTimer;

    public void initialize() {
        carcaseStorageSize.setText(String.valueOf(config.carcaseStorageSize));
        motorStorageSize.setText(String.valueOf(config.engineStorageSize));
        accessoryStorageSize.setText(String.valueOf(config.accessoryStorageSize));
        autoStorageSize.setText(String.valueOf(config.autoStorageSize));
        carcaseSupplierCount.setText(String.valueOf(config.carcaseSupplierCount));
        engineSuppliers.setText(String.valueOf(config.engineSuppliersCount));
        accessorySuppliers.setText(String.valueOf(config.accessorySuppliersCount));
        workerCount.setText(String.valueOf(config.workersCount));
        dealerCount.setText(String.valueOf(config.dealersCount));
        updateUi();
    }

    public void startFactory() {
        stopFactory();

        readUi();

        factory = new Factory(config, this::appendSaleLog);
        factory.start();

        uiTimer = new Timeline(new KeyFrame(Duration.millis(200), event -> updateUi()));
        uiTimer.setCycleCount(Timeline.INDEFINITE);
        uiTimer.play();
    }

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
        updateUi();
    }

    private void appendSaleLog(String line) {
        logger.info(line);
        Platform.runLater(() -> {
            logArea.appendText(line + System.lineSeparator());
        });
    }

    private void readUi() {
        config.carcaseStorageSize = parseOrDefault(carcaseStorageSize.getText(), config.carcaseStorageSize);
        config.engineStorageSize = parseOrDefault(motorStorageSize.getText(), config.engineStorageSize);
        config.accessoryStorageSize = parseOrDefault(accessoryStorageSize.getText(), config.accessoryStorageSize);
        config.autoStorageSize = parseOrDefault(autoStorageSize.getText(), config.autoStorageSize);

        config.carcaseSupplierCount = parseOrDefault(carcaseSupplierCount.getText(), config.carcaseSupplierCount);
        config.engineSuppliersCount = parseOrDefault(engineSuppliers.getText(), config.engineSuppliersCount);
        config.accessorySuppliersCount = parseOrDefault(accessorySuppliers.getText(), config.accessorySuppliersCount);
        config.workersCount = parseOrDefault(workerCount.getText(), config.workersCount);
        config.dealersCount = parseOrDefault(dealerCount.getText(), config.dealerDelay);

        updateDelays();
    }

    private void updateDelays() {
        config.carcaseDelay = (int) carcaseSupplierDelay.getValue();
        config.engineDelay = (int) engineSupplierDelay.getValue();
        config.accessoryDelay = (int) accessorySupplierDelay.getValue();
        config.workerDelay = (int) workerDelay.getValue();
        config.dealerDelay = (int) dealerDelay.getValue();
    }

    private void updateUi() {
        if (factory == null) {
            return;
        }
        updateDelays();
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
