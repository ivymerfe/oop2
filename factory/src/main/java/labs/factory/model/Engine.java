package labs.factory.model;

public class Engine extends Item {
    public static int size = 100;

    public Engine() {
        super(ItemType.Engine, size);
    }
}
