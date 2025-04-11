package com.javarepowizards.portfoliomanager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    //Placeholder buttons for nav bar
    @FXML
    private void showDashboard() {
        System.out.println("Dashboard clicked");
    }

    @FXML
    private void showWatchlist() {
        System.out.println("Watchlist clicked");
    }

    @FXML
    private void showPortfolio() {
        System.out.println("Portfolio clicked");
    }

    @FXML
    private void showStocks() {
        System.out.println("Stocks clicked");
    }

    @FXML
    private void showSimulation() {
        System.out.println("Simulation clicked");
    }

    @FXML
    private void showSettings() {
        System.out.println("Settings clicked");
    }



    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}