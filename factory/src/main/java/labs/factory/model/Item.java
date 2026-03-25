package labs.factory.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Item {
    static AtomicInteger globalId;

    private ItemType type;
    private int id;
    private int size;

    public Item(ItemType type, int size) {
        this.type = type;
        this.size = size;
        this.id = globalId.incrementAndGet();
    }

    public ItemType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return type.toString() + "<" + id + ">";
    }
}
