module me.ivy.calc.gui {
    requires me.ivy.calc;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports me.ivy.calc_app;
    opens me.ivy.calc_app to javafx.fxml;
}
