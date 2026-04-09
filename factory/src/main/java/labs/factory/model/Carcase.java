package labs.factory.model;

public class Carcase extends Item {
    public Carcase() {
        super();
    }

    @Override
    public ItemType getType() {
        return ItemType.Carcase;
    }
}
