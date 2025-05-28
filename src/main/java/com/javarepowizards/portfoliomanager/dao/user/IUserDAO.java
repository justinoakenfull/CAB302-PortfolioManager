package com.javarepowizards.portfoliomanager.dao.user;
import com.javarepowizards.portfoliomanager.models.User;

import java.sql.SQLException;
import java.util.Optional;


/**
 * Interface defining data access operations for {@link User} entities.
 */
public interface IUserDAO {

    /**
     * Creates a new user in the database.
     *
     * @param user the {@link User} to create
     * @return {@code true} if the user was successfully created
     * @throws SQLException if a database access error occurs
     */
    boolean createUser(User user, double startingBalance) throws SQLException;

    double getBalance(int userId) throws SQLException;

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching {@link User}, or empty if none found
     * @throws SQLException if a database access error occurs
     */
    Optional<User> getUserByEmail(String email) throws SQLException;

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the matching {@link User}, or empty if none found
     * @throws SQLException if a database access error occurs
     */
    Optional<User> getUserByUsername(String username) throws SQLException;

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the user ID to search for
     * @return an {@link Optional} containing the matching {@link User}, or empty if none found
     * @throws SQLException if a database access error occurs
     */
    Optional<User> getUserById(int userId) throws SQLException;

    /**
     * Retrieves the currently authenticated user.
     *
     * @return an {@link Optional} containing the current {@link User}, or empty if no user is authenticated
     */
    Optional<User> getCurrentUser();

    /**
     * Updates the simulation difficulty preference for a user.
     *
     * @param userId the ID of the user to update
     * @param difficulty the new simulation difficulty level
     * @throws SQLException if a database access error occurs
     */
    void updateSimulationDifficulty(int userId, String difficulty) throws SQLException;

    /**
     * Updates the email address of a user.
     *
     * @param userId the ID of the user to update
     * @param email the new email address
     */
    void updateEmail(int userId, String email);

    /**
     * Updates the username of a user.
     *
     * @param userId the ID of the user to update
     * @param username the new username
     */
    void updateUsername(int userId, String username);

    /**
     * Updates the password hash of a user.
     *
     * @param userId the ID of the user to update
     * @param newPassword the new password hash
     */
    void updatePassword(int userId, String newPassword);

    /**
     * Updates the first and/or last name of a user.
     *
     * @param userId the ID of the user to update
     * @param fName the new first name, or {@code null} to leave unchanged
     * @param lName the new last name, or {@code null} to leave unchanged
     * @throws SQLException if a database access error occurs
     */
    void updateFullName(int userId, String fName, String lName) throws SQLException;
}
