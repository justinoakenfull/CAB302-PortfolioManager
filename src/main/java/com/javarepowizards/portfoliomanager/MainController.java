package com.javarepowizards.portfoliomanager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import javafx.scene.Parent;


public class MainController {
    @FXML
    private StackPane contentArea;

    @FXML
    private void showDashboard() {
        loadPage("dashboard/dashboard.fxml");
    }

    @FXML
    private void showWatchlist() {
        loadPage("watchlist/watchlist.fxml");
    }

    @FXML
    private void showPortfolio() {
        loadPage("portfolio/portfolio.fxml");
    }

    @FXML
    private void showStocks() {
        loadPage("stockspage/stocks.fxml");
    }

    @FXML
    private void showSimulation() {
        loadPage("simulation/simulation.fxml");
    }

    @FXML
    private void showSettings() {
        loadPage("settings.fxml"); // need to be implemented
    }

    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/" + page));
            Parent root = loader.load(); // Load the FXML file into the root

            // Clear the current content and add the new loaded page
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();  // Handle exceptions (e.g., file not found)
        }
    }
}