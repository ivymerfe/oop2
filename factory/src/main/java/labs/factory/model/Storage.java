package labs.factory.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Storage implements Serializable {
    @Serial
    private static final long serialVersionUID = 0;

    private final List<Item> items = new LinkedList<>();
    private int freeSpace;

    private transient Runnable takeListener;

    public Storage(int capacity) {
        this.freeSpace = capacity;
    }

    public void setTakeListener(Runnable r) {
        takeListener = r;
    }

    public synchronized int getFreeSpace() {
        return freeSpace;
    }

    public synchronized int getItemCount() {
        return items.size();
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
        if (takeListener != null) {
            takeListener.run();
        }
        return item;
    }
}
