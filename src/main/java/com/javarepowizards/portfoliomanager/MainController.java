package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.stock.IStockDAO;
import com.javarepowizards.portfoliomanager.dao.stock.StockDAO;
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
import javafx.util.Duration;

/**
 * Controller for the main application window.
 * Manages navigation between views and UI animations for menu buttons.
 */
public class MainController implements Initializable {
    @FXML
    private StackPane contentArea;

    @FXML private Button dashboardBtn;
    @FXML private Button watchlistBtn;
    @FXML private Button portfolioBtn;
    @FXML private Button stocksBtn;
    @FXML private Button simulationBtn;
    @FXML private Button settingsBtn;
    List<Button> menuButtons;

    private NavigationService nav;


    private IUserDAO userDAO;
    private IPortfolioDAO portfolioDAO;
    private IStockDAO stockDAO;


    /**
     * Initializes the controller after its FXML components have been injected.
     * Sets up menu buttons, navigation service, and loads the default view.
     *
     * @param url            the location used to resolve relative paths, or null if unknown
     * @param resourceBundle the resources used to localize the root object, or null if none
     */
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        menuButtons = List.of(
                dashboardBtn, watchlistBtn, portfolioBtn,
                stocksBtn, simulationBtn, settingsBtn
        );

        stockDAO = StockDAO.getInstance();

        // Initialize the NavigationService with the content area
        nav = new NavigationService(contentArea);
        // Load the initial page (e.g., dashboard)
        showDashboard();

        menuButtons.forEach(this::attachHoverGlow);
        setActivePage(menuButtons.getFirst());
    }


    /**
     * Attaches a continuous glow effect when hovering over the button.
     *
     * @param btn the button to decorate with hover animation
     */
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

    /**
     * Displays the dashboard view.
     */
    @FXML
    public void showDashboard() {
        setActivePage(menuButtons.getFirst());
        nav.loadView("dashboard/dashboard.fxml", controller ->{});
    }

    /**
     * Displays the watchlist view.
     */
    @FXML
    private void showWatchlist() {
        setActivePage(menuButtons.get(1));
        nav.loadView("watchlist/watchlist.fxml", controller -> {});
    }

    /**
     * Displays the portfolio view.
     */
    @FXML
    private void showPortfolio() {
        setActivePage(menuButtons.get(2));
        nav.loadView("portfolio/portfolio.fxml", controller -> {});
    }

    /**
     * Displays the stocks listing view.
     */
    @FXML
    private void showStocks() {
        setActivePage(menuButtons.get(3));
        nav.loadView("stockspage/stocks.fxml", controller -> {});
    }

    /**
     * Displays the simulation view.
     */
    @FXML
    private void showSimulation(){
        setActivePage(menuButtons.get(4));
        nav.loadView("simulation/simulation.fxml", controller -> {});
    }

    /**
     * Displays the user account profile view.
     */
    @FXML
    private void showMyAccount() {
        setActivePage(menuButtons.get(5));
        nav.loadView("useraccounts/userAccountsProfile.fxml", controller -> {});
    }

    /**
     * Updates the visual state to mark the given button as active.
     *
     * @param activePageButton the button to mark as active
     */
    private void setActivePage(Button activePageButton) {
        menuButtons.forEach(btn -> btn.getStyleClass().remove("active"));
        activePageButton.getStyleClass().add("active");
    }

}