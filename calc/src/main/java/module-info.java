module me.ivy.calc_app {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports me.ivy.calc;
    exports me.ivy.calc_app;
    opens me.ivy.calc_app to javafx.fxml;
}