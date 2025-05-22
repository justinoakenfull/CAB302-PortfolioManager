package com.javarepowizards.portfoliomanager.dao;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.User;
import java.util.List;
import java.sql.SQLException;
import java.util.Optional;


public interface IUserDAO {
    boolean createUser(User user) throws SQLException;
    Optional<User> getUserByEmail(String email) throws SQLException;
    Optional<User> getUserByUsername(String username) throws SQLException;
    Optional<User> getUserById(int userId) throws SQLException;
    Optional<User> getCurrentUser();

    void updateSimulationDifficulty(int userId, String difficulty) throws SQLException;

    void updateEmail(int userId, String email);
    void updateUsername(int userId, String username);
    void updatePassword(int userId, String newPassword);
    void updateFullName(int userId, String fName, String lName) throws SQLException;


//    // Register a purchase (or top-up an existing holding)
//    void upsertHolding(int userId,
//                       StockName stock,
//                       int quantity,
//                       double totalValue) throws SQLException;
//
//    List<PortfolioEntry> getHoldingsForUser(int userId) throws SQLException;




}
