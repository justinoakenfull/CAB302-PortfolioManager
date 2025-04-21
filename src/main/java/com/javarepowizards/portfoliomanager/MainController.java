package com.javarepowizards.portfoliomanager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainController {
    @FXML
    private StackPane contentArea;

    @FXML
    private BorderPane rootLayout;


    @FXML
    public void showDashboard() {
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
            Parent fxml = FXMLLoader.load(getClass().getResource("/com/javarepowizards/portfoliomanager/views/" + page));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}