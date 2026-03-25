package labs.factory.model;

import java.util.LinkedList;
import java.util.List;

public class Storage {
    private final List<Item> items = new LinkedList<>();
    private int freeSpace;
    private Runnable itemTakenListener;

    public Storage(int capacity) {
        this.freeSpace = capacity;
    }

    public synchronized int getFreeSpace() {
        return freeSpace;
    }

    public synchronized int getItemCount() {
        return items.size();
    }

    public synchronized void setItemTakenListener(Runnable itemTakenListener) {
        this.itemTakenListener = itemTakenListener;
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
        if (itemTakenListener != null) {
            itemTakenListener.run();
        }
        return item;
    }
}
