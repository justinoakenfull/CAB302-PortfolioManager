package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection connection;

    public void UserDAO() {
        connection = DatabaseConnection.getInstance();
    }

    public void createTable() throws SQLException {
        try {
            Statement createTable = connection.createStatement();
            createTable.execute(
                    "CREATE TABLE IF NOT EXISTS user_auth ("
                    + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username VARCHAR(24) UNIQUE NOT NULL,"
                    + "email VARCHAR(255) UNIQUE NOT NULL,"
                    + "password_hash VARCHAR(255) NOT NULL,"
                    + "salt VARCHAR(255) NOT NULL"
                    + ")"
                    + "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT PRIMARY KEY,"
                    + "username VARCHAR(24) UNIQUE NOT NULL,"
                    + "email VARCHAR(255) UNIQUE NOT NULL,"
                    + "phone BIGINT,"
                    + "first_name VARCHAR(255),"
                    + "last_name VARCHAR(255),"
                    + "FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE"
                    + ")"
                    + "CREATE TABLE IF NOT EXISTS user_balances ("
                    + "user_id INT PRIMARY KEY,"
                    + "balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,"
                    + "currency VARCHAR(3) DEFAULT 'AUD',"
                    + "FOREIGN KEY (user_id) REFERENCES user_auth(user_id) ON DELETE CASCADE"
            );
        } catch (SQLException ex) {
            System.err.println(ex);
        }
    }

    public void CreateUser(User user) {
        try {
            PreparedStatement createUser = connection.prepareStatement(
                    "INSERT INTO user_auth (username, email, password_hash, salt) VALUES (?, ?, ?, ?)"
            );
            createUser.setString(1, user.getUsername());
            createUser.setString(2, user.getEmail());
            createUser.setString(3, user.getPasswordHash();
            createUser.setString(4, user.getSalt());
            createUser.execute();
        } catch (SQLException ex) {
            System.err.print(ex);
        }
    }

    public void update(User user) {
        return;
    }

    public void delete(int userId) {
        return;
    }

    public void close() throws SQLException {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e);
        }
    }
}