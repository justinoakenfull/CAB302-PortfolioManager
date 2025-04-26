package com.javarepowizards.portfoliomanager.dao;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection implements IDatabaseConnection {

    private final Connection connection;

    public DatabaseConnection() throws SQLException {
        String url = "jdbc:sqlite:database.db";
        this.connection = DriverManager.getConnection(url);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
