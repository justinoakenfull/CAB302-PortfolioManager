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
    requires spring.beans;
    requires spring.context;
    requires java.sql;
    requires spring.security.crypto;

    opens com.javarepowizards.portfoliomanager to javafx.fxml;
    exports com.javarepowizards.portfoliomanager;

    exports com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.Portfolio to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.portfolio to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.dashboard to javafx.fxml;

    exports com.javarepowizards.portfoliomanager.controllers.dashboard;

    // Open the controllers.simulation package for reflection by javafx.fxml
    opens com.javarepowizards.portfoliomanager.controllers.simulation to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.watchlist to javafx.fxml;



}