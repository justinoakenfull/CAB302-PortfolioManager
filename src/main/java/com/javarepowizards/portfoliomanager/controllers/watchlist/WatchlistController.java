package com.javarepowizards.portfoliomanager.controllers.watchlist;
import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Controller for the Watchlist view.
 * Manages the UI components and interactions for displaying and modifying
 * the user's watchlist, including stock removal, addition, and detail viewing.
 * Retrieves data from the IWatchlistService and binds it to the UI.
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
     * Initializes the controller after its FXML elements have been injected.
     * Retrieves the watchlist service, binds UI properties,
     * sets up the table columns, and loads initial data.
     *
     * @param location the location used to resolve relative paths for the root object, or null if unknown
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

        setupTable();
        loadAll();
    }


    /**
     * Configures the watchlist table by creating columns,
     * adding cell factories, styling, and selection listeners.
     */
    private void setupTable() {
        List<ColumnConfig<WatchlistRow,?>> cols = List.of(
                new ColumnConfig<>("Ticker",    WatchlistRow::shortNameProperty),
                new ColumnConfig<>("Name",      WatchlistRow::displayNameProperty),
                new ColumnConfig<>("Open",      r -> r.openProperty().asObject(),
                        TableCellFactories.numericFactory(2, false)),
                new ColumnConfig<>("Close",     r -> r.closeProperty().asObject(),
                        TableCellFactories.numericFactory(2, false)),
                new ColumnConfig<>("Change",    r -> r.changeProperty().asObject(),
                        TableCellFactories.numericFactory(2, true)),
                new ColumnConfig<>("Change %",  r -> r.changePercentProperty().asObject(),
                        TableCellFactories.numericFactory(2, true)),
                new ColumnConfig<>("Price",     r -> r.priceProperty().asObject(),
                        TableCellFactories.currencyFactory(AU_LOCALE, 2)),
                new ColumnConfig<>("Volume",    r -> r.volumeProperty().asObject(),
                        TableCellFactories.longFactory()),
                new ColumnConfig<>("Remove",    WatchlistRow::removeProperty)
        );

        TableView<WatchlistRow> tv = TableViewFactory.create(cols);
        tv.getStyleClass().add("watchlist-table");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tv.getColumns().forEach(col ->
                col.getStyleClass().add("column-header-background")
        );
        tv.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldRow, newRow) -> onRowSelected(newRow)
        );

        HBox.setHgrow(tv, Priority.ALWAYS);
        VBox.setVgrow(tv, Priority.ALWAYS);
        tableContainer.getChildren().setAll(tv);
        tableView = tv;
    }


    /**
     * Handles row selection in the watchlist table.
     * Triggers loading of the selected stock's short description asynchronously.
     *
     * @param row the selected WatchlistRow, or null if no selection
     */
    private void onRowSelected(WatchlistRow row) {
        if (row == null) {
            snapshotText.setText("No stock selected.");
        } else {
            StockName sym = StockName.fromString(row.shortNameProperty().get());
            toggleProgress();
            snapshotText.setText("Loading description…");

            Thread t = startAIThread(sym);
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Starts a background thread to fetch the short description of a stock.
     *
     * @param sym the StockName representing the selected stock
     * @return a daemon Thread executing the description retrieval task
     */
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
                    snapshotText.setText(descTask.getValue());
                    toggleProgress();
                });
        descTask.setOnFailed(e -> {
            Throwable ex = descTask.getException();

            if (ex != null) {
                return;
            }

            snapshotText.setText(descTask.getValue());
            toggleProgress();
        });

        return new Thread(descTask);
    }

    /**
     * Loads all watchlist data into the UI components: table, pie chart, and balance cards.
     * Sets up removal buttons and applies chart colors.
     *
     * @throws RuntimeException if an I/O or SQL exception occurs while loading data
     */
    private void loadAll() {
        try {
            List<WatchlistRow> rows = watchlistService.getWatchlistRows();
            tableView.setItems(FXCollections.observableArrayList(rows));

            for (WatchlistRow row : rows) {
                Button removeBtn = row.removeProperty().get();
                removeBtn.setOnAction(e -> {
                    try {
                        watchlistService.removeStock(
                                StockName.fromString(row.shortNameProperty().get())
                        );
                        loadAll();
                    } catch (SQLException ex) {
                        throw new RuntimeException("Failed to remove stock", ex);
                    }
                });
            }

            tableView.setItems(FXCollections.observableArrayList(rows));

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    watchlistService.getPortfolioPieData()
            );

            noStocksWarning.setVisible(pieData.isEmpty());

            PieChart chart = new PieChart(pieData);
            chart.setLegendVisible(false);
            chart.setLabelsVisible(false);
            bindChartSize(chart);
            portfolioPieContainer.getChildren().setAll(chart);

            List<Optional<PortfolioEntry>> picks = watchlistService.getBalancePicks();
            List<Text> lbls = List.of(stockOneLabel, stockTwoLabel, stockThreeLabel, stockFourLabel);
            List<Text> vals = List.of(stockOneValue, stockTwoValue, stockThreeValue, stockFourValue);
            for (int i = 0; i < 4; i++) {
                PortfolioEntry e = picks.get(i).orElse(null);
                setCard(lbls.get(i), vals.get(i), e);
            }

            Platform.runLater(() -> applyLabelColors(chart));

        } catch (IOException | SQLException ex) {
            throw new RuntimeException("Failed to load Watchlist UI", ex);
        }
    }


    /**
     * Binds the size properties of the given PieChart to its container's dimensions.
     *
     * @param chart the PieChart to bind
     */
    private void bindChartSize(PieChart chart) {
        chart.minWidthProperty().bind(portfolioPieContainer.widthProperty());
        chart.minHeightProperty().bind(portfolioPieContainer.heightProperty());
        chart.maxWidthProperty().bind(portfolioPieContainer.widthProperty());
        chart.maxHeightProperty().bind(portfolioPieContainer.heightProperty());
    }

    /**
     * Toggles the visibility and size of the progress indicator.
     * Shows it when hidden and hides it when visible.
     */
    private void toggleProgress() {
        boolean visible = !progressIndicator.isVisible();
        progressIndicator.setVisible(visible);
        progressIndicator.setMinHeight(visible ? 100 : 0);
        progressIndicator.setMaxHeight(visible ? 100 : 0);
    }

    /**
     * Opens a dialog allowing the user to add a new stock to their watchlist.
     * If no stocks remain to add, shows an informational alert.
     *
     * @throws RuntimeException if unable to load addable symbols or add a stock due to database errors
     */
    @FXML
    private void onAddStock() {
        try {
            List<StockName> choices = watchlistService.getAddableSymbols();
            String dialogCss = "/com/javarepowizards/portfoliomanager/views/watchlist/dialog.css";
            if (choices.isEmpty()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        "You’ve already added every available stock.");
                info.getDialogPane()
                        .getStylesheets()
                        .add(Objects.requireNonNull(getClass()
                                        .getResource(dialogCss))
                                .toExternalForm());
                info.setTitle("Nothing to Add");
                info.setHeaderText(null);
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
                    loadAll();
                } catch (SQLException ex) {
                    throw new RuntimeException("Failed to add stock", ex);
                }
            });

        } catch (SQLException e) {
            throw new RuntimeException("Unable to load watchlist for adding stocks", e);
        }
    }

    /**
     * Opens a modal window displaying detailed information about the selected stock.
     *
     * @throws RuntimeException if the FXML resource is not found or loading fails,
     *                          or if a database error occurs while initializing modal data
     */
    @FXML
    private void onViewStock() {
        WatchlistRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String resource = "/com/javarepowizards/portfoliomanager/views/watchlist/WatchlistModal.fxml";
        URL fxmlUrl = getClass().getResource(resource);
        if (fxmlUrl == null) {
            throw new IllegalStateException("Cannot find FXML: " + resource);
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            WatchlistModalController modal = loader.getController();

            StockName ticker = StockName.fromString(selected.shortNameProperty().get());

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


    /**
     * Sets the fill color of a Text label based on the provided colour map.
     *
     * @param label the Text node whose fill colour will be set
     * @param colourMap a mapping of stock symbols to Paint colours
     */
    private void setLabelFill(Text label, Map<String, Paint> colourMap) {
        Paint p = colourMap.get(label.getText());
        if (p != null) {
            label.setFill(p);
        }
    }

    /**
     * Applies the colours of the PieChart slices to the corresponding Text labels.
     *
     * @param chart the PieChart whose slice colours are applied
     */
    private void applyLabelColors(PieChart chart) {
        chart.applyCss();
        chart.layout();

        Map<String, Paint> colourMap = new HashMap<>();
        for (PieChart.Data d : chart.getData()) {
            Node n = d.getNode();
            Paint fill = null;
            if (n instanceof Shape) {
                fill = ((Shape)n).getFill();
            } else if (n instanceof Region) {
                var bg = ((Region)n).getBackground();
                if (bg!=null && !bg.getFills().isEmpty()) {
                    fill = bg.getFills().getFirst().getFill();
                }
            }
            colourMap.put(d.getName(), fill);
        }

        setLabelFill(stockOneLabel, colourMap);
        setLabelFill(stockTwoLabel, colourMap);
        setLabelFill(stockThreeLabel, colourMap);
        setLabelFill(stockFourLabel, colourMap);
    }


    /**
     * Populates a card slot with a portfolio entry's symbol and percentage change,
     * or clears the card if no entry is provided.
     *
     * @param labelNode the Text node for the stock symbol
     * @param valueNode the Text node for the percentage change
     * @param entryOpt the PortfolioEntry to display, or null to clear
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
