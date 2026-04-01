package labs.factory.controller;

import labs.factory.model.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class Factory {
    private final FactoryConfig config;

    private final Storage carcaseStorage;
    private final Storage engineStorage;
    private final Storage accessoryStorage;
    private final Storage autoStorage;

    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<Dealer> dealers = new ArrayList<>();

    private ThreadPoolExecutor workerPool;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private Thread storageController;
    private boolean running;

    public Factory(FactoryConfig config) {
        this.config = config;
        this.engineStorage = new Storage(config.engineStorageSize);
        this.carcaseStorage = new Storage(config.carcaseStorageSize);
        this.accessoryStorage = new Storage(config.accessoryStorageSize);
        this.autoStorage = new Storage(config.autoStorageSize);
    }

    public synchronized void start(BiConsumer<Dealer, Auto> onSale) {
        if (running) {
            return;
        }
        running = true;
        for (int i = 0; i < config.carcaseSupplierCount; i++) {
            Supplier supplier = new Supplier(carcaseStorage, ItemType.Carcase, () -> config.carcaseSupplierDelay);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < config.engineSuppliersCount; i++) {
            Supplier supplier = new Supplier(engineStorage, ItemType.Engine, () -> config.engineSupplierDelay);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < config.accessorySuppliersCount; i++) {
            Supplier supplier = new Supplier(accessoryStorage, ItemType.Accessory, () -> config.accessorySupplierDelay);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < config.dealersCount; i++) {
            Dealer dealer = new Dealer(i, autoStorage, () -> config.dealerDelay, onSale);
            dealers.add(dealer);
            dealer.start();
        }
        workerPool = new ThreadPoolExecutor(
                config.workersCount, config.workersCount,
                100000, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        storageController = new Thread(this::storageControllerThread);
        storageController.start();

        refillTasks();
    }

    public synchronized void stop() throws InterruptedException {
        if (!running) {
            return;
        }
        running = false;

        for (Supplier supplier : suppliers) {
            supplier.interrupt();
        }
        for (Dealer dealer : dealers) {
            dealer.interrupt();
        }
        if (storageController != null) {
            storageController.interrupt();
        }
        workerPool.shutdownNow();
        for (Supplier sup : suppliers) {
            sup.join();
        }
        for (Dealer dealer : dealers) {
            dealer.join();
        }
        suppliers.clear();
        dealers.clear();
        storageController.join();
        storageController = null;
    }

    private void refillTasks() {
        if (!running) return;

        int free = autoStorage.getFreeSpace();
        int target = Math.min(free, config.workersCount);

        while (activeWorkers.get() < target && running) {
            workerPool.submit(this::buildTask);
            activeWorkers.incrementAndGet();
        }
    }

    public void buildTask() {
        try {
            Carcase carcase = (Carcase) carcaseStorage.takeItem();
            Engine engine = (Engine) engineStorage.takeItem();
            Accessory accessory = (Accessory) accessoryStorage.takeItem();
            Thread.sleep(config.workerDelay);
            Auto auto = new Auto(carcase, engine, accessory);
            autoStorage.addItem(auto);
        } catch (InterruptedException ignored) {

        } finally {
            activeWorkers.decrementAndGet();
            refillTasks();
        }
    }

    public void storageControllerThread() {
        while (running) {
            try {
                autoStorage.waitForTake();
            } catch (InterruptedException e) {
                break;
            }
            refillTasks();
        }
    }

    public Storage getEngineStorage() {
        return engineStorage;
    }

    public Storage getCarcaseStorage() {
        return carcaseStorage;
    }

    public Storage getAccessoryStorage() {
        return accessoryStorage;
    }

    public Storage getAutoStorage() {
        return autoStorage;
    }

    public int getTaskCount() {
        return activeWorkers.get();
    }

    public void serialize(DataOutputStream out) throws IOException {
        carcaseStorage.serialize(out);
        engineStorage.serialize(out);
        accessoryStorage.serialize(out);
        autoStorage.serialize(out);
    }

    public void deserialize(DataInputStream in) throws IOException {
        carcaseStorage.deserialize(in);
        engineStorage.deserialize(in);
        accessoryStorage.deserialize(in);
        autoStorage.deserialize(in);
    }
}
