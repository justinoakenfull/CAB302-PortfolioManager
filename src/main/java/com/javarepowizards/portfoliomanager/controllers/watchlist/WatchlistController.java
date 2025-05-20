package com.javarepowizards.portfoliomanager.controllers.watchlist;
import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    @FXML private VBox      tableContainer;
    @FXML private TableView<WatchlistRow> tableView;
    @FXML private Text snapshotText;
    @FXML private ScrollPane snapshotScrollPane;
    @FXML private Button    viewStockButton;

    private IWatchlistService watchlistService;

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
                        TableCellFactories.currencyFactory(new Locale("en","AU"), 2)),
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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().forEach(col ->
                col.getStyleClass().add("column-header-background")
        );

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, newRow) -> {
            if (newRow == null) {
                setSnapshotText("No stock selected.");
            } else {
                StockName sym = StockName.fromString(newRow.shortNameProperty().get());

                setSnapshotText("Loading description…");

                Thread t = startAIThread(sym);
                t.setDaemon(true);
                t.start();
            }
        });
    }

    private Thread startAIThread(StockName sym) {
        Task<String> descTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return watchlistService.getShortDescription(sym);
            }
        };

        descTask.setOnSucceeded(e -> setSnapshotText(descTask.getValue()));descTask.setOnFailed(e -> {
            Throwable ex = descTask.getException();
            String msg = "Failed to load description";
            if (ex != null) {
                msg += ": " + ex.getClass().getSimpleName()
                        + (ex.getMessage() != null ? " – " + ex.getMessage() : "");
                //ex.printStackTrace();
            }
            setSnapshotText(msg);
        });

        return new Thread(descTask);
    }

    private void setSnapshotText(String description) {
        snapshotText.setText(description);
    }


    @FXML
    private void onAddStock() {
        try {
            List<StockName> choices = watchlistService.getAddableSymbols();
            if (choices.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "You’ve already added every available stock.")
                        .showAndWait();
                return;
            }

            ChoiceDialog<StockName> dlg =
                    new ChoiceDialog<>(choices.getFirst(), choices);
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
}
