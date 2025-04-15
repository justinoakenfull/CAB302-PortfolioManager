package com.javarepowizards.portfoliomanager.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection instance = null;

    private DatabaseConnection() throws SQLException {
        String url = "jdbc:mysql:database.db";
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException sqlEx) {
            System.err.println(sqlEx);
        }
    }

    public static Connection getInstance() {
        if (instance == null) {
            try {
                new DatabaseConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
}