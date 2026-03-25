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
import java.util.function.IntSupplier;

public class Factory {
    private final Storage engineStorage;
    private final Storage carcaseStorage;
    private final Storage accessoryStorage;
    private final Storage autoStorage;

    private final int carcaseSupplierCount;
    private final int engineSuppliersCount;
    private final int accessorySuppliersCount;
    private final int workersCount;
    private final int dealersCount;

    private final IntSupplier bodyDelaySupplier;
    private final IntSupplier engineDelaySupplier;
    private final IntSupplier accessoryDelaySupplier;
    private final IntSupplier workerDelaySupplier;
    private final IntSupplier dealerDelaySupplier;

    private final Consumer<String> logConsumer;

    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<Dealer> dealers = new ArrayList<>();

    private ThreadPoolExecutor workerPool;
    private final AtomicInteger activeWorkers = new AtomicInteger(0);

    private Thread storageController;
    private volatile boolean running;

    private final AtomicInteger builtAutos = new AtomicInteger(0);
    private final AtomicInteger soldAutos = new AtomicInteger(0);

    public Factory(int carcaseStorageSize, int engineStorageSize, int accessoryStorageSize, int autoStorageSize, int carcaseSupplierCount, int engineSuppliersCount, int accessorySuppliersCount, int workersCount, int dealersCount, IntSupplier bodyDelaySupplier, IntSupplier engineDelaySupplier, IntSupplier accessoryDelaySupplier, IntSupplier workerDelaySupplier, IntSupplier dealerDelaySupplier, Consumer<String> logConsumer) {
        this.engineStorage = new Storage(engineStorageSize);
        this.carcaseStorage = new Storage(carcaseStorageSize);
        this.accessoryStorage = new Storage(accessoryStorageSize);
        this.autoStorage = new Storage(autoStorageSize);
        this.carcaseSupplierCount = carcaseSupplierCount;
        this.engineSuppliersCount = engineSuppliersCount;
        this.accessorySuppliersCount = accessorySuppliersCount;
        this.workersCount = workersCount;
        this.dealersCount = dealersCount;
        this.bodyDelaySupplier = bodyDelaySupplier;
        this.engineDelaySupplier = engineDelaySupplier;
        this.accessoryDelaySupplier = accessoryDelaySupplier;
        this.workerDelaySupplier = workerDelaySupplier;
        this.dealerDelaySupplier = dealerDelaySupplier;
        this.logConsumer = logConsumer;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        builtAutos.set(0);
        soldAutos.set(0);

        autoStorage.setItemTakenListener(this::refillTasks);

        for (int i = 0; i < carcaseSupplierCount; i++) {
            Supplier supplier = new Supplier(carcaseStorage, ItemType.Carcase, bodyDelaySupplier);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < engineSuppliersCount; i++) {
            Supplier supplier = new Supplier(engineStorage, ItemType.Engine, engineDelaySupplier);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < accessorySuppliersCount; i++) {
            Supplier supplier = new Supplier(accessoryStorage, ItemType.Accessory, accessoryDelaySupplier);
            suppliers.add(supplier);
            supplier.start();
        }
        for (int i = 0; i < dealersCount; i++) {
            Dealer dealer = new Dealer(i, autoStorage, dealerDelaySupplier, this::onAutoSold);
            dealers.add(dealer);
            dealer.start();
        }
        workerPool = new ThreadPoolExecutor(
                workersCount, workersCount,
                100000, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        refillTasks();
    }

    public synchronized void stop() throws InterruptedException {
        if (!running) {
            return;
        }
        running = false;
        autoStorage.setItemTakenListener(null);

        for (Supplier supplier : suppliers) {
            supplier.interrupt();
        }
        for (Dealer dealer : dealers) {
            dealer.interrupt();
        }
        if (storageController != null) {
            storageController.interrupt();
        }
        joinAll(suppliers);
        joinAll(dealers);
        workerPool.shutdownNow();
        storageController.join();
        suppliers.clear();
        dealers.clear();
        storageController = null;
    }

    private void refillTasks() {
        if (!running) return;

        int free = autoStorage.getFreeSpace();
        int target = Math.min(free, workersCount);

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
            Thread.sleep(workerDelaySupplier.getAsInt());
            Auto auto = new Auto(carcase, engine, accessory);
            autoStorage.addItem(auto);
            builtAutos.incrementAndGet();
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
