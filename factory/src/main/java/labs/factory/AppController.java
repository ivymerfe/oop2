package labs.factory;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AppController {
    public TextField bodySize;
    @FXML
    public TextField motorSize;
    @FXML
    public TextField accessorySize;
    @FXML
    public TextArea logArea;
    @FXML
    public TextField bodySuppliers;
    @FXML
    public Slider bodySupplierSpeed;
    @FXML
    public TextField engineSuppliers;
    @FXML
    public Slider engineSupplierSpeed;
    @FXML
    public TextField accessorySuppliers;
    @FXML
    public Slider accessorySupplierSpeed;
    @FXML
    public TextField workerCount;
    @FXML
    public Slider workerSpeed;
    @FXML
    public TextField dealerCount;
    @FXML
    public Slider dealerSpeed;

    @FXML
    public Label carcaseCount;
    @FXML
    public Label carcaseFreeSpace;
    @FXML
    public Label engineCount;
    @FXML
    public Label engineFreeSpace;
    @FXML
    public Label accessoryCount;
    @FXML
    public Label accessoryFreeSpace;
    @FXML
    public Label autoCount;
    @FXML
    public Label autoFreeSpace;
    @FXML
    public Label autoProduced;
    @FXML
    public Label tasksInQueue;


    @FXML
    public void startFactory() {

    }

    @FXML
    public void stopFactory() {

    }
}
