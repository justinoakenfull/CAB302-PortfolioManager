package com.javarepowizards.portfoliomanager.domain;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Read-only interface for retrieving a user's watchlist.
 */
public interface IWatchlistReadOnly {

    /**
     *
     * @return the list of stocks in the current user's watchlist
     * @throws SQLException on database access error
     * @throws IOException on CSV data file error
     */
    List<IStock> getWatchlist() throws SQLException, IOException;
}