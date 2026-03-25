package labs.factory.model;

import java.util.LinkedList;
import java.util.List;

public class Storage {
    List<Item> items = new LinkedList<>();
    int capacity;
    int freeSpace;

    public Storage(int capacity) {
        this.capacity = capacity;
        this.freeSpace = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public synchronized int getFreeSpace() {
        return freeSpace;
    }

    public synchronized boolean hasItem() {
        return !items.isEmpty();
    }

    public synchronized void addItem(Item item) throws InterruptedException {
        while (freeSpace < item.getSize()) {
            wait();
        }
        freeSpace -= item.getSize();
        items.add(item);
        notifyAll();
    }

    public synchronized Item takeItem() throws InterruptedException {
        while (items.isEmpty()) {
            wait();
        }
        Item item = items.removeLast();
        freeSpace += item.getSize();
        notifyAll();
        return item;
    }
}
