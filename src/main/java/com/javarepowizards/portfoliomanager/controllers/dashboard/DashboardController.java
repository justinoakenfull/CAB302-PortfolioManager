package com.javarepowizards.portfoliomanager.controllers.dashboard;

import com.javarepowizards.portfoliomanager.AppContext;

import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import com.javarepowizards.portfoliomanager.ui.QuickTips;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
public class DashboardController implements Initializable {

    /* FXML-injected nodes */
    @FXML private VBox           tableContainer;
    @FXML private Label          quickTipsLabel;
    @FXML private Pane           portfolioPieContainer;

    /* presenter classes*/
    private WatchlistTablePresenter watchlistPresenter;
    private PortfolioChartPresenter portfolioPresenter;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        /* Create and inject the watch-list table */
        watchlistPresenter = new WatchlistTablePresenter(
                tableContainer,
                AppContext.getService(IWatchlistService.class),
                AppContext.getService(StockRepository.class));

        /* Create and inject the portfolio pie-chart */
        portfolioPresenter = new PortfolioChartPresenter(
                portfolioPieContainer,
                AppContext.getService(IPortfolioDAO.class));

        /* Begin quick-tips fade-in/out loop */
        new QuickTips(quickTipsLabel).start();
    }
}
