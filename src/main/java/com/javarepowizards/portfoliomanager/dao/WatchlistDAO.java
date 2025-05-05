package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.StockName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WatchlistDAO implements IWatchlistDAO {
    // SQL for table creation + CRUD
    private static final String CREATE_SQL = """
        CREATE TABLE IF NOT EXISTS user_watchlist (
          user_id INTEGER     NOT NULL,
          symbol  TEXT        NOT NULL,
          PRIMARY KEY (user_id, symbol),
          FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE
        );
        """;
    private static final String SELECT_SQL =
            "SELECT symbol FROM user_watchlist WHERE user_id = ?";
    private static final String INSERT_SQL =
            "INSERT OR IGNORE INTO user_watchlist(user_id, symbol) VALUES(?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM user_watchlist WHERE user_id = ? AND symbol = ?";

    private final Connection conn;

    @Autowired
    public WatchlistDAO(IDatabaseConnection databaseConnection) throws SQLException {
        this.conn = databaseConnection.getConnection();
        // ensure table exists
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_SQL);
        }
    }

    /** Returns all symbols in this user's watchlist. */
    public List<StockName> listForUser(int userId) throws SQLException {
        List<StockName> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_SQL)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(StockName.valueOf(rs.getString("symbol")));
                }
            }
        }
        return out;
    }

    /** Adds the given symbol to the user's watchlist (no‑op if already present). */
    public void addForUser(int userId, StockName symbol) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol.name());
            ps.executeUpdate();
        }
    }

    /** Removes the given symbol from the user's watchlist (no‑op if absent). */
    public void removeForUser(int userId, StockName symbol) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol.name());
            ps.executeUpdate();
        }
    }
}
