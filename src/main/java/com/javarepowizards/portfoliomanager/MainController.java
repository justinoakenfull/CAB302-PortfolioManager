package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.user.IUserDAO;
import com.javarepowizards.portfoliomanager.models.SimulationDifficulty;
import com.javarepowizards.portfoliomanager.services.session.NavigationService;
import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;



import com.javarepowizards.portfoliomanager.services.session.Session;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.javarepowizards.portfoliomanager.dao.stock.StockDAO;
import javafx.util.Duration;

public class MainController implements Initializable {
    @FXML
    private StackPane contentArea;

    @FXML private Button dashboardBtn;
    @FXML private Button watchlistBtn;
    @FXML private Button portfolioBtn;
    @FXML private Button stocksBtn;
    @FXML private Button simulationBtn;
    @FXML private Button myAccountBtn;
    @FXML private Button settingsBtn;
    List<Button> menuButtons;

    private NavigationService nav;

    private final LocalDate mostRecentDate = LocalDate.of(2023, 12, 29);


    private IUserDAO userDAO;
    private IPortfolioDAO portfolioDAO;
    private StockDAO stockDAO;

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        menuButtons = List.of(
                dashboardBtn, watchlistBtn, portfolioBtn,
                stocksBtn, simulationBtn, myAccountBtn, settingsBtn
        );

        stockDAO = stockDAO.getInstance();

        // Initialize the NavigationService with the content area
        nav = new NavigationService(contentArea);
        // Load the initial page (e.g., dashboard)
        showDashboard();

        menuButtons.forEach(this::attachHoverGlow);
        setActivePage(menuButtons.getFirst());
    }

    private void attachHoverGlow(Button btn) {
        Glow glow = new Glow(0.0);
        btn.setEffect(glow);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.levelProperty(), 0.0)
                ),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(glow.levelProperty(), 0.2)
                )
        );
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);

        btn.setOnMouseEntered(e -> pulse.play());
        btn.setOnMouseExited(e -> {
            pulse.stop();
            glow.setLevel(0.0);
        });
    }

    @FXML
    public void showDashboard() {
        setActivePage(menuButtons.getFirst());
        nav.loadView("dashboard/dashboard.fxml", controller ->{});
    }

    @FXML
    private void showWatchlist() {
        setActivePage(menuButtons.get(1));
        nav.loadView("watchlist/watchlist.fxml", controller -> {});
    }

    @FXML
    private void showPortfolio() {
        setActivePage(menuButtons.get(2));
        nav.loadView("portfolio/portfolio.fxml", controller -> {});
    }

    @FXML
    private void showStocks() {
        setActivePage(menuButtons.get(3));
        nav.loadView("stockspage/stocks.fxml", controller -> {});
    }

    @FXML
    private void showSimulation(){
        setActivePage(menuButtons.get(4));
        nav.loadView("simulation/simulation.fxml", controller -> {});
    }

    @FXML
    private void showMyAccount() {
        setActivePage(menuButtons.get(5));
        nav.loadView("useraccounts/userAccountsProfile.fxml", controller -> {});
    }


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



    private void setActivePage(Button activePageButton) {
        menuButtons.forEach(btn -> btn.getStyleClass().remove("active"));
        activePageButton.getStyleClass().add("active");
    }

}