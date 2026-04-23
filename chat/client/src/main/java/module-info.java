module labs.network.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires labs.network.protocol;

    opens labs.network.client to javafx.fxml;
    exports labs.network.client;
}