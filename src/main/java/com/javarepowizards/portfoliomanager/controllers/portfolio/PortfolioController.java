package com.javarepowizards.portfoliomanager.controllers.portfolio;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.session.Session;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import com.javarepowizards.portfoliomanager.services.watchlist.IWatchlistService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Portfolio view.
 * Manages the display of the user's holdings, pie chart distribution,
 * and stock information descriptions.
 */
public class PortfolioController implements Initializable {

    /** Donut chart showing holdings distribution */
    @FXML private PieChart pieChart;

    /** Displays total portfolio value */
    @FXML private Text totalValueText;

    /** Placeholder for overall change percentage */
    @FXML private Text changePctText;

    /** Table showing all portfolio holdings */
    @FXML private TableView<PortfolioEntry> portfolioTable;

    /** Column for stock names */
    @FXML private TableColumn<PortfolioEntry, String> stockCol;

    /** Column showing each stock's percentage of portfolio */
    @FXML private TableColumn<PortfolioEntry, String> changeCol;

    /** Column showing market value of each stock holding */
    @FXML private TableColumn<PortfolioEntry, Number> balanceCol;

    /** Column containing the "Sell" buttons */
    @FXML private TableColumn<PortfolioEntry, Void> sellCol;

    /** Text area displaying the selected stock's description */
    @FXML private TextArea stockInfoText;

    /** Portfolio data access object */
    private IPortfolioDAO portfolioDAO;

    /** Currently logged-in user's ID */
    private int currentUserId;

    /** Watchlist service used to retrieve stock descriptions */
    private IWatchlistService watchlistService;

    /**
     * Initializes the controller.
     * Sets up table columns, fetches data, and configures row selection listeners.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        portfolioDAO = AppContext.getService(IPortfolioDAO.class);
        currentUserId = Session.getCurrentUser().getUserId();
        watchlistService = AppContext.getService(IWatchlistService.class);

        setupSellColumn();
        setupTableColumns();
        refreshPortfolio();

        portfolioTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadStockDescription(newVal);
            } else {
                stockInfoText.setText("No stock selected.");
            }
        });

        setupPlaceholderText(portfolioTable);
    }

    private static void setupPlaceholderText(TableView<PortfolioEntry> portfolioTable) {
        var placeholder = new Label("Go to Stocks Page to Begin Building Your Portfolio.");
        placeholder.getStyleClass().add("placeholder");
        portfolioTable.setPlaceholder(placeholder);
    }

    /**
     * Loads the selected stock's description from the watchlist service.
     * Executes the fetch in a background thread to avoid blocking the UI.
     *
     * @param entry The portfolio entry selected by the user
     */
    private void loadStockDescription(PortfolioEntry entry) {
        StockName stock = entry.getStock();
        stockInfoText.setText("Loading...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return watchlistService.getShortDescription(stock);
            }
        };

        task.setOnSucceeded(e -> stockInfoText.setText(task.getValue()));
        task.setOnFailed(e -> stockInfoText.setText("Failed to load description."));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    /**
     * Configures each TableColumn’s cell factory.
     */
    private void setupSellColumn() {
        sellCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Sell");


            {
                btn.getStyleClass().add("sell-button");
                btn.setOnAction(e -> {
                    PortfolioEntry entry = getTableView().getItems().get(getIndex());
                    handleSell(entry);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void handleSell(PortfolioEntry entry) {
        try {
            portfolioDAO.sellHolding(currentUserId, entry.getStock());
            refreshPortfolio();  // Keep this here since it's UI-related
        } catch (Exception e) {
            /* Consume the exception */
        }
    }


    private void setupTableColumns() {
        // show stock display name (e.g. "BHP Group Ltd")
        stockCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStock().getDisplayName())
        );

        // compute each holding's percentage of the total portfolio
        changeCol.setCellValueFactory(c -> {
            PortfolioEntry entry = c.getValue();

            // sum up marketValue for all entries in this table
            double total = c.getTableView()
                    .getItems()
                    .stream()
                    .mapToDouble(PortfolioEntry::getMarketValue)
                    .sum();

            // avoid division-by-zero
            double pctOfTotal = total > 0
                    ? entry.getMarketValue() / total * 100
                    : 0.0;

            return new SimpleStringProperty(String.format("%.2f%%", pctOfTotal));
        });

        // show each holding’s market value formatted as currency
        balanceCol.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getMarketValue())
        );
        balanceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", val.doubleValue()));
                }
            }
        });
    }

    /**
     * Fetches the user’s holdings from the DB, rebuilds the pie chart & table.
     */
    private void refreshPortfolio() {
        List<PortfolioEntry> holdings;
        try {
            holdings = portfolioDAO.getHoldingsForUser(currentUserId);
        } catch (Exception e) {
            return;  // bail out on error
        }

        // rebuild pie slices by actual market value
        var data = pieChart.getData();
        data.clear();
        for (PortfolioEntry entry : holdings) {
            data.add(new PieChart.Data(
                    entry.getStock().getDisplayName(),
                    entry.getMarketValue()
            ));
        }

        // after nodes are created, attach tooltips + hover highlights + scaling
        Platform.runLater(() -> {
            for (PieChart.Data slice : data) {
                Node node = slice.getNode();
                node.setPickOnBounds(true);  // entire slice is clickable

                // show exact dollar value on hover
                Tooltip.install(node, new Tooltip(
                        slice.getName() + ": " + String.format("$%.2f", slice.getPieValue())
                ));

                //  glow effect on hover
                node.setOnMouseEntered(e -> {
                    node.setScaleX(1.1);
                    node.setScaleY(1.1);
                });
                node.setOnMouseExited(e -> {
                    node.setScaleX(1.0);
                    node.setScaleY(1.0);
                });
            }

            // scale the whole chart +10% per stock, max +50%
            double factor = 1.0 + Math.min(holdings.size(), 5) * 0.1;
            pieChart.setScaleX(factor);
            pieChart.setScaleY(factor);
        });

        // update summary texts
        double total = holdings.stream()
                .mapToDouble(PortfolioEntry::getMarketValue)
                .sum();
        totalValueText.setText(String.format("$%.2f", total));
        changePctText.setText("0.00%");  // could calculate real change here

        //populate bottom table
        portfolioTable.setItems(FXCollections.observableArrayList(holdings));
    }
}
