package labs.factory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import labs.factory.controller.Dealer;
import labs.factory.controller.Factory;
import labs.factory.model.Auto;
import labs.factory.model.FactoryConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Label autoSold;
    public Label tasksInQueue;

    private static final Path SAVE_PATH = Path.of("factory.bin");

    private final AtomicInteger soldAutos = new AtomicInteger(0);
    private FactoryConfig config = new FactoryConfig();
    private Factory factory = new Factory(config);
    private Timeline uiTimer;

    public void initialize() {
        loadUi();
    }

    public boolean load() {
        if (!Files.exists(SAVE_PATH)) {
            return false;
        }
        try (InputStream stream = Files.newInputStream(SAVE_PATH)) {
            ObjectInputStream in = new ObjectInputStream(stream);
            config = (FactoryConfig) in.readObject();
            stopFactory();
            factory = new Factory(config, in);
            loadUi();
            return true;
        } catch (IOException e) {
            logger.error(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void save() {
        try (OutputStream stream = Files.newOutputStream(SAVE_PATH)) {
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(config);
            factory.serialize(out);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void newFactory() {
        stopFactory();
        readUi();
        factory = new Factory(config);
        updateUi();
    }

    public void startFactory() {
        readUi();

        factory.start(this::onSale);

        uiTimer = new Timeline(new KeyFrame(Duration.millis(200), event -> updateUi()));
        uiTimer.setCycleCount(Timeline.INDEFINITE);
        uiTimer.play();
    }

    public void stopFactory() {
        try {
            factory.stop();
        } catch (InterruptedException e) {
            logger.error(e);
        }
        if (uiTimer != null) {
            uiTimer.stop();
            uiTimer = null;
        }
    }

    private void onSale(Dealer dealer, Auto auto) {
        soldAutos.incrementAndGet();
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String line = time + ": " + dealer + ":\n" + auto;
        logger.info(line);
        Platform.runLater(() -> {
            logArea.appendText(line + System.lineSeparator());
        });
    }

    private void loadUi() {
        carcaseStorageSize.setText(String.valueOf(config.carcaseStorageSize));
        motorStorageSize.setText(String.valueOf(config.engineStorageSize));
        accessoryStorageSize.setText(String.valueOf(config.accessoryStorageSize));
        autoStorageSize.setText(String.valueOf(config.autoStorageSize));
        carcaseSupplierCount.setText(String.valueOf(config.carcaseSupplierCount));
        carcaseSupplierDelay.setValue(config.carcaseSupplierDelay);
        engineSuppliers.setText(String.valueOf(config.engineSuppliersCount));
        engineSupplierDelay.setValue(config.engineSupplierDelay);
        accessorySuppliers.setText(String.valueOf(config.accessorySuppliersCount));
        accessorySupplierDelay.setValue(config.accessorySupplierDelay);
        workerCount.setText(String.valueOf(config.workersCount));
        workerDelay.setValue(config.workerDelay);
        dealerCount.setText(String.valueOf(config.dealersCount));
        dealerDelay.setValue(config.dealerDelay);
        updateUi();
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
        config.carcaseSupplierDelay = (int) carcaseSupplierDelay.getValue();
        config.engineSupplierDelay = (int) engineSupplierDelay.getValue();
        config.accessorySupplierDelay = (int) accessorySupplierDelay.getValue();
        config.workerDelay = (int) workerDelay.getValue();
        config.dealerDelay = (int) dealerDelay.getValue();
    }

    private void updateUi() {
        updateDelays();
        setLabel(carcaseCount, factory.getCarcaseStorage().getItemCount());
        setLabel(carcaseFreeSpace, factory.getCarcaseStorage().getFreeSpace());
        setLabel(engineCount, factory.getEngineStorage().getItemCount());
        setLabel(engineFreeSpace, factory.getEngineStorage().getFreeSpace());
        setLabel(accessoryCount, factory.getAccessoryStorage().getItemCount());
        setLabel(accessoryFreeSpace, factory.getAccessoryStorage().getFreeSpace());
        setLabel(autoCount, factory.getAutoStorage().getItemCount());
        setLabel(autoFreeSpace, factory.getAutoStorage().getFreeSpace());
        setLabel(autoSold, soldAutos.get());
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
