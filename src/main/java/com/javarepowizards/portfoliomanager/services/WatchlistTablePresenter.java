package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.domain.IStockRepoReadOnly;
import com.javarepowizards.portfoliomanager.domain.IWatchlistReadOnly;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
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

/**
 * Presenter for the watchlist table widget.
 * Builds the TableView columns and keeps it in sync with the service.
 */
public final class WatchlistTablePresenter {

    private final VBox                container;
    private final IWatchlistReadOnly  watchlist;
    private final IStockRepoReadOnly  repo;
    private TableView<WatchlistRow>   table;

    /**
     *
     * @param container where the table is placed
     * @param watchlist provides watchlist data and removal
     * @param repo provides available tickers
     */
    public WatchlistTablePresenter(
            VBox container,
            IWatchlistReadOnly watchlist,
            IStockRepoReadOnly repo)
    {
        this.container = container;
        this.watchlist = watchlist;
        this.repo      = repo;

        buildTable();
        refresh();
    }

    /** Re-queries the watchlist and updates the table rows */
    public void refresh() {
        try {
            Set<String> csvTickers = repo.availableTickers();

            List<WatchlistRow> rows = new ArrayList<>();
            for (IStock s : watchlist.getWatchlist()) {
                if (!csvTickers.contains(s.getTicker()))
                    continue;

                rows.add(new WatchlistRow(s, () -> {}));
            }

            ObservableList<WatchlistRow> model = FXCollections.observableArrayList(rows);
            table.setItems(model);

        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }

   /** Creates the TableView with all columns and styling */
    private void buildTable() {

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

        // build and style table
        table = TableViewFactory.create(cols);
        table.getColumns().forEach(c -> c.getStyleClass().add("column-header-background"));
        table.getStyleClass().add("watchlist-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // allow it to grow within its container
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().setAll(table);
    }
}
