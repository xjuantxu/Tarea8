module biblioteca {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens biblioteca to javafx.fxml;
    exports biblioteca;
    exports biblioteca.vista.controllers;
    opens biblioteca.vista.controllers to javafx.fxml;
    opens biblioteca.modelo.dominio to javafx.base;
}