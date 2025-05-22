package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.services.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PortfolioDAO implements IPortfolioDAO {
    //shared fields
    private final Connection conn;            // null if using in-memory mode
    private final boolean dbMode;             // true=DB-backed, false=in-memory
    private List<PortfolioEntry> holdings;    // only non-null in in-memory mode
    private final double availableBalance;    // only meaningful in in-memory mode

    //DB-backed constructorS
    @Autowired
    public PortfolioDAO(IDatabaseConnection dbConnection) {
        try {
            this.conn = dbConnection.getConnection();
            createTables(); // Add this line
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain DB connection", e);
        }
        this.dbMode = true;
        this.holdings = null;
        this.availableBalance = 0.0;
    }

    // In-memory constructor (for dummy/testing use)
    public PortfolioDAO(List<PortfolioEntry> holdings, double availableBalance) {
        // Protect against accidental use in production
        if (!isTestContext()) {
            throw new IllegalStateException("In-memory PortfolioDAO should only be used in tests or simulations.");
        }

        this.conn = null;
        this.dbMode = false;
        this.holdings = holdings;
        this.availableBalance = availableBalance;
    }

    private boolean isTestContext() {
        // Check if the call is from a test or dummy class
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement frame : stack) {
            String cls = frame.getClassName();
            if (cls.contains("Test") || cls.contains("PortfolioInitializer")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<PortfolioEntry> getHoldings() {
        if (!dbMode) {
            // in-memory: just return the list given
            return holdings;
        }

        // DB-mode: pull from user_holdings
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

    @Override
    public double getAvailableBalance() {
        if (!dbMode) {
            return availableBalance;
        }

        // DB-mode: read from user_balances
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

    @Override
    public void addToHoldings(PortfolioEntry entry) {
        if (!dbMode) {
            // in-memory: just append
            holdings.add(entry);
            return;
        }

        // DB-mode: upsert into user_holdings
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


    @Override
    public double getTotalPortfolioValue() {
        if (!dbMode) {
            // in-memory: cash + sum of holdings
            double total = availableBalance;
            for (PortfolioEntry e : holdings) {
                total += e.getMarketValue();
            }
            return total;
        }

        // DB-mode: balance + SUM(holding_value)
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
}
