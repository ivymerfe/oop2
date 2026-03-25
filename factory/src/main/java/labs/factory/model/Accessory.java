package labs.factory.model;

public class Accessory extends Item {
    public static int size = 100;

    public Accessory() {
        super(ItemType.Accessory, size);
    }
}
