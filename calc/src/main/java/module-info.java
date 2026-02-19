module me.ivy.calc {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires atlantafx.base;

    exports me.ivy.calc;
    opens me.ivy.calc to javafx.fxml;
    exports me.ivy.calc.commands;
    opens me.ivy.calc.commands to javafx.fxml;
}