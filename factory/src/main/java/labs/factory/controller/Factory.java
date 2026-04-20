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

    private final Storage<Carcase> carcaseStorage;
    private final Storage<Engine> engineStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Auto> autoStorage;

    private final List<Thread> suppliers = new ArrayList<>();
    private final List<Thread> dealers = new ArrayList<>();

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
            suppliers.add(Thread.ofVirtual().start(supplier));
        }
        for (int i = 0; i < config.engineSuppliersCount; i++) {
            Supplier supplier = new Supplier(engineStorage, ItemType.Engine, () -> config.engineSupplierDelay);
            suppliers.add(Thread.ofVirtual().start(supplier));
        }
        for (int i = 0; i < config.accessorySuppliersCount; i++) {
            Supplier supplier = new Supplier(accessoryStorage, ItemType.Accessory, () -> config.accessorySupplierDelay);
            suppliers.add(Thread.ofVirtual().start(supplier));
        }
        for (int i = 0; i < config.dealersCount; i++) {
            Dealer dealer = new Dealer(i, autoStorage, () -> config.dealerDelay, onSale);
            dealers.add(Thread.ofVirtual().start(dealer));
        }
        workerPool = new ThreadPool(config.workersCount);

        refillTasks();
    }

    public synchronized void stop() throws InterruptedException {
        if (!running) {
            return;
        }
        running = false;

        for (Thread supplier : suppliers) {
            supplier.interrupt();
        }
        for (Thread dealer : dealers) {
            dealer.interrupt();
        }
        workerPool.stop();
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
            Carcase carcase = carcaseStorage.takeItem();
            Engine engine = engineStorage.takeItem();
            Accessory accessory = accessoryStorage.takeItem();
            Thread.sleep(config.workerDelay);
            Auto auto = new Auto(carcase, engine, accessory);
            autoStorage.addItem(auto);
        } catch (InterruptedException ignored) {

        } finally {
            activeWorkers.decrementAndGet();
        }
    }

    public Storage<Engine> getEngineStorage() {
        return engineStorage;
    }

    public Storage<Carcase> getCarcaseStorage() {
        return carcaseStorage;
    }

    public Storage<Accessory> getAccessoryStorage() {
        return accessoryStorage;
    }

    public Storage<Auto> getAutoStorage() {
        return autoStorage;
    }

    public int getTaskCount() {
        return activeWorkers.get();
    }
}
