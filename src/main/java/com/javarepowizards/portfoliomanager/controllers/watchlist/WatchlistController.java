package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class WatchlistController implements Initializable {

    public DropShadow dropShadow;
    @FXML private VBox      tableContainer;
    @FXML private TableView<WatchlistRow> tableView;
    @FXML private Button    addStockButton;

    private IWatchlistDAO watchlistDAO;
    private StockRepository repo;
    private int currentUserId = 1; // TODO: set this on login

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        watchlistDAO = AppContext.getService(IWatchlistDAO.class);
        repo = AppContext.getService(StockRepository.class);

        try {
            refreshTable();
        } catch (IOException | SQLException e) {
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
                        TableCellFactories.longFactory()),
                new ColumnConfig<>("Remove",
                        WatchlistRow::removeProperty)
        );


        TableView<WatchlistRow> table = TableViewFactory.create(cols);

        // 1) copy over the style class from FXML
        table.getStyleClass().add("watchlist-table");

        // 2) re-apply the drop shadow (if you want it)
        table.setEffect(tableView.getEffect());

        // 3) preserve the layout constraints
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        // 4) swap it in
        tableContainer.getChildren().setAll(table);
        tableView = table;
        table.setItems(model);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getColumns().forEach(col ->
                col.getStyleClass().add("column-header-background")
        );

    }


    @FXML
    private void onAddStock() {
        ChoiceDialog<StockName> dlg = new ChoiceDialog<>(StockName.values()[0], List.of(StockName.values()));
        dlg.setTitle("Add to Watchlist");
        dlg.setHeaderText("Select a stock to watch");
        dlg.setContentText("Symbol:");
        dlg.showAndWait().ifPresent(sym -> {
            try {
                watchlistDAO.addForUser(currentUserId, sym);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            try {
                refreshTable();
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
