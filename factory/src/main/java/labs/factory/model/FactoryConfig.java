package labs.factory.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FactoryConfig {
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

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(carcaseStorageSize);
        out.writeInt(engineStorageSize);
        out.writeInt(accessoryStorageSize);
        out.writeInt(autoStorageSize);
        out.writeInt(carcaseSupplierCount);
        out.writeInt(carcaseSupplierDelay);
        out.writeInt(engineSuppliersCount);
        out.writeInt(engineSupplierDelay);
        out.writeInt(accessorySuppliersCount);
        out.writeInt(accessorySupplierDelay);
        out.writeInt(workersCount);
        out.writeInt(workerDelay);
        out.writeInt(dealersCount);
        out.writeInt(dealerDelay);
    }

    public void deserialize(DataInputStream in) throws IOException {
        carcaseStorageSize = in.readInt();
        engineStorageSize = in.readInt();
        accessoryStorageSize = in.readInt();
        autoStorageSize = in.readInt();
        carcaseSupplierCount = in.readInt();
        carcaseSupplierDelay = in.readInt();
        engineSuppliersCount = in.readInt();
        engineSupplierDelay = in.readInt();
        accessorySuppliersCount = in.readInt();
        accessorySupplierDelay = in.readInt();
        workersCount = in.readInt();
        workerDelay = in.readInt();
        dealersCount = in.readInt();
        dealerDelay = in.readInt();
    }
}
