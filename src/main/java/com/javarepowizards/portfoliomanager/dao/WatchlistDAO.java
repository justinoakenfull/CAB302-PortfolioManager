package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.StockName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for managing a user's watchlist in the database.
 * Uses JDBC to create the watchlist table if necessary and perform CRUD operations.
 * Notifies registered listeners when the watchlist is modified.
 */
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

    private final List<Runnable> listeners = new ArrayList<>();

    /**
     * Registers a listener to be invoked whenever the watchlist is changed.
     *
     * @param r the callback to register
     */
    @Override
    public void addListener(Runnable r) { listeners.add(r); }

    /**
     * Unregisters a previously registered watchlist change listener.
     *
     * @param r the callback to unregister
     */
    @Override
    public void removeListener (Runnable r) { listeners.remove(r);}

    private void notifyListeners() {listeners.forEach(Runnable ::run);}


    /**
     * Constructs a WatchlistDAO using the provided database connection.
     * Ensures that the user_watchlist table exists by executing the DDL.
     *
     * @param databaseConnection provider of the JDBC connection
     * @throws SQLException if an error occurs creating the table or obtaining the connection
     */
    @Autowired
    public WatchlistDAO(IDatabaseConnection databaseConnection) throws SQLException {
        this.conn = databaseConnection.getConnection();
        // ensure table exists
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_SQL);
        }
    }

    /**
     * Retrieves all stock symbols in the specified user's watchlist.
     *
     * @param userId the unique identifier of the user
     * @return a list of StockName enums representing the user's watchlist
     * @throws SQLException if a database access error occurs
     */
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

    /**
     * Adds the given stock symbol to the user's watchlist.
     * Does nothing if the symbol is already present.
     * Notifies listeners if the insertion affects at least one row.
     *
     * @param userId the unique identifier of the user
     * @param symbol the stock symbol to add
     * @throws SQLException if a database access error occurs
     */
    public void addForUser(int userId, StockName symbol) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol.name());
            if (ps.executeUpdate() > 0){
             notifyListeners();
            }
        }
    }

    /**
     * Removes the given stock symbol from the user's watchlist.
     * Does nothing if the symbol is not present.
     * Notifies listeners if the deletion affects at least one row.
     *
     * @param userId the unique identifier of the user
     * @param symbol the stock symbol to remove
     * @throws SQLException if a database access error occurs
     */
    public void removeForUser(int userId, StockName symbol) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, userId);
            ps.setString(2, symbol.name());
            if (ps.executeUpdate() > 0) {
                notifyListeners();
            }
        }
    }
}
