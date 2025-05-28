package com.javarepowizards.portfoliomanager.models;

import com.javarepowizards.portfoliomanager.ui.table.TableRow.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.watchlist.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Manages the observable watchlist for a single user.
 * The rows list is live and updates any bound TableView automatically.
 */
public class Watchlist {
    private final StockRepository   repo;
    private final IWatchlistDAO dao;
    private final int               userId;
    private final ObservableList<WatchlistRow> rows = FXCollections.observableArrayList();

    /**
     * Creates a Watchlist model for the given user.
     * Immediately loads the current watchlist entries.
     *
     * @param repo   repository for retrieving stock details
     * @param dao    DAO for persisting and querying the user's watchlist
     * @param userId unique identifier of the user whose watchlist is managed
     */
    public Watchlist(StockRepository repo,
                          IWatchlistDAO dao,
                          int userId) {
        this.repo   = repo;
        this.dao    = dao;
        this.userId = userId;
        refresh();
    }

    /**
     * Returns the live list of WatchlistRow objects.
     * Use this in TableView#setItems to bind the view to this model.
     *
     * @return an observable list of WatchlistRow entries
     */
    public ObservableList<WatchlistRow> getRows() {
        return rows;
    }

    /**
     * Reloads the watchlist entries from the DAO and stock repository.
     * Clears existing rows, then for each symbol in the user's watchlist:
     * verifies availability, retrieves the IStock, and adds a row
     * with a removal callback that updates both DAO and view.
     * Any SQLException or IOException is wrapped in a RuntimeException.
     */
    public void refresh() {
        rows.clear();
        try {
            List<StockName> symbols = dao.listForUser(userId);
            Set<String>     avail   = repo.availableTickers();
            for (StockName sym : symbols) {
                String ticker = sym.getSymbol();
                if (!avail.contains(ticker)) continue;
                IStock stock = repo.getByTicker(ticker);
                rows.add(new WatchlistRow(stock, () -> {
                    try {
                        dao.removeForUser(userId, sym);
                        refresh();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
