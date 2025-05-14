package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.StockName;
import java.sql.SQLException;
import java.util.List;
/**
 * Data access interface for managing a user's watchlist.
 */
public interface IWatchlistDAO {

    /**
     * Retrieves the list of stock symbols in the watchlist for the specified user.
     *
     * @param userId the unique identifier of the user
     * @return a list of stock names in the user's watchlist
     * @throws SQLException if a database access error occurs
     */
    List<StockName> listForUser(int userId) throws SQLException;

    /**
     * Adds a stock symbol to the watchlist of the specified user.
     *
     * @param userId the unique identifier of the user
     * @param symbol the stock symbol to add
     * @throws SQLException if a database access error occurs
     */
    void addForUser(int userId, StockName symbol) throws SQLException;

    /**
     * Removes a stock symbol from the watchlist of the specified user.
     *
     * @param userId the unique identifier of the user
     * @param symbol the stock symbol to remove
     * @throws SQLException if a database access error occurs
     */
    void removeForUser(int userId, StockName symbol) throws SQLException;

    /**
     * Registers a listener to be notified when the watchlist changes.
     *
     * @param r the listener to add
     */
    void addListener (Runnable r);

    /**
     * Unregisters a previously registered watchlist change listener.
     *
     * @param r the listener to remove
     */
    void removeListener (Runnable r);
}
