package com.javarepowizards.portfoliomanager.controllers.dashboard;


import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.Session;
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
import java.util.stream.Collectors;
import java.io.IOException;

/**
 * Controller class for Dashboard view.
 * Manages the quick tips section and watchlist table display
 */
public class DashboardController {

    // Label that displays rotating investment tips
    @FXML
    private Label quickTipsLabel;
    @FXML
    private Pane portfolioPieContainer;

    // Tableview and columns for the watchlist section
    @FXML
    private TableView<WatchlistRow> watchlistTable;
    @FXML
    private TableColumn<WatchlistRow, Double> changeColumn;
    @FXML
    private TableColumn<WatchlistRow, String> tickerColumn;
    @FXML
    private TableColumn<WatchlistRow, String> nameColumn;
    @FXML
    private TableColumn<WatchlistRow, Double> openColumn;
    @FXML
    private TableColumn<WatchlistRow, Double> closeColumn;
    @FXML
    private TableColumn<WatchlistRow, Double> changePercentColumn;
    @FXML
    private TableColumn<WatchlistRow, Double> priceColumn;
    @FXML
    private TableColumn<WatchlistRow, Long> volumeColumn;


    private final IWatchlistDAO watchlistDAO = AppContext.getService(IWatchlistDAO.class);

   // private final IPortfolioDAO portfolioDAO = AppContext.getService(IPortfolioDAO.class);
    private final StockRepository repo = AppContext.getService(StockRepository.class);


    /**
     * Called automatically when the FXML is loaded
     * Initialises quick tips and sets dynamic widths for table columns
     */
    @FXML
    public void initialize() {
        //Start rotating investment tips
        new QuickTips(quickTipsLabel).start();


        // Bind column widths to resize proportionately with the table
        watchlistTable.getColumns().forEach(col -> {
            col.setResizable(false);
            col.setReorderable(false);
        });

        watchlistTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        bindColumnWidths();
        configureTableColumns();

        // Whenever the DAO signals a change, reload
        watchlistDAO.addListener(this::loadWatchlist);
        loadWatchlist();


        // Build portfolio pie-chart from real DAO
        buildPortfolioPieChart();

    }

    private void bindColumnWidths() {
        var total = watchlistTable.widthProperty();
        double pct = 1.0 / 8;

        tickerColumn.prefWidthProperty().bind(total.multiply(pct));
        nameColumn.prefWidthProperty().bind(total.multiply(pct));
        openColumn.prefWidthProperty().bind(total.multiply(pct));
        closeColumn.prefWidthProperty().bind(total.multiply(pct));
        changePercentColumn.prefWidthProperty().bind(total.multiply(pct));
        priceColumn.prefWidthProperty().bind(total.multiply(pct));
        volumeColumn.prefWidthProperty().bind(total.multiply(pct));
    }

    private void configureTableColumns() {

        tickerColumn.setCellValueFactory(c -> c.getValue().shortNameProperty());
        nameColumn.setCellValueFactory(c -> c.getValue().displayNameProperty());
        openColumn.setCellValueFactory(c -> c.getValue().openProperty().asObject());
        closeColumn.setCellValueFactory(c -> c.getValue().closeProperty().asObject());
        changeColumn.setCellValueFactory(c -> c.getValue().changeProperty().asObject());
        changePercentColumn.setCellValueFactory(c -> c.getValue().changePercentProperty().asObject());
        priceColumn.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        volumeColumn.setCellValueFactory(c -> c.getValue().volumeProperty().asObject());


        // apply two-decimal place rounding & currency factories
        openColumn.setCellFactory(TableCellFactories.numericFactory(2, false));
        closeColumn.setCellFactory(TableCellFactories.numericFactory(2, false));
        changeColumn.setCellFactory(TableCellFactories.numericFactory(2, true));
        changePercentColumn.setCellFactory(TableCellFactories.numericFactory(2, true));
        priceColumn.setCellFactory(TableCellFactories.currencyFactory(new Locale("en", "AU"), 2));
    }

    private void loadWatchlist() {
        int userId = Session.getCurrentUser().getUserId();
        List<WatchlistRow> rows = new ArrayList<>();

        try {
            // Fetch all symbols in user’s watchlist from database
            List<StockName> symbols = watchlistDAO.listForUser(userId);

            for (StockName symbol : symbols) {
                // skip if no CSV history
                if (!repo.availableTickers().contains(symbol.getSymbol())) {
                    continue;
                }

                IStock stock;
                try {
                    // may throw IOException
                    stock = repo.getByTicker(symbol.getSymbol());
                } catch (IOException ioe) {
                    // log & skip bad entries
                    ioe.printStackTrace();
                    continue;
                }

                // wrap each IStock in  WatchlistRow
                rows.add(new WatchlistRow(
                        stock,
                        () -> {
                        }
                ));
            }

            // add stocks to the TableView
            watchlistTable.setItems(FXCollections.observableArrayList(rows));

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            // TODO: show JavaFx Alert
        }
    }


    private void buildPortfolioPieChart() {
        int userId = Session.getCurrentUser().getUserId();
        List<StockName> symbols;
        try {
            symbols = watchlistDAO.listForUser(userId);
        } catch (SQLException e) {
            // handle error - think need to come back to
            return;
        }

        // Count occurrences of each symbol
        Map<String, Long> counts = symbols.stream()
                .map(StockName::getSymbol)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        counts.forEach((sym, cnt) ->
                data.add(new PieChart.Data(sym, cnt))
        );

        PieChart chart = new PieChart(data);
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);

        // stretch to fill the Pane
        chart.minWidthProperty().bind(portfolioPieContainer.widthProperty());
        chart.minHeightProperty().bind(portfolioPieContainer.heightProperty());
        chart.maxWidthProperty().bind(portfolioPieContainer.widthProperty());
        chart.maxHeightProperty().bind(portfolioPieContainer.heightProperty());

        portfolioPieContainer.getChildren().setAll(chart);
    }
}










