package labs.factory.model;

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

    @Override
    public ItemType getType() {
        return ItemType.Auto;
    }

    @Override
    public String toString() {
        return super.toString() + "\n\t" + carcase.toString() + "\n\t" + engine.toString() + "\n\t" + accessory.toString();
    }
}
