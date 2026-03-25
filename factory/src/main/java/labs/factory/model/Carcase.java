package labs.factory.model;

public class Carcase extends Item {
    public static int size = 100;

    public Carcase() {
        super(ItemType.Carcase, size);
    }
}
