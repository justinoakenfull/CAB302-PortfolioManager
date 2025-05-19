package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDAO implements IUserDAO {

    private final Connection connection;

    @Autowired
    public UserDAO(IDatabaseConnection dbConnection) throws SQLException {
        this.connection = dbConnection.getConnection();
        createTables();
    }

    /**
     * Create or migrate all tables.
     * Always drops & recreates user_holdings so we get (user_id,ticker) as a true PK.
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // --- auth table ---
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_auth (
                  user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username VARCHAR(24) UNIQUE NOT NULL,
                  email VARCHAR(255) UNIQUE NOT NULL,
                  password_hash VARCHAR(255) NOT NULL
                )
            """);

            // --- users profile table ---
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  user_id INTEGER PRIMARY KEY,
                  phone VARCHAR(20),
                  first_name VARCHAR(255),
                  last_name VARCHAR(255),
                  simulation_difficulty TEXT 
                    CHECK (simulation_difficulty IN ('Easy','Medium','Hard')) 
                    DEFAULT 'Easy',
                  FOREIGN KEY (user_id) REFERENCES user_auth(user_id) 
                    ON DELETE CASCADE
                )
            """);

            // --- balances table ---
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_balances (
                  user_id INTEGER PRIMARY KEY,
                  balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                  currency VARCHAR(3) DEFAULT 'AUD',
                  FOREIGN KEY (user_id) REFERENCES user_auth(user_id) 
                    ON DELETE CASCADE
                )
            """);

            // --- holdings table: always drop & recreate with composite PK ---
            //stmt.execute("DROP TABLE IF EXISTS user_holdings");
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
        }
    }

    @Override
    public boolean createUser(User user) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String authSql = """
                INSERT INTO user_auth (username, email, password_hash)
                VALUES (?, ?, ?)
            """;
            try (PreparedStatement authStmt = connection.prepareStatement(
                    authSql, Statement.RETURN_GENERATED_KEYS)) {
                authStmt.setString(1, user.getUsername());
                authStmt.setString(2, user.getEmail());
                authStmt.setString(3, user.getPasswordHash());
                if (authStmt.executeUpdate() == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }
                try (ResultSet rs = authStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        user.setId(userId);
                        try (PreparedStatement p1 = connection.prepareStatement(
                                "INSERT INTO users (user_id) VALUES (?)")) {
                            p1.setInt(1, userId); p1.executeUpdate();
                        }
                        try (PreparedStatement p2 = connection.prepareStatement(
                                "INSERT INTO user_balances (user_id) VALUES (?)")) {
                            p2.setInt(1, userId); p2.executeUpdate();
                        }
                    }
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) throws SQLException {
        String sql = """
            SELECT ua.user_id, ua.username, ua.email, ua.password_hash
              FROM user_auth ua
             WHERE ua.email = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                    u.setId(rs.getInt("user_id"));
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = """
            SELECT ua.user_id, ua.username, ua.email, ua.password_hash
              FROM user_auth ua
             WHERE ua.username = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                    u.setId(rs.getInt("user_id"));
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, email, password_hash FROM user_auth WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                    u.setId(rs.getInt("user_id"));
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void updateSimulationDifficulty(int userId, String difficulty) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE users SET simulation_difficulty = ? WHERE user_id = ?")) {
            p.setString(1, difficulty);
            p.setInt(2, userId);
            p.executeUpdate();
        }
    }

    @Override
    public void updateFullName(int userId, String fName, String lName) throws SQLException {
        if (fName != null) {
            try (PreparedStatement p = connection.prepareStatement(
                    "UPDATE users SET first_name = ? WHERE user_id = ?")) {
                p.setString(1, fName);
                p.setInt(2, userId);
                p.executeUpdate();
            }
        }
        if (lName != null) {
            try (PreparedStatement p = connection.prepareStatement(
                    "UPDATE users SET last_name = ? WHERE user_id = ?")) {
                p.setString(1, lName);
                p.setInt(2, userId);
                p.executeUpdate();
            }
        }
    }

    @Override
    public void updateEmail(int userId, String email) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE user_auth SET email = ? WHERE user_id = ?")) {
            p.setString(1, email);
            p.setInt(2, userId);
            p.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateUsername(int userId, String username) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE user_auth SET username = ? WHERE user_id = ?")) {
            p.setString(1, username);
            p.setInt(2, userId);
            p.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(int userId, String newPassword) {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE user_auth SET password_hash = ? WHERE user_id = ?")) {
            p.setString(1, newPassword);
            p.setInt(2, userId);
            p.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> getCurrentUser() {
        try {
            return getUserById(Session.getCurrentUser().getUserId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PortfolioEntry> getHoldingsForUser(int userId) throws SQLException {
        String sql = """
            SELECT ticker, holding_amount, holding_value
              FROM user_holdings
             WHERE user_id = ?
        """;
        var holdings = new ArrayList<PortfolioEntry>();
        try (PreparedStatement p = connection.prepareStatement(sql)) {
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
        }
        return holdings;
    }

    @Override
    public void upsertHolding(int userId, StockName stock, int quantity, double totalValue) throws SQLException {
        String sql = """
            INSERT INTO user_holdings (user_id, ticker, holding_amount, holding_value)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id, ticker) DO UPDATE
              SET holding_amount = user_holdings.holding_amount + excluded.holding_amount,
                  holding_value  = user_holdings.holding_value  + excluded.holding_value
        """;
        try (PreparedStatement p = connection.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, stock.getSymbol());
            p.setInt(3, quantity);
            p.setDouble(4, totalValue);
            p.executeUpdate();
        }
    }
}
