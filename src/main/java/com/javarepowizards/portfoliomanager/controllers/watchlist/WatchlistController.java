package com.javarepowizards.portfoliomanager.controllers.watchlist;
import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Controller for the Watchlist view.
 * Manages the retrieval of a user's watchlist from the DAO, displays it in a table,
 * and handles user interactions such as adding or viewing stock details.
 */
public class WatchlistController implements Initializable {

    @FXML private ProgressIndicator progressIndicator;
    @FXML private VBox tableContainer;
    @FXML private TableView<WatchlistRow> tableView;
    @FXML private Text snapshotText;
    @FXML private ScrollPane snapshotScrollPane;
    @FXML private Button viewStockButton;
    @FXML private Pane portfolioPieContainer;
    @FXML private Label noStocksWarning;
    @FXML Text stockOneLabel, stockTwoLabel, stockThreeLabel, stockFourLabel;
    @FXML Text stockOneValue, stockTwoValue, stockThreeValue, stockFourValue;

    private IWatchlistService watchlistService;

    private static final Locale AU_LOCALE = Locale.forLanguageTag("en-AU");

    /**
     * Initializes the controller after its root element has been processed.
     * Sets up DAO instances, determines the current user ID, logs it,
     * and populates the watchlist table.
     *
     * @param location  the location used to resolve relative paths for the root object, or null if unknown
     * @param resources the resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.watchlistService = AppContext.getService(IWatchlistService.class);
        snapshotText.wrappingWidthProperty()
                .bind(snapshotScrollPane.widthProperty().subtract(20));
        snapshotText.textProperty().addListener((obs, old, neo) ->
                snapshotScrollPane.setVvalue(0)
        );

        try {
            refreshTable();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshTable() throws IOException, SQLException {

        List<IStock> stocks = watchlistService.getWatchlist();

        List<WatchlistRow> rows = new ArrayList<>();

        for (IStock stock : stocks) {
            StockName sym = StockName.fromString(stock.getTicker());
            rows.add(new WatchlistRow(stock, () -> {
                try {
                    watchlistService.removeStock(sym);
                    refreshTable();
                } catch (SQLException | IOException ex) {
                    throw new RuntimeException("Failed to remove stock", ex);
                }
            }));
        }

        ObservableList<WatchlistRow> model = FXCollections.observableArrayList(rows);

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
                        TableCellFactories.currencyFactory(AU_LOCALE, 2)),
                new ColumnConfig<>("Volume",
                        r -> r.volumeProperty().asObject(),
                        TableCellFactories.longFactory()),
                new ColumnConfig<>("Remove",
                        WatchlistRow::removeProperty)
        );

        TableView<WatchlistRow> table = TableViewFactory.create(cols);

        table.getStyleClass().add("watchlist-table");
        table.setEffect(tableView.getEffect());
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        tableContainer.getChildren().setAll(table);
        tableView = table;
        table.setItems(model);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        table.getColumns().forEach(col ->
                col.getStyleClass().add("column-header-background")
        );

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            if (newRow == null) {
                setSnapshotText("No stock selected.");
            } else {
                StockName sym = StockName.fromString(newRow.shortNameProperty().get());
                toggleProgress();
                setSnapshotText("Loading description…");

                Thread t = startAIThread(sym);
                t.setDaemon(true);
                t.start();
            }
        });

        buildPortfolioPieChart();
    }

    private Thread startAIThread(StockName sym) {
        Task<String> descTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return watchlistService.getShortDescription(sym);
            }
        };

        descTask.setOnSucceeded(
                e ->
                {
                    setSnapshotText(descTask.getValue());
                    toggleProgress();
                });
        descTask.setOnFailed(e -> {
            Throwable ex = descTask.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
            setSnapshotText(descTask.getValue());
            toggleProgress();
        });

        return new Thread(descTask);
    }

    private void toggleProgress() {
        if (progressIndicator.isVisible()) {
            progressIndicator.setVisible(false);
            progressIndicator.setMinHeight(0);
            progressIndicator.setMaxHeight(0);
        } else {
            progressIndicator.setVisible(true);
            progressIndicator.setMinHeight(100);
            progressIndicator.setMaxHeight(100);
        }

    }

    private void setSnapshotText(String description) {
        snapshotText.setText(description);
    }


    @FXML
    private void onAddStock() {
        try {
            List<StockName> choices = watchlistService.getAddableSymbols();
            String dialogCss = "/com/javarepowizards/portfoliomanager/views/watchlist/dialog.css";
            if (choices.isEmpty()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        "You’ve already added every available stock.");
                // load your dialog.css into this alert’s dialog pane
                info.getDialogPane()
                        .getStylesheets()
                        .add(Objects.requireNonNull(getClass()
                                        .getResource(dialogCss))
                                .toExternalForm());
                info.setTitle("Nothing to Add");
                info.setHeaderText(null);  // or whatever header you like
                info.showAndWait();
                return;
            }

            ChoiceDialog<StockName> dlg =
                    new ChoiceDialog<>(choices.getFirst(), choices);
            dlg.getDialogPane()
                    .getStylesheets()
                    .add(Objects.requireNonNull(getClass().getResource(dialogCss)).toExternalForm());
            dlg.setTitle("Add to Watchlist");
            dlg.setHeaderText("Select a stock to watch");
            dlg.setContentText("Symbol:");
            dlg.showAndWait().ifPresent(sym -> {
                try {
                    watchlistService.addStock(sym);
                    refreshTable();
                } catch (SQLException | IOException ex) {
                    throw new RuntimeException("Failed to add stock", ex);
                }
            });

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Unable to load watchlist for adding stocks", e);
        }
    }

    @FXML
    private void onViewStock() {
        WatchlistRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        StockName ticker = StockName.fromString(selected.shortNameProperty().get());

        String resource = "/com/javarepowizards/portfoliomanager/views/watchlist/WatchlistModal.fxml";
        URL fxmlUrl = getClass().getResource(resource);
        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find FXML: " + resource);
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            WatchlistModalController modal = loader.getController();
            modal.initData(ticker);

            Stage dialog = new Stage();
            dialog.initOwner(viewStockButton.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(ticker + " Details");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to open details modal", e);
        }
    }

    @FXML
    private void buildPortfolioPieChart(){
        IPortfolioDAO portfolioDAO = AppContext.getService(IPortfolioDAO.class);
        List<PortfolioEntry> entries = portfolioDAO.getHoldings();

        if (entries.isEmpty()) {
            noStocksWarning.setVisible(true);
            noStocksWarning.setMinHeight(100);
            noStocksWarning.setMaxHeight(100);
            return;
        } else
        {
            noStocksWarning.setVisible(false);
            noStocksWarning.setMinHeight(0);
            noStocksWarning.setMaxHeight(0);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (PortfolioEntry entry : entries) {
            double value = entry.getMarketValue();
            pieData.add(new PieChart.Data(entry.getStock().getSymbol(),value));
        }

        PieChart chart = new PieChart(pieData);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);


        chart.minWidthProperty().bind(
                Bindings.when(portfolioPieContainer.widthProperty().lessThan(4000))
                        .then(portfolioPieContainer.widthProperty())
                        .otherwise(4000));
        chart.minHeightProperty().bind(
                Bindings.when(portfolioPieContainer.heightProperty().lessThan(4000))
                        .then(portfolioPieContainer.heightProperty())
                        .otherwise(4000));
        chart.maxWidthProperty().bind(
                Bindings.when(portfolioPieContainer.widthProperty().lessThan(4000))
                        .then(portfolioPieContainer.widthProperty())
                        .otherwise(4000));
        chart.maxHeightProperty().bind(
                Bindings.when(portfolioPieContainer.heightProperty().lessThan(4000))
                        .then(portfolioPieContainer.heightProperty())
                        .otherwise(4000));
        portfolioPieContainer.getChildren().setAll(chart);

        updateBalanceCard(entries);
    }

    /**
     * Refreshes the four balance-card entries without repeating any stock.
     * Slot 1: highest positive change %
     * Slot 2: change % closest to zero
     * Slot 3: smallest non-zero change % by absolute value
     * Slot 4: largest negative change %
     *
     * @param entries the current portfolio entries
     */
    public void updateBalanceCard(List<PortfolioEntry> entries) {

        List<PortfolioEntry> remaining = new ArrayList<>(entries);
        List<Optional<PortfolioEntry>> picks = new ArrayList<>(4);

        Optional<PortfolioEntry> highestPos = remaining.stream()
                .filter(entry -> watchlistService.computeChangePercent(entry) > 0)
                .max(Comparator.comparingDouble(watchlistService::computeChangePercent));
        highestPos.ifPresent(remaining::remove);
        picks.add(highestPos);

        Optional<PortfolioEntry> closestZero = remaining.stream()
                .min(Comparator.comparingDouble(entry -> Math.abs(watchlistService.computeChangePercent(entry))));
        closestZero.ifPresent(remaining::remove);
        picks.add(closestZero);

        Optional<PortfolioEntry> smallestNonZero = remaining.stream()
                .filter(entry -> watchlistService.computeChangePercent(entry) != 0)
                .min(Comparator.comparingDouble(entry -> Math.abs(watchlistService.computeChangePercent(entry))));
        smallestNonZero.ifPresent(remaining::remove);
        picks.add(smallestNonZero);

        Optional<PortfolioEntry> largestNeg = remaining.stream()
                .filter(entry -> watchlistService.computeChangePercent(entry) < 0)
                .min(Comparator.comparingDouble(watchlistService::computeChangePercent));
        picks.add(largestNeg);

        List<Text> labels = List.of(stockOneLabel, stockTwoLabel, stockThreeLabel, stockFourLabel);
        List<Text> values = List.of(stockOneValue, stockTwoValue, stockThreeValue, stockFourValue);

        for (int i = 0; i < picks.size(); i++) {
            setCard(labels.get(i), values.get(i), picks.get(i).orElse(null));
        }
    }



    /**
     * Populates a single card slot, or shows “—” if no entry available.
     *
     * @param labelNode the ticker Text node
     * @param valueNode the percent-change Text node
     * @param entryOpt  the optional portfolio entry
     */
    private void setCard(Text labelNode, Text valueNode, PortfolioEntry entryOpt) {
        if (entryOpt != null) {
            labelNode.setText(entryOpt.getStock().getSymbol());
            valueNode.setText(watchlistService.formatPercent(watchlistService.computeChangePercent(entryOpt)));
        } else {
            labelNode.setText("");
            valueNode.setText("");
        }
    }


}
