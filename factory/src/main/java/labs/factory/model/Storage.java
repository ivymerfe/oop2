package labs.factory.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Storage<T extends Item> implements Serializable {
    @Serial
    private static final long serialVersionUID = 0;

    private final List<T> items = new LinkedList<>();
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

    public synchronized void addItem(T item) throws InterruptedException {
        while (freeSpace == 0) {
            wait(100);
        }
        freeSpace -= 1;
        items.add(item);
        notifyAll();
    }

    public T takeItem() throws InterruptedException {
        T item;
        synchronized (this) {
            while (items.isEmpty()) {
                wait(100);
            }
            item = items.removeLast();
            freeSpace += 1;
            notifyAll();
        }
        if (takeListener != null) {
            takeListener.run();
        }
        return item;
    }
}
