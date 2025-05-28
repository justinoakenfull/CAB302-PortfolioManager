package com.javarepowizards.portfoliomanager.dao;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a connection to the SQLite database.
 * Initializes a single JDBC connection on construction
 * and exposes it via the IDatabaseConnection interface.
 */
@Component
public class DatabaseConnection implements IDatabaseConnection {

    private final Connection connection;

    /**
     * Creates a DatabaseConnection by opening a JDBC connection
     * to the local SQLite database file.
     *
     * @throws SQLException if a database access error occurs
     */
    public DatabaseConnection() throws SQLException {
        String url = "jdbc:sqlite:database.db";
        this.connection = DriverManager.getConnection(url);
    }

    /**
     * Returns the active JDBC Connection instance.
     *
     * @return the JDBC Connection to the SQLite database
     */
    @Override
    public Connection getConnection() {
        return connection;
    }
}
