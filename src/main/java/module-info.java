/**
 * Module definition for the Portfolio Manager application.
 */
module com.javarepowizards.portfoliomanager {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // third-party libs
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.opencsv;
    requires org.json;

    // database & security
    requires java.sql;
    requires spring.security.crypto;

    // --- exported packages ---
    exports com.javarepowizards.portfoliomanager;
    exports com.javarepowizards.portfoliomanager.dao;
    exports com.javarepowizards.portfoliomanager.domain;
    exports com.javarepowizards.portfoliomanager.domain.price;
    exports com.javarepowizards.portfoliomanager.domain.stock;
    exports com.javarepowizards.portfoliomanager.models;
    exports com.javarepowizards.portfoliomanager.infrastructure;

    // Services sub-packages
    exports com.javarepowizards.portfoliomanager.services.Auth;
    exports com.javarepowizards.portfoliomanager.services.portfolio;
    exports com.javarepowizards.portfoliomanager.services.session;
    exports com.javarepowizards.portfoliomanager.services.simulation;
    exports com.javarepowizards.portfoliomanager.services.utility;
    exports com.javarepowizards.portfoliomanager.services.watchlist;

    // UI components
    exports com.javarepowizards.portfoliomanager.ui.componants;
    exports com.javarepowizards.portfoliomanager.ui.table.TableRow;

    // --- open packages for JavaFX reflection (FXML controllers) ---
    opens com.javarepowizards.portfoliomanager to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.dashboard to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.portfolio to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.simulation to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.stocks to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.watchlist to javafx.fxml;
}
