package labs.factory.model;

public class Accessory extends Item {
    public Accessory() {
        super();
    }

    @Override
    public ItemType getType() {
        return ItemType.Accessory;
    }
}
