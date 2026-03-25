package labs.factory.model;

public class Auto extends Item {
    Carcase carcase;
    Engine engine;
    Accessory accessory;

    public Auto(Carcase carcase, Engine engine, Accessory accessory) {
        super(ItemType.Auto, carcase.getSize() + engine.getSize() + accessory.getSize());
        this.carcase = carcase;
        this.engine = engine;
        this.accessory = accessory;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + carcase.toString() + engine.toString() + accessory.toString() + ")";
    }
}
