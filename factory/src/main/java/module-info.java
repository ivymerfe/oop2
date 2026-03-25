module labs.factory {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;


    opens labs.factory to javafx.fxml;
    exports labs.factory;
}