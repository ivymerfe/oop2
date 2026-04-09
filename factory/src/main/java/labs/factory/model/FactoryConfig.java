package labs.factory.model;

import java.io.Serializable;

public class FactoryConfig implements Serializable {
    public int carcaseStorageSize = 10;
    public int engineStorageSize = 10;
    public int accessoryStorageSize = 10;
    public int autoStorageSize = 6;
    public int carcaseSupplierCount = 2;
    public int carcaseSupplierDelay = 500;
    public int engineSuppliersCount = 2;
    public int engineSupplierDelay = 1000;
    public int accessorySuppliersCount = 2;
    public int accessorySupplierDelay = 800;
    public int workersCount = 5;
    public int workerDelay = 2000;
    public int dealersCount = 4;
    public int dealerDelay = 8000;
}
