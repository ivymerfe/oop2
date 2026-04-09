package labs.factory.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 0;

    private final String id;

    public Item() {
        this.id = UUID.randomUUID().toString();
    }
    public ItemType getType() {
        return ItemType.Item;
    }

    @Override
    public String toString() {
        return getType().toString() + "<" + id + ">";
    }

}
