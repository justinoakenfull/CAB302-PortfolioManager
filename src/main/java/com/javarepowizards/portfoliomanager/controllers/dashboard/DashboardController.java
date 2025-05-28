package com.javarepowizards.portfoliomanager.controllers.dashboard;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.domain.IStockRepoReadOnly;
import com.javarepowizards.portfoliomanager.domain.IWatchlistReadOnly;
import com.javarepowizards.portfoliomanager.services.PortfolioChartPresenter;
import com.javarepowizards.portfoliomanager.services.WatchlistTablePresenter;
import com.javarepowizards.portfoliomanager.ui.QuickTips;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Dashboard View.
 * Composes the watchlist table and portfolio overview chart,
 * and starts the QuickTips rotation.
 */
public class DashboardController implements Initializable {

    @FXML
    private VBox  tableContainer;

    @FXML
    private Label quickTipsLabel;

    @FXML
    private Pane  portfolioPieContainer;

    /**
     * Called to initialize the controller after its root element has been completely processed.
     * Sets up the watchlist table presenter, the portfolio chart presenter,
     * and starts the quick tips rotation.
     *
     * @param url the location used to resolve relative paths for the root object, or null if unknown
     * @param rb  the resources used to localize the root object, or null if not specified
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inject and build the watchlist table
        new WatchlistTablePresenter(
                tableContainer,
                AppContext.getService(IWatchlistReadOnly.class),
                AppContext.getService(IStockRepoReadOnly.class));

        // Inject and build the portfolio overview chart
        new PortfolioChartPresenter(
                portfolioPieContainer,
                AppContext.getService(IPortfolioDAO.class));

        // Start the rotating QuickTips
        new QuickTips(quickTipsLabel).start();
    }
}
