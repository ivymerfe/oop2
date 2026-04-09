package labs.factory.controller;

import labs.factory.model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
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

    private ThreadPool workerPool;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private boolean running;

    public Factory(FactoryConfig config) {
        this.config = config;
        this.carcaseStorage = new Storage(config.carcaseStorageSize);
        this.engineStorage = new Storage(config.engineStorageSize);
        this.accessoryStorage = new Storage(config.accessoryStorageSize);
        this.autoStorage = new Storage(config.autoStorageSize);
        this.autoStorage.setTakeListener(this::refillTasks);
    }

    public Factory(FactoryConfig config, ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.config = config;
        this.carcaseStorage = (Storage) in.readObject();
        this.engineStorage = (Storage) in.readObject();
        this.accessoryStorage = (Storage) in.readObject();
        this.autoStorage = (Storage) in.readObject();
        this.autoStorage.setTakeListener(this::refillTasks);
    }

    public void serialize(ObjectOutputStream out) throws IOException {
        out.writeObject(carcaseStorage);
        out.writeObject(engineStorage);
        out.writeObject(accessoryStorage);
        out.writeObject(autoStorage);
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
        workerPool = new ThreadPool(config.workersCount);

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
        workerPool.stop();
        for (Supplier sup : suppliers) {
            sup.join();
        }
        for (Dealer dealer : dealers) {
            dealer.join();
        }
        suppliers.clear();
        dealers.clear();
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
}
