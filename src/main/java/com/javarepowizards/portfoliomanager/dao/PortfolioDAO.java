package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for portfolio operations.
 * Supports both DB-backed and in-memory modes for retrieving and updating
 * user holdings and balances.
 */
@Repository
public class PortfolioDAO implements IPortfolioDAO {

    /** Connection to the SQL database (null if in-memory mode) */
    private final Connection conn;

    /** Indicates whether the DAO is using a real database */
    private final boolean dbMode;

    /** In-memory list of portfolio holdings (used only in testing) */
    private List<PortfolioEntry> holdings;

    /** Available balance (only meaningful in in-memory mode) */
    private final double availableBalance;

    /**
     * Constructs a DB-backed PortfolioDAO.
     *
     * @param dbConnection The database connection provider
     */
    @Autowired
    public PortfolioDAO(IDatabaseConnection dbConnection) {
        try {
            this.conn = dbConnection.getConnection();
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain DB connection", e);
        }
        this.dbMode = true;
        this.holdings = null;
        this.availableBalance = 0.0;
    }

    /**
     * Constructs an in-memory PortfolioDAO for testing or simulation.
     *
     * @param holdings Portfolio entries
     * @param availableBalance Available balance in the test context
     */
    public PortfolioDAO(List<PortfolioEntry> holdings, double availableBalance) {
        if (!isTestContext()) {
            throw new IllegalStateException("In-memory PortfolioDAO should only be used in tests or simulations.");
        }

        this.conn = null;
        this.dbMode = false;
        this.holdings = holdings;
        this.availableBalance = availableBalance;
    }

