package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.models.SimulationDifficulty;
import com.javarepowizards.portfoliomanager.services.NavigationService;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.InMemoryPortfolioDAO;


import com.javarepowizards.portfoliomanager.services.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.Parent;

import com.javarepowizards.portfoliomanager.dao.StockDAO;
import org.springframework.beans.factory.annotation.Autowired;

public class MainController implements Initializable {
    @FXML
    private StackPane contentArea;

    private NavigationService nav;

    private final LocalDate mostRecentDate = LocalDate.of(2023, 12, 29);

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IPortfolioDAO portfolioDAO;
    @Autowired
    private StockDAO stockDAO;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        stockDAO = stockDAO.getInstance();
        portfolioDAO = new InMemoryPortfolioDAO();

        // Initialize the NavigationService with the content area
        nav = new NavigationService(contentArea);
        // Load the initial page (e.g., dashboard)
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        nav.loadView("dashboard/dashboard.fxml", controller ->{});
    }

    @FXML
    private void showWatchlist() {
        nav.loadView("watchlist/watchlist.fxml", controller -> {});
    }

    @FXML
    private void showPortfolio() {
        nav.loadView("portfolio/portfolio.fxml", controller -> {});
    }

    @FXML
    private void showStocks() {
        nav.loadView("stockspage/stocks.fxml", controller -> {});
    }

    @FXML
    private void showSimulation(){
        nav.loadView("simulation/simulation.fxml", controller -> {
            var sim = (com.javarepowizards.portfoliomanager.controllers.simulation.SimulationController) controller;
            sim.setPortfolioDAO(portfolioDAO);
            sim.setStockDAO(stockDAO);
            sim.setMostRecentDate(LocalDate.of(2023, 12, 29));
        });
    }

    @FXML
    private void showMyAccount() { nav.loadView("useraccounts/userAccountsProfile.fxml", controller -> {});}


    /* MUST BE LOGGED IN TO UPDATE SIMULATION DIFFICULTY*/
    @FXML
    private void showSettings() {
        // Set the default selected difficulty (first enum value, "Easy" by default)
        SimulationDifficulty defaultDifficulty = SimulationDifficulty.values()[0];

        // Create a choice dialog with all difficulty levels as options
        ChoiceDialog<SimulationDifficulty> dlg = new ChoiceDialog<>(defaultDifficulty,
                List.of(SimulationDifficulty.values()));

        // Set dialog window title and content
        dlg.setTitle("User Settings");
        dlg.setHeaderText("Select your desired Simulation Difficulty.");
        dlg.setContentText("Difficulty:");

        // Load and apply external CSS styling to the dialog
        DialogPane pane = dlg.getDialogPane();
        String css = getClass()
                .getResource("/com/javarepowizards/portfoliomanager/views/useraccounts/settings.css")
                .toExternalForm();
        pane.getStylesheets().add(css);
        pane.getStyleClass().add("dialog-pane");

        // Show the dialog and wait for user input
        dlg.showAndWait().ifPresent(diff -> {
            try {
                // Retrieve the current logged-in user's ID from the session
                int userId = Session.getCurrentUser().getUserId();
                userDAO = AppContext.getUserDAO();
                // Use the injected userDAO to update the user's selected simulation difficulty
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