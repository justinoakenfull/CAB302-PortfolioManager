package com.javarepowizards.portfoliomanager.controllers.dashboard;

import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import com.javarepowizards.portfoliomanager.ui.ColumnConfig;
import com.javarepowizards.portfoliomanager.ui.TableCellFactories;
import com.javarepowizards.portfoliomanager.ui.TableViewFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/** Build watchlist-table columns and keep in sync with user actions */
final class WatchlistTablePresenter {

    private final VBox               container;
    private final IWatchlistService  service;
    private final StockRepository    repo;

    private TableView<WatchlistRow>  table;

    WatchlistTablePresenter(VBox container,
                            IWatchlistService service,
                            StockRepository repo)
    {
        this.container = container;
        this.service   = service;
        this.repo      = repo;

        buildTable();     // define all columns and styling
        refresh();        // populate table with current data
    }

    /** Re-query watchlist and update table rows */
    void refresh() {
        try {
            Set<String> csvTickers = repo.availableTickers();
            List<WatchlistRow> rows = new ArrayList<>();

            for (IStock stock : service.getWatchlist()) {
                if (!csvTickers.contains(stock.getTicker())) continue;

                rows.add(new WatchlistRow(
                        stock,
                        () -> {
                            try { service.removeStock(stock); }
                            catch (SQLException ignored) {}
                        }));
            }

            ObservableList<WatchlistRow> model =
                    FXCollections.observableArrayList(rows);
            table.setItems(model);

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    /* build TableView */
    private void buildTable() {

        /* Column definitions */
        List<ColumnConfig<WatchlistRow,?>> cols = List.of(
                new ColumnConfig<>("Ticker",   WatchlistRow::shortNameProperty),
                new ColumnConfig<>("Name",     WatchlistRow::displayNameProperty),
                new ColumnConfig<>("Open",     r -> r.openProperty().asObject(),
                        TableCellFactories.numericFactory(2,false)),
                new ColumnConfig<>("Close",    r -> r.closeProperty().asObject(),
                        TableCellFactories.numericFactory(2,false)),
                new ColumnConfig<>("Change",   r -> r.changeProperty().asObject(),
                        TableCellFactories.numericFactory(2,true)),
                new ColumnConfig<>("Change %", r -> r.changePercentProperty().asObject(),
                        TableCellFactories.numericFactory(2,true)),
                new ColumnConfig<>("Price",    r -> r.priceProperty().asObject(),
                        TableCellFactories.currencyFactory(new Locale("en","AU"),2)),
                new ColumnConfig<>("Volume",   r -> r.volumeProperty().asObject(),
                        TableCellFactories.longFactory())
        );


        table = TableViewFactory.create(cols);

        /*Dark header row */
        table.getColumns().forEach(col -> col.getStyleClass().add("column-header-background"));

        table.getStyleClass().add("watchlist-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        /* let the table grow with its parent VBox */
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        container.getChildren().setAll(table);
    }

    /* ------------------------------------------------------------ */


}
