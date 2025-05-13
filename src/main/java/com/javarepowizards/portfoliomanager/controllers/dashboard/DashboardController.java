package com.javarepowizards.portfoliomanager.controllers.dashboard;


import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.ui.QuickTips;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.*;
import java.util.Locale;
import javafx.util.Callback;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Pane;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.services.PortfolioInitializer;
import java.time.LocalDate;


/**
 * Controller class for Dashboard view.
 * Manages the quick tips section and watchlist table display
 */
public class DashboardController {

    // Label that displays rotating investment tips
    @FXML private  Label quickTipsLabel;
    @FXML private Pane portfolioPieContainer;

    // Tableview and columns for the watchlist section
    @FXML private TableView<WatchlistRow> watchlistTable;
    @FXML private TableColumn<WatchlistRow, Double> changeColumn;
    @FXML private TableColumn<WatchlistRow, String> tickerColumn;
    @FXML private TableColumn<WatchlistRow, String> nameColumn;
    @FXML private TableColumn<WatchlistRow, Double> openColumn;
    @FXML private TableColumn<WatchlistRow, Double> closeColumn;
    @FXML private TableColumn<WatchlistRow, Double> changePercentColumn;
    @FXML private TableColumn<WatchlistRow, Double> priceColumn;
    @FXML private TableColumn<WatchlistRow, Long> volumeColumn;
    @FXML private TableColumn<WatchlistRow, Button> removeColumn;

    private final IWatchlistDAO watchlistDAO = AppContext.getService(IWatchlistDAO.class);
    private final StockRepository repo = AppContext.getService(StockRepository.class);
    private final int currentUserId = 1;
    private final StockDAO stockDAO = StockDAO.getInstance();

    /**
     * Called automatically when the FXML is loaded
     * Initialises quick tips and sets dynamic widths for table columns
     */
    @FXML
    public void initialize() {
        //Start rotating investment tips
        new QuickTips(quickTipsLabel).start();


        // Bind column widths to resize proportionately with the table
        bindColumnWidths();
        refreshWatchlist();
        watchlistDAO.addListener(this :: refreshWatchlist);
        buildPortfolioPieChart();

    }

    /**
     * Dynamically binds each table's column's width to a percentage
     * of the total table width for a responsive layout
     */
    private void bindColumnWidths() {
        tickerColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.11));
        nameColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.15));
        openColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
        closeColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
        changePercentColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
        priceColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.10));
        volumeColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.12));
        removeColumn.prefWidthProperty().bind(watchlistTable.widthProperty().multiply(0.12));


        tickerColumn.setCellValueFactory(c -> c.getValue().shortNameProperty());
        nameColumn.setCellValueFactory(c -> c.getValue().displayNameProperty());

        openColumn.setCellValueFactory(c -> c.getValue().openProperty().asObject());
        closeColumn.setCellValueFactory(c -> c.getValue().closeProperty().asObject());
        changeColumn.setCellValueFactory(c -> c.getValue().changeProperty().asObject());
        changePercentColumn.setCellValueFactory(c -> c.getValue().changePercentProperty().asObject());
        priceColumn.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        volumeColumn.setCellValueFactory(c -> c.getValue().volumeProperty().asObject());
        removeColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().removeProperty().get()));

        openColumn.setCellFactory(TableCellFactories.numericFactory(2, false));
        closeColumn.setCellFactory(TableCellFactories.numericFactory(2, false));
        changeColumn.setCellFactory(TableCellFactories.numericFactory(2, true));
        changePercentColumn.setCellFactory(TableCellFactories.numericFactory(2, true));
        priceColumn.setCellFactory(TableCellFactories.currencyFactory(new Locale ("en", "AU"), 2));
    }


    private void refreshWatchlist() {
        try {
            List<StockName> symbols = watchlistDAO.listForUser(currentUserId);
            List<WatchlistRow> rows = new ArrayList<>();
            Set<String> available = repo.availableTickers();

            for (StockName sym : symbols) {
                if (!available.contains(sym.getSymbol())) continue;
                IStock stock = repo.getByTicker(sym.getSymbol());


                rows.add(new WatchlistRow(stock, () -> {
                    try {watchlistDAO.removeForUser(currentUserId,sym); }
                    catch (SQLException ignored) {}

                }));

            }
            watchlistTable.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildPortfolioPieChart(){
        StockDAO stockDAO = StockDAO.getInstance();
        PortfolioDAO portfolioDAO = PortfolioInitializer.createDummyPortfolio(stockDAO, LocalDate.of(2023, 12, 29));
        List<PortfolioEntry> entries = portfolioDAO.getHoldings();

        double totalValue = entries.stream()
                .mapToDouble(PortfolioEntry::getMarketValue)
                .sum();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (PortfolioEntry entry : entries) {
            double value = entry.getMarketValue();
            pieData.add(new PieChart.Data(entry.getStock().getSymbol(),value));
        }

        PieChart chart = new PieChart(pieData);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);

        chart.minWidthProperty().bind(portfolioPieContainer.widthProperty());
        chart.minHeightProperty().bind(portfolioPieContainer.heightProperty());
        chart.maxWidthProperty().bind(portfolioPieContainer.widthProperty());
        chart.maxHeightProperty().bind(portfolioPieContainer.heightProperty());

        portfolioPieContainer.getChildren().setAll(chart);
    }
}


