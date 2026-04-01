package labs.factory.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Accessory extends Item {
    public Accessory() {
        super();
    }

    public Accessory(DataInputStream in) throws IOException {
        super(in);
    }

    @Override
    public ItemType getType() {
        return ItemType.Accessory;
    }
}
