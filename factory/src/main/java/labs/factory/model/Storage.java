package labs.factory.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Storage {
    private final List<Item> items = new LinkedList<>();
    private int freeSpace;

    private final Object takeLock = new Object();

    public Storage(int capacity) {
        this.freeSpace = capacity;
    }

    public synchronized int getFreeSpace() {
        return freeSpace;
    }

    public synchronized int getItemCount() {
        return items.size();
    }

    public void waitForTake() throws InterruptedException {
        synchronized (takeLock) {
            takeLock.wait();
        }
    }

    public synchronized void addItem(Item item) throws InterruptedException {
        while (freeSpace == 0) {
            wait();
        }
        freeSpace -= 1;
        items.add(item);
        notifyAll();
    }

    public synchronized Item takeItem() throws InterruptedException {
        while (items.isEmpty()) {
            wait();
        }
        Item item = items.removeLast();
        freeSpace += 1;
        notifyAll();
        synchronized (takeLock) {
            takeLock.notifyAll();
        }
        return item;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(items.size());
        for (Item item : items) {
            out.writeInt(item.getType().ordinal());
            item.serialize(out);
        }
    }

    public void deserialize(DataInputStream in) throws IOException {
        int itemCount = in.readInt();
        for (int i = 0; i < itemCount; i++) {
            ItemType type = ItemType.values()[in.readInt()];
            Item item = switch (type) {
                case Carcase -> new Carcase(in);
                case Accessory -> new Accessory(in);
                case Engine -> new Engine(in);
                case Auto -> new Auto(in);
                case Item -> new Item(in);
            };
            items.add(item);
            freeSpace -= 1;
        }
    }
}
