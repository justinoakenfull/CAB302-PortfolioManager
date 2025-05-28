package com.javarepowizards.portfoliomanager.dao.user;

import com.javarepowizards.portfoliomanager.dao.IDatabaseConnection;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.session.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserDAO implements IUserDAO {
    private final Connection connection;

    /**
     * Constructs a {@code UserDAO} instance with the provided database connection
     * and initializes the necessary database tables.
     *
     * @param dbConnection an {@link IDatabaseConnection} providing the database connection
     * @throws SQLException if a database access error occurs during table creation
     */
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
                    CHECK (simulation_difficulty IN ('Easy','Medium','Hard')) DEFAULT 'Easy', 
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
                  FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE
                )
            """);
        }
    }

    /**
     * Creates a new user by inserting authentication, profile, and balance records in a single transaction.
     * <p>
     * This method inserts user credentials into the {@code user_auth} table, retrieves the generated user ID,
     * and uses it to initialize related entries in the {@code users} and {@code user_balances} tables.
     * The entire operation is atomic: if any step fails, all changes are rolled back.
     *
     * @param user the {@link User} containing username, email, and password hash; its ID is set upon success
     * @return {@code true} if the user was successfully created
     * @throws SQLException if a database error occurs or no rows are affected during insertion
     */
    @Override
    public boolean createUser(User user, double startingBalance) throws SQLException {
        connection.setAutoCommit(false);
        try {
            final String authSql = """
            INSERT INTO user_auth (username, email, password_hash)
            VALUES (?, ?, ?)
        """;

            try (PreparedStatement authStmt = connection.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {
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

                        try (PreparedStatement userStmt = connection.prepareStatement("INSERT INTO users (user_id) VALUES (?)")) {
                            userStmt.setInt(1, userId);
                            userStmt.executeUpdate();
                        }

                        try (PreparedStatement balanceStmt = connection.prepareStatement("INSERT INTO user_balances (user_id, balance) VALUES (?, ?)")) {
                            balanceStmt.setInt(1, userId);
                            balanceStmt.setDouble(2, startingBalance);
                            balanceStmt.executeUpdate();
                        }
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
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



    /**
     * Retrieves a user from the database by their email address.
     * <p>
     * Queries the {@code user_auth} table to find a user with the specified email.
     * If a matching user is found, returns an {@link Optional} containing the {@link User}.
     * Otherwise, returns {@link Optional#empty()}.
     *
     * @param email the email address of the user to retrieve
     * @return an {@link Optional} with the {@link User} if found, or empty if no matching user exists
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Optional<User> getUserByEmail(String email) throws SQLException {
        final String sql = """
        SELECT user_id, username, email, password_hash
          FROM user_auth
         WHERE email = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        return Optional.empty();
    }

    /**
     * Retrieves a user from the database using their username.
     * <p>
     * This method queries the {@code user_auth} table to find a user
     * with the specified username. If a matching record is found, a
     * {@link User} object is created and populated with the retrieved data,
     * including the user's ID, username, email, and password hash.
     *
     * @param username the username of the user to retrieve
     * @return an {@link Optional} containing the {@link User} if found,
     *         or {@link Optional#empty()} if no matching user exists
     * @throws SQLException if a database access error occurs
     */
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
                    User user = new User(
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    );
                    user.setId(rs.getInt("user_id"));
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a user from the database using their unique user ID.
     * <p>
     * This method queries the {@code user_auth} table to find a user
     * with the specified user ID. If a matching record is found, a
     * {@link User} object is created and populated with the retrieved data,
     * including the user's ID, username, email, and password hash.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return an {@link Optional} containing the {@link User} if found,
     *         or {@link Optional#empty()} if no matching user exists
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Optional<User> getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, email, password_hash FROM user_auth WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        return Optional.empty();
    }

    /**
     * Updates the simulation difficulty setting for a specific user.
     * <p>
     * This method updates the {@code simulation_difficulty} field in the
     * {@code users} table for the user identified by the given user ID.
     *
     * @param userId the unique identifier of the user whose difficulty setting is to be updated
     * @param difficulty the new simulation difficulty value to set
     * @throws SQLException if a database access error occurs or the update fails
     */
    @Override
    public void updateSimulationDifficulty(int userId, String difficulty) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "UPDATE users SET simulation_difficulty = ? WHERE user_id = ?")) {
            p.setString(1, difficulty);
            p.setInt(2, userId);
            p.executeUpdate();
        }
    }

    /**
     * Updates the first name and/or last name of a specific user in the database.
     * <p>
     * This method builds and executes a single SQL {@code UPDATE} statement to modify the
     * {@code first_name} and/or {@code last_name} columns in the {@code users} table,
     * depending on which parameters are non-null. If both {@code fName} and {@code lName}
     * are {@code null}, no operation is performed.
     *
     * @param userId the unique identifier of the user whose name is to be updated
     * @param fName the new first name to set, or {@code null} to leave it unchanged
     * @param lName the new last name to set, or {@code null} to leave it unchanged
     * @throws SQLException if a database access error occurs during the update
     */
    @Override
    public void updateFullName(int userId, String fName, String lName) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();
        if (fName != null) {
            sql.append("first_name = ?");
            params.add(fName);
        }
        if (lName != null) {
            if (!params.isEmpty()) sql.append(", ");
            sql.append("last_name = ?");
            params.add(lName);
        }
        sql.append(" WHERE user_id = ?");
        params.add(userId);
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Updates the email address for a specific user.
     * <p>
     * This method updates the {@code email} field in the {@code user_auth} table
     * for the user identified by the provided user ID.
     *
     * @param userId the unique identifier of the user whose email is to be updated
     * @param email the new email address to assign to the user
     * @throws RuntimeException if a database access error occurs
     */
    @Override
    public void updateEmail(int userId, String email) {
        final String sql = "UPDATE user_auth SET email = ? WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update email for user ID: " + userId, e);
        }
    }



    /**
     * Updates the username for a specific user.
     * <p>
     * This method updates the {@code username} field in the {@code user_auth} table
     * for the user identified by the provided user ID.
     *
     * @param userId the unique identifier of the user whose username is to be updated
     * @param username the new username to assign to the user
     * @throws RuntimeException if a database access error occurs
     */
    @Override
    public void updateUsername(int userId, String username) {
        final String sql = "UPDATE user_auth SET username = ? WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update username for user ID: " + userId, e);
        }
    }

    /**
     * Updates the password hash for a specific user.
     * <p>
     * This method updates the {@code password_hash} field in the {@code user_auth} table
     * for the user identified by the given user ID.
     *
     * @param userId the unique identifier of the user whose password is to be updated
     * @param newPassword the new password hash to assign to the user
     * @throws RuntimeException if a database access error occurs
     */
    @Override
    public void updatePassword(int userId, String newPassword) {
        final String sql = "UPDATE user_auth SET password_hash = ? WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update password for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves the currently authenticated user based on the active session.
     * <p>
     * This method obtains the current user's ID from the session and fetches
     * the corresponding {@link User} from the database.
     *
     * @return an {@link Optional} containing the current {@link User} if present,
     *         or {@link Optional#empty()} if no user is authenticated
     * @throws RuntimeException if a database access error occurs while retrieving the user
     */
    @Override
    public Optional<User> getCurrentUser() {
        try {
            return getUserById(Session.getCurrentUser().getUserId());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve current user from session", e);
        }
    }

   @Override
    public double getBalance(int userId) throws SQLException {
        String sql = "SELECT balance FROM user_balances WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        }
        return 0.0;
    }
}
