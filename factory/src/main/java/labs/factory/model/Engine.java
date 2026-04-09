package labs.factory.model;

public class Engine extends Item {
    public Engine() {
        super();
    }
    @Override
    public ItemType getType() {
        return ItemType.Engine;
    }
}
