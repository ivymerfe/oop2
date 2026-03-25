package labs.factory.model;

public class Auto extends Item {
    private final Carcase carcase;
    private final Engine engine;
    private final Accessory accessory;

    public Auto(Carcase carcase, Engine engine, Accessory accessory) {
        super(ItemType.Auto);
        this.carcase = carcase;
        this.engine = engine;
        this.accessory = accessory;
    }

    public Carcase getCarcase() {
        return carcase;
    }

    public Engine getEngine() {
        return engine;
    }

    public Accessory getAccessory() {
        return accessory;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + carcase.toString() + engine.toString() + accessory.toString() + ")";
    }
}
