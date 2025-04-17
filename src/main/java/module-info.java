module com.javarepowizards.portfoliomanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires spring.security.crypto;
    requires spring.context;
    requires java.desktop;
    requires spring.beans;

    opens com.javarepowizards.portfoliomanager to javafx.fxml;
    exports com.javarepowizards.portfoliomanager;

    exports com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;

}