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
    requires com.opencsv;

    opens com.javarepowizards.portfoliomanager to javafx.fxml;
    exports com.javarepowizards.portfoliomanager;

    exports com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;

    exports com.javarepowizards.portfoliomanager.controllers.dashboard;



}