package labs.factory.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Carcase extends Item {
    public Carcase() {
        super();
    }

    public Carcase(DataInputStream in) throws IOException {
        super(in);
    }

    @Override
    public ItemType getType() {
        return ItemType.Carcase;
    }
}
