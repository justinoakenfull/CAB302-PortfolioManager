package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.UserDAO;
import com.javarepowizards.portfoliomanager.models.SimulationDifficulty;
import com.javarepowizards.portfoliomanager.services.NavigationService;

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
import org.springframework.beans.factory.annotation.Autowired;

public class MainController {
    @FXML
    private StackPane contentArea;

    private NavigationService nav;

    @FXML
    private BorderPane rootLayout;

    @Autowired
    private UserDAO userDAO;

    private PortfolioDAO portfolioDAO;
    private StockDAO stockDAO;

    @FXML
    public void initialize() {
        // Initialize the NavigationService with the content area
        nav = new NavigationService(contentArea);
        // Load the initial page (e.g., dashboard)
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        System.out.println("Showing dashboard");
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
        nav.loadView("simulation/simulation.fxml", controller -> {});
    }


    @FXML
    private void showSettings(){
        nav.loadView("useraccounts/settings.fxml", controller -> {});

    }


    // old method
    @FXML
    private void showSimulationOld() {

        StockDAO stockDAO = StockDAO.getInstance();

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

    /* MUST BE LOGGED IN TO UPDATE SIMULATION DIFFICULTY*/
    @FXML
    private void showSettingsOld() {
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