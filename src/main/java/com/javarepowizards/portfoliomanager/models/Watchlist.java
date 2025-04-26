package com.javarepowizards.portfoliomanager.models;

import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.WatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Holds the shared, observable list of WatchlistRow for one user.
 * Any view binding to this model will see updates automatically.
 */
public class Watchlist {
    private final StockRepository   repo;
    private final WatchlistDAO      dao;
    private final int               userId;
    private final ObservableList<WatchlistRow> rows = FXCollections.observableArrayList();

    public Watchlist(StockRepository repo,
                          WatchlistDAO dao,
                          int userId) {
        this.repo   = repo;
        this.dao    = dao;
        this.userId = userId;
        refresh();
    }

    /** @return the live list of rows, for TableView#setItems(...) */
    public ObservableList<WatchlistRow> getRows() {
        return rows;
    }

    /** Reloads the list from the DAO & shared repo. */
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
