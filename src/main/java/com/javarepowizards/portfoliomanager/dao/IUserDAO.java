package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.User;

import java.sql.SQLException;
import java.util.Optional;

public interface IUserDAO {
    boolean createUser(User user) throws SQLException;
    Optional<User> getUserByEmail(String email) throws SQLException;
    Optional<User> getUserByUsername(String username) throws SQLException;
    Optional<User> getUserById(int userId) throws SQLException;

    void updateSimulationDifficulty(int userId, String difficulty) throws SQLException;

    void updateEmail(int userId, String email);
    void updateUsername(int userId, String username);
    void updatePassword(int userId, String newPassword);

    Optional<User> getCurrentUser();
}
