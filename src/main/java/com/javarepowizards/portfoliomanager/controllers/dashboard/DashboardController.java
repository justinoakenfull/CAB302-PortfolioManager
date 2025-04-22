package com.javarepowizards.portfoliomanager.controllers.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class DashboardController {

    @FXML
    private Label watchListLabel;

    @FXML
    public void initialize() {
        System.out.println("▶ DashboardController.initialize() called");
    }
}


