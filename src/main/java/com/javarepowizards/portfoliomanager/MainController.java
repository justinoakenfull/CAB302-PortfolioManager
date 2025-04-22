package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.UserDAO;
import com.javarepowizards.portfoliomanager.models.SimulationDifficulty;

import com.javarepowizards.portfoliomanager.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;

public class MainController {
    @FXML
    private StackPane contentArea;

    @FXML
    private BorderPane rootLayout;

    private PortfolioDAO portfolioDAO;
    private StockDAO stockDAO;


    @FXML
    public void showDashboard() {
        System.out.println("Showing dashboard");
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

        StockDAO stockDAO = new StockDAO();

        // Define the date for which you want to simulate.
        LocalDate date = LocalDate.of(2023, 12, 29);

        // Use the PortfolioInitializer to create your dummy portfolio.
        PortfolioDAO portfolioDAO;
        try {
            portfolioDAO = com.javarepowizards.portfoliomanager.services.PortfolioInitializer.createDummyPortfolio(stockDAO, date);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }


        try {
            // Create a custom FXMLLoader instance
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/javarepowizards/portfoliomanager/views/simulation/simulation.fxml"));
            Parent simulationRoot = loader.load();

            // Retrieve the SimulationController instance
            com.javarepowizards.portfoliomanager.controllers.simulation.SimulationController simController = loader.getController();

            // Inject dependencies into the SimulationController.
            // For example, if you have these available:
            simController.setPortfolioDAO(portfolioDAO);  // yourPortfolioDAO should already be available
            simController.setStockDAO(stockDAO);          // yourStockDAO should already be available
            simController.setMostRecentDate(LocalDate.of(2023, 12, 29)); // Or any dynamic date

            // Now clear the content area and add the simulation page.
            contentArea.getChildren().clear();
            contentArea.getChildren().add(simulationRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void showSettings() {
        SimulationDifficulty defaultDifficulty = SimulationDifficulty.values()[0];
        ChoiceDialog<SimulationDifficulty> dlg = new ChoiceDialog<>(defaultDifficulty,
                List.of(SimulationDifficulty.values()));
        dlg.setTitle("User Settings");
        dlg.setHeaderText("Select your desired Simulation Difficulty.");
        dlg.setContentText("Difficulty:");

        DialogPane pane = dlg.getDialogPane();
        String css = getClass()
                .getResource(
                        "/com/javarepowizards/portfoliomanager/views/useraccounts/settings.css"
                )
                .toExternalForm();

        pane.getStylesheets().add(css);
        pane.getStyleClass().add("dialog-pane");

        dlg.showAndWait().ifPresent(diff -> {
                try {
                    UserDAO userDAO = new UserDAO();
                    int userId = Session.getCurrentUser().getUserId();
                    userDAO.updateSimulationDifficulty(userId, diff.name());

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
        });



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