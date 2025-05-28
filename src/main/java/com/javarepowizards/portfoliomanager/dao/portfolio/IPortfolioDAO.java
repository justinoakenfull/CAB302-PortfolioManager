package com.javarepowizards.portfoliomanager.dao.portfolio;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for portfolio data access operations.
 * Defines methods to manipulate user portfolio holdings and balances.
 * Supports both reading and updating data in persistent or in-memory storage.
 */
public interface IPortfolioDAO {

    /**
     * Gets the holdings for the currently logged-in user.
     *
     * @return List of PortfolioEntry objects
     */
    List<PortfolioEntry> getHoldings();

    /**
     * Gets the available cash balance for the currently logged-in user.
     *
     * @return The available balance as a double
     */
    double getAvailableBalance();

    /**
     * Adds a new holding or updates existing one for the user.
     *
     * @param entry The portfolio entry to be added or updated
     */
    void addToHoldings(PortfolioEntry entry);

    /**
     * Calculates the total value of the user's portfolio.
     *
     * @return Total portfolio value including holdings and cash
     */
    double getTotalPortfolioValue();

    /**
     * Inserts or updates a specific stock holding.
     *
     * @param userId ID of the user
     * @param stock Stock name
     * @param quantity Quantity to add
     * @param totalValue Total market value of the added quantity
     */
    void upsertHolding(int userId, StockName stock, int quantity, double totalValue);

    /**
     * Gets all holdings for a specified user ID.
     *
     * @param userId ID of the user
     * @return List of PortfolioEntry records
     * @throws SQLException if retrieval fails
     */
    List<PortfolioEntry> getHoldingsForUser(int userId) throws SQLException;

    /**
     * Sells a specific stock holding for the user.
     *
     * @param userId ID of the user
     * @param stock Stock to be sold
     * @throws SQLException if sell operation fails
     */
    void sellHolding(int userId, StockName stock) throws SQLException;

    /**
     * Deducts a specific amount from the user's available balance.
     *
     * @param userId ID of the user
     * @param amount Amount to deduct
     * @throws SQLException if the update fails
     */
    void deductFromBalance(int userId, double amount) throws SQLException;
}

