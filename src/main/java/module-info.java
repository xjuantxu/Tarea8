module biblioteca {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.xml;


    opens biblioteca to javafx.fxml;
    exports biblioteca;
    exports biblioteca.vista.controllers;
    opens biblioteca.vista.controllers to javafx.fxml;
    opens biblioteca.modelo.dominio to javafx.base;
    opens biblioteca.modelo.negocio.recursos to javafx.base;
    opens biblioteca.modelo.negocio to javafx.base;
}
