package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
public class UserDAO implements IUserDAO {

    private final Connection connection;

    @Autowired
    public UserDAO(IDatabaseConnection dbConnection) throws SQLException {
        this.connection = dbConnection.getConnection();
        createTables();
    }

    private void createTables() throws SQLException {
        String[] sqlStatements = {
                """
            CREATE TABLE IF NOT EXISTS user_auth (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username VARCHAR(24) UNIQUE NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY,
                phone VARCHAR(20),
                first_name VARCHAR(255),
                last_name VARCHAR(255),
                simulation_difficulty TEXT CHECK (simulation_difficulty IN ('Easy', 'Medium', 'Hard')) DEFAULT 'Easy',
                FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS user_balances (
                user_id INTEGER PRIMARY KEY,
                balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                currency VARCHAR(3) DEFAULT 'AUD',
                FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS user_holdings (
                user_id INTEGER PRIMARY KEY,
                ticker VARCHAR(10) NOT NULL,
                holding_amount INTEGER NOT NULL,
                holding_value DECIMAL(15,2) NOT NULL,
                FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE
            )
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
        }
    }

    public boolean createUser(User user) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String authSql = """
                INSERT INTO user_auth (username, email, password_hash)
                VALUES (?, ?, ?)
                """;

            try (PreparedStatement authStmt = connection.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
                authStmt.setString(1, user.getUsername());
                authStmt.setString(2, user.getEmail());
                authStmt.setString(3, user.getPasswordHash());

                int affectedRows = authStmt.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Creating user failed, no rows affected.");

                try (ResultSet rs = authStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        user.setId(userId);

                        try (PreparedStatement profileStmt = connection.prepareStatement("""
                            INSERT INTO users (user_id) VALUES (?)""")) {
                            profileStmt.setInt(1, userId);
                            profileStmt.executeUpdate();
                        }

                        try (PreparedStatement balanceStmt = connection.prepareStatement("""
                            INSERT INTO user_balances (user_id) VALUES (?)""")) {
                            balanceStmt.setInt(1, userId);
                            balanceStmt.executeUpdate();
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

    public Optional<User> getUserByEmail(String email) throws SQLException {
        String sql = """
            SELECT ua.user_id, ua.username, ua.email, ua.password_hash,
                   up.first_name, up.last_name, up.phone,
                   ub.balance, ub.currency
            FROM user_auth ua
            LEFT JOIN users up ON ua.user_id = up.user_id
            LEFT JOIN user_balances ub ON ua.user_id = ub.user_id
            WHERE ua.email = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );
                user.setId(rs.getInt("user_id"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = """
            SELECT ua.user_id, ua.username, ua.email, ua.password_hash,
                   up.first_name, up.last_name, up.phone,
                   ub.balance, ub.currency
            FROM user_auth ua
            LEFT JOIN users up ON ua.user_id = up.user_id
            LEFT JOIN user_balances ub ON ua.user_id = ub.user_id
            WHERE ua.username = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );
                user.setId(rs.getInt("user_id"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }


    public Optional<User> getUserById(int userId) throws SQLException {
        String sql = """
            SELECT ua.user_id, ua.username, ua.email, ua.password_hash,
                   up.first_name, up.last_name, up.phone,
                   ub.balance, ub.currency
            FROM user_auth ua
            LEFT JOIN users up ON ua.user_id = up.user_id
            LEFT JOIN user_balances ub ON ua.user_id = ub.user_id
            WHERE ua.user_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );
                user.setId(rs.getInt("user_id"));
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public void updateSimulationDifficulty(int userId, String difficulty) throws SQLException {
        String sql = "UPDATE users SET simulation_difficulty = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, difficulty);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void updateFullName(int userId, String fName, String lName) throws SQLException {
        String sqlFName = "UPDATE users SET first_name = ? WHERE user_id = ?";
        String sqlLName = "UPDATE users SET last_name = ? WHERE user_id = ?";
        if (fName != null) {
            try (PreparedStatement pstmt = connection.prepareStatement(sqlFName)) {
                pstmt.setString(1, fName);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }
        }
        if (lName != null) {
            try (PreparedStatement pstmt = connection.prepareStatement(sqlLName)) {
                pstmt.setString(1, lName);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }
        }
    }




    public void updateEmail(int userId, String email) {
        String sql = "UPDATE user_auth SET email = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void updateUsername(int userId, String username) {
        String sql = "UPDATE user_auth SET username = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePassword(int userId, String newPassword) {
        String sql = "UPDATE user_auth set password_hash = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> getCurrentUser() {
        Optional<User> user;

        try {
            user = getUserById(Session.getCurrentUser().getUserId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return user;
    }
}
