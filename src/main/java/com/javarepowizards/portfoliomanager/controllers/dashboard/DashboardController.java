package com.javarepowizards.portfoliomanager.controllers.dashboard;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;


public class DashboardController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       System.out.println("DashboardController initialized");

        // Run simulation after dashboard loads
        SimulationTestRunner testRunner = new SimulationTestRunner();
        testRunner.runTestSimulation();
    }
}
