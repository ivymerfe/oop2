package labs.factory.controller;

import labs.factory.model.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

public class Supplier extends Thread {
    private final Storage storage;
    private final ItemType itemType;
    private final IntSupplier delaySupplier;

    public Supplier(Storage storage, ItemType itemType, IntSupplier delaySupplier) {
        super();
        this.storage = storage;
        this.itemType = itemType;
        this.delaySupplier = delaySupplier;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(0, delaySupplier.getAsInt()));
        } catch (InterruptedException e) {
            interrupt();
            return;
        }
        while (!isInterrupted()) {
            try {
                Item item = switch (itemType) {
                    case Carcase -> new Carcase();
                    case Engine -> new Engine();
                    case Accessory -> new Accessory();
                    default -> throw new RuntimeException("bad item");
                };
                this.storage.addItem(item);
                Thread.sleep(delaySupplier.getAsInt());
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}
