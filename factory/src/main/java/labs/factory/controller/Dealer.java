package labs.factory.controller;

import labs.factory.model.Auto;
import labs.factory.model.Storage;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

public class Dealer extends Thread {
    private final int dealerId;
    private final Storage autoStorage;
    private final IntSupplier delaySupplier;
    private final BiConsumer<Dealer, Auto> onSale;

    public Dealer(int dealerId, Storage autoStorage, IntSupplier delaySupplier, BiConsumer<Dealer, Auto> onSale) {
        super();
        this.dealerId = dealerId;
        this.autoStorage = autoStorage;
        this.delaySupplier = delaySupplier;
        this.onSale = onSale;
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
                Auto auto = (Auto) autoStorage.takeItem();
                if (onSale != null) {
                    onSale.accept(this, auto);
                }
                Thread.sleep(delaySupplier.getAsInt());
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Dealer-" + this.dealerId;
    }
}
