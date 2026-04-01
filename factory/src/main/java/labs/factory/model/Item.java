package labs.factory.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Item {
    private static final AtomicInteger globalId = new AtomicInteger(0);
    private final int id;

    public Item() {
        this.id = globalId.incrementAndGet();
    }

    public Item(DataInputStream in) throws IOException {
        this.id = in.readInt();
        globalId.updateAndGet(current -> Math.max(current, this.id + 1));
    }

    public ItemType getType() {
        return ItemType.Item;
    }

    @Override
    public String toString() {
        return getType().toString() + "<" + id + ">";
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(id);
    }
}
