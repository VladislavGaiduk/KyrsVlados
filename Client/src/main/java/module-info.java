module Client {
    requires com.fasterxml.jackson.databind;
    requires jakarta.persistence;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;

    requires java.sql;
    requires java.desktop;
    requires com.google.gson;

    opens com.gui.controllers to javafx.fxml;
    opens com.server.models to com.google.gson, javafx.base;
    opens com.server.utils to com.google.gson, javafx.base;
    opens com.server.network to com.google.gson;
    exports com.server.enums to com.google.gson;
    opens com.gui.services to javafx.fxml;
    exports com;

}