package com.javarepowizards.portfoliomanager.controllers.dashboard;


import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.QuickTips;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.Locale;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.services.PortfolioInitializer;
import java.time.LocalDate;


/**
 * Controller class for Dashboard view.
 * Manages the quick tips section and watchlist table display
 */
public class DashboardController implements Initializable {

    @FXML private VBox tableContainer;
    // Label that displays rotating investment tips
    @FXML private  Label quickTipsLabel;
    @FXML private Pane portfolioPieContainer;

    // Tableview and columns for the watchlist section
    @FXML private TableView<WatchlistRow> watchlistTable;


    private IWatchlistDAO watchlistDAO;
    private StockRepository repo;
    private int currentUserId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.watchlistDAO = AppContext.getService(IWatchlistDAO.class);
        this.repo = AppContext.getService(StockRepository.class);
        IUserDAO userDAO = AppContext.getService(IUserDAO.class);
        currentUserId = userDAO.getCurrentUser().isPresent() ? userDAO.getCurrentUser().get().getUserId() : 1;
        buildPortfolioPieChart();
        //Start rotating investment tips
        new QuickTips(quickTipsLabel).start();
        try {
            refreshTable();
            
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshTable() throws IOException, SQLException {

        List<StockName> symbols = watchlistDAO.listForUser(currentUserId);
        List<WatchlistRow> rows = new ArrayList<>();

        Set<String> available = repo.availableTickers();

        for (StockName sym : symbols) {
            String ticker = sym.getSymbol();
            if (!available.contains(ticker)) {
                System.err.println("No CSV history for " + ticker + ", skipping");
                continue;
            }
            IStock stock = repo.getByTicker(ticker);
            rows.add(new WatchlistRow(stock, () -> {
                try {
                    watchlistDAO.removeForUser(currentUserId, sym);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    refreshTable();
                } catch (IOException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        ObservableList<WatchlistRow> model = FXCollections.observableArrayList(rows);
        // 2) describe columns
        List<ColumnConfig<WatchlistRow,?>> cols = List.of(
                new ColumnConfig<>("Ticker",
                        WatchlistRow::shortNameProperty),
                new ColumnConfig<>("Name",
                        WatchlistRow::displayNameProperty),
                new ColumnConfig<>("Open",
                        r -> r.openProperty().asObject(),
                        TableCellFactories.numericFactory(2,false)),
                new ColumnConfig<>("Close",
                        r -> r.closeProperty().asObject(),
                        TableCellFactories.numericFactory(2,false)),
                new ColumnConfig<>("Change",
                        r -> r.changeProperty().asObject(),
                        TableCellFactories.numericFactory(2,true)),
                new ColumnConfig<>("Change %",
                        r -> r.changePercentProperty().asObject(),
                        TableCellFactories.numericFactory(2,true)),
                new ColumnConfig<>("Price",
                        r -> r.priceProperty().asObject(),
                        TableCellFactories.currencyFactory(new Locale("en","AU"), 2)),
                new ColumnConfig<>("Volume",
                        r -> r.volumeProperty().asObject(),
                        TableCellFactories.longFactory())
        );


        TableView<WatchlistRow> table = TableViewFactory.create(cols);


        table.getStyleClass().add("watchlist-table");

        table.setEffect(watchlistTable.getEffect());

        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer.getChildren().setAll(table);
        watchlistTable = table;
        table.setItems(model);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().forEach(col ->
                col.getStyleClass().add("column-header-background")
        );
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


