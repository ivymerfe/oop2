package labs.factory.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Item {
    private static final AtomicInteger globalId = new AtomicInteger(0);

    private final ItemType type;
    private final int id;

    public Item(ItemType type) {
        this.type = type;
        this.id = globalId.incrementAndGet();
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return type.toString() + "<" + id + ">";
    }
}