    /**
     * Checks whether the DAO is being used in a test context.
     *
     * @return true if used in test or simulation context, false otherwise
     */
    private boolean isTestContext() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement frame : stack) {
            String cls = frame.getClassName();
            if (cls.contains("Test") || cls.contains("PortfolioInitializer")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the list of portfolio holdings for the current user.
     *
     * @return List of PortfolioEntry objects
     */
    @Override
    public List<PortfolioEntry> getHoldings() {
        if (!dbMode) {
            return holdings;
        }

        int userId = Session.getCurrentUser().getUserId();
        String sql = """
                  SELECT ticker, holding_amount, holding_value
                    FROM user_holdings
                   WHERE user_id = ?
                """;
        List<PortfolioEntry> out = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ticker = rs.getString("ticker");
                    int qty = rs.getInt("holding_amount");
                    double val = rs.getDouble("holding_value");
                    double avg = qty > 0 ? val / qty : 0.0;

                    out.add(new PortfolioEntry(
                            StockName.fromString(ticker),
                            avg,
                            qty
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load holdings for user " + userId, e);
        }

        return out;
    }

    /**
     * Gets the available cash balance for the current user.
     *
     * @return Available balance as a double
     */
    @Override
    public double getAvailableBalance() {
        if (!dbMode) {
            return availableBalance;
        }

        int userId = Session.getCurrentUser().getUserId();
        String sql = "SELECT balance FROM user_balances WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("balance") : 0.0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load balance for user " + userId, e);
        }
    }


    /**
     * Deducts a specific amount from the user's balance.
     *
     * @param userId User identifier
     * @param amount Amount to deduct
     * @throws SQLException If the SQL operation fails
     */
    public void deductFromBalance(int userId, double amount) throws SQLException {
        String sql = "UPDATE user_balances SET balance = balance - ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new holding or updates existing one for the user.
     *
     * @param entry Portfolio entry to be added or updated
     */
    @Override
    public void addToHoldings(PortfolioEntry entry) {
        if (!dbMode) {
            holdings.add(entry);
            return;
        }

        int userId = Session.getCurrentUser().getUserId();
        String sql = """
              INSERT INTO user_holdings 
                (user_id, ticker, holding_amount, holding_value)
              VALUES (?, ?, ?, ?)
              ON CONFLICT(user_id, ticker) DO UPDATE
                SET holding_amount = user_holdings.holding_amount + excluded.holding_amount,
                    holding_value  = user_holdings.holding_value  + excluded.holding_value
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, entry.getStock().getSymbol());
            ps.setInt(3, entry.getAmountHeld());
            ps.setDouble(4, entry.getMarketValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upsert holding for user " + userId, e);
        }
    }

    /**
     * Fetches all holdings for a specific user.
     *
     * @param userId User identifier
     * @return List of PortfolioEntry objects
     */
    public List<PortfolioEntry> getHoldingsForUser(int userId) {
        String sql = """
                SELECT ticker, holding_amount, holding_value
                  FROM user_holdings
                 WHERE user_id = ?
            """;
        List<PortfolioEntry> holdings = new ArrayList<>();
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int amt = rs.getInt("holding_amount");
                    double val = rs.getDouble("holding_value");
                    double avg = amt > 0 ? val / amt : 0.0;
                    holdings.add(new PortfolioEntry(
                            StockName.fromString(rs.getString("ticker")),
                            avg,
                            amt
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch holdings for user " + userId, e);
        }
        return holdings;
    }

    /**
     * Inserts or updates a specific holding in the database.
     *
     * @param userId User identifier
     * @param stock Stock to update
     * @param quantity Quantity of shares
     * @param totalValue Total market value
     */
    public void upsertHolding(int userId, StockName stock, int quantity, double totalValue) {
        String sql = """
                INSERT INTO user_holdings (user_id, ticker, holding_amount, holding_value)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(user_id, ticker) DO UPDATE
                  SET holding_amount = user_holdings.holding_amount + excluded.holding_amount,
                      holding_value  = user_holdings.holding_value  + excluded.holding_value
            """;
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, stock.getSymbol());
            p.setInt(3, quantity);
            p.setDouble(4, totalValue);
            p.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to upsert holding for user " + userId, e);
        }
    }

    /**
     * Calculates the total value of the user's portfolio.
     *
     * @return Total portfolio value including cash and holdings
     */
    @Override
    public double getTotalPortfolioValue() {
        if (!dbMode) {
            double total = availableBalance;
            for (PortfolioEntry e : holdings) {
                total += e.getMarketValue();
            }
            return total;
        }

        int userId = Session.getCurrentUser().getUserId();
        double cash = getAvailableBalance();
        String sql = "SELECT SUM(holding_value) AS total FROM user_holdings WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                double sum = rs.next() ? rs.getDouble("total") : 0.0;
                return cash + sum;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to compute total for user " + userId, e);
        }
    }

    /**
     * Creates the user_holdings table if it does not exist.
     */
    public void createTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_holdings (
                      user_id         INTEGER     NOT NULL,
                      ticker          VARCHAR(10) NOT NULL,
                      holding_amount  INTEGER     NOT NULL,
                      holding_value   DECIMAL(15,2) NOT NULL,
                      PRIMARY KEY (user_id, ticker),
                      FOREIGN KEY (user_id) REFERENCES user_auth(user_id) 
                        ON DELETE CASCADE
                    )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user_holdings table", e);
        }
    }

    /**
     * Sells a specific holding and updates the user's balance.
     *
     * @param userId User identifier
     * @param stock Stock to sell
     * @throws SQLException If an error occurs during SQL operations
     */
    @Override
    public void sellHolding(int userId, StockName stock) throws SQLException {
        double marketValue = 0.0;

        // Fetch current market value of the holding
        String selectSQL = "SELECT holding_value FROM user_holdings WHERE user_id = ? AND ticker = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
            selectStmt.setInt(1, userId);
            selectStmt.setString(2, stock.getSymbol());

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    marketValue = rs.getDouble("holding_value");
                } else {
                    throw new SQLException("Holding not found for stock: " + stock.getSymbol());
                }
            }
        }

        // Delete holding and update user balance
        String deleteSQL = "DELETE FROM user_holdings WHERE user_id = ? AND ticker = ?";
        String updateSQL = "UPDATE user_balances SET balance = balance + ? WHERE user_id = ?";

        try (
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL);
                PreparedStatement updateStmt = conn.prepareStatement(updateSQL)
        ) {
            deleteStmt.setInt(1, userId);
            deleteStmt.setString(2, stock.getSymbol());
            deleteStmt.executeUpdate();

            updateStmt.setDouble(1, marketValue);
            updateStmt.setInt(2, userId);
            updateStmt.executeUpdate();
        }
    }
}




