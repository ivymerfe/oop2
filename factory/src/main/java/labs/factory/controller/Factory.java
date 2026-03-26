package labs.factory.controller;

import labs.factory.model.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Factory {
    private final FactoryConfig config;

    private final Storage engineStorage;
    private final Storage carcaseStorage;
    private final Storage accessoryStorage;
    private final Storage autoStorage;

    private final Consumer<String> logConsumer;

    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<Dealer> dealers = new ArrayList<>();

    private ThreadPoolExecutor workerPool;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private Thread storageController;
    private volatile boolean running;

    private final AtomicInteger builtAutos = new AtomicInteger(0);
    private final AtomicInteger soldAutos = new AtomicInteger(0);

    public Factory(FactoryConfig config, Consumer<String> logConsumer) {
        this.config = config;
        this.engineStorage = new Storage(config.engineStorageSize);
        this.carcaseStorage = new Storage(config.carcaseStorageSize);
        this.accessoryStorage = new Storage(config.accessoryStorageSize);
        this.autoStorage = new Storage(config.autoStorageSize);
        this.logConsumer = logConsumer;
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        builtAutos.set(0);
        soldAutos.set(0);

        for (int i = 0; i < config.carcaseSupplierCount; i++) {
            Supplier supplier = new Supplier(carcaseStorage, ItemType.Carcase, () -> config.carcaseDelay);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < config.engineSuppliersCount; i++) {
            Supplier supplier = new Supplier(engineStorage, ItemType.Engine, () -> config.engineDelay);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < config.accessorySuppliersCount; i++) {
            Supplier supplier = new Supplier(accessoryStorage, ItemType.Accessory, () -> config.accessoryDelay);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < config.dealersCount; i++) {
            Dealer dealer = new Dealer(i, autoStorage, () -> config.dealerDelay, this::onAutoSold);
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
        joinAll(suppliers);
        joinAll(dealers);
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
            builtAutos.incrementAndGet();
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

    private void onAutoSold(Dealer dealer, Auto auto) {
        soldAutos.incrementAndGet();
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String line = time + ": " + dealer + " -> " + auto;
        logConsumer.accept(line);
    }

    public synchronized int getBuiltAutos() {
        return builtAutos.get();
    }

    public synchronized int getSoldAutos() {
        return soldAutos.get();
    }

    public int getTaskCount() {
        return activeWorkers.get();
    }

    private void joinAll(List<? extends Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
