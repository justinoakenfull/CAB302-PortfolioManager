module com.javarepowizards.portfoliomanager {
    requires javafx.controls;
    requires javafx.fxml;
    // … your other requires …

    opens com.javarepowizards.portfoliomanager to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.useraccounts to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.portfolio to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.controllers.dashboard to javafx.fxml;

    opens com.javarepowizards.portfoliomanager.views.portfolio to javafx.fxml;
    opens com.javarepowizards.portfoliomanager.views.dashboard to javafx.fxml;

    exports com.javarepowizards.portfoliomanager;
    exports com.javarepowizards.portfoliomanager.controllers.useraccounts;
    exports com.javarepowizards.portfoliomanager.controllers.dashboard;
}

