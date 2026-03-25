module labs.factory {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.apache.logging.log4j;


    opens labs.factory to javafx.fxml;
    exports labs.factory;
}