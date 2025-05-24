package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;

import java.sql.SQLException;
import java.util.List;

/**
 * <summary>Interface for portfolio data access operations</summary>
 * <remarks>
 * Defines methods to manipulate user portfolio holdings and balances.
 * Supports both reading and updating data in persistent or in-memory storage.
 * </remarks>
 */
public interface IPortfolioDAO {

    /**
     * <summary>Gets the holdings for the currently logged-in user</summary>
     * @return List of PortfolioEntry objects
     */
    List<PortfolioEntry> getHoldings();

    /**
     * <summary>Gets the available cash balance for the currently logged-in user</summary>
     * @return The available balance as a double
     */
    double getAvailableBalance();

    /**
     * <summary>Adds a new holding or updates existing one for the user</summary>
     * @param entry The portfolio entry to be added or updated
     */
    void addToHoldings(PortfolioEntry entry);

    /**
     * <summary>Calculates the total value of the user's portfolio</summary>
     * @return Total portfolio value including holdings and cash
     */
    double getTotalPortfolioValue();

    /**
     * <summary>Inserts or updates a specific stock holding</summary>
     * @param userId ID of the user
     * @param stock Stock name
     * @param quantity Quantity to add
     * @param totalValue Total market value of the added quantity
     */
    void upsertHolding(int userId, StockName stock, int quantity, double totalValue);

    /**
     * <summary>Gets all holdings for a specified user ID</summary>
     * @param userId ID of the user
     * @return List of PortfolioEntry records
     * @throws SQLException if retrieval fails
     */
    List<PortfolioEntry> getHoldingsForUser(int userId) throws SQLException;

    /**
     * <summary>Sells a specific stock holding for the user</summary>
     * @param userId ID of the user
     * @param stock Stock to be sold
     * @throws SQLException if sell operation fails
     */
    void sellHolding(int userId, StockName stock) throws SQLException;

    /**
     * <summary>Deducts a specific amount from the user's available balance</summary>
     * @param userId ID of the user
     * @param amount Amount to deduct
     * @throws SQLException if the update fails
     */
    void deductFromBalance(int userId, double amount) throws SQLException;
}
