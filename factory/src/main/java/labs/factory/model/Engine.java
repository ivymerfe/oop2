package labs.factory.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Engine extends Item {
    public Engine() {
        super();
    }

    public Engine(DataInputStream in) throws IOException {
        super(in);
    }

    @Override
    public ItemType getType() {
        return ItemType.Engine;
    }
}
