package labs.factory.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Auto extends Item {
    public final Carcase carcase;
    public final Engine engine;
    public final Accessory accessory;

    public Auto(Carcase carcase, Engine engine, Accessory accessory) {
        super();
        this.carcase = carcase;
        this.engine = engine;
        this.accessory = accessory;
    }

    public Auto(DataInputStream in) throws IOException {
        super(in);
        this.carcase = new Carcase(in);
        this.engine = new Engine(in);
        this.accessory = new Accessory(in);
    }

    @Override
    public ItemType getType() {
        return ItemType.Auto;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + carcase.toString() + engine.toString() + accessory.toString() + ")";
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        carcase.serialize(out);
        engine.serialize(out);
        accessory.serialize(out);
    }
}
