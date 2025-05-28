package com.javarepowizards.portfoliomanager.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides a connection to the SQLite database.
 * Initializes a single JDBC connection on construction
 * and exposes it via the IDatabaseConnection interface.
 */
public interface IDatabaseConnection {

    /**
     * Creates a DatabaseConnection by opening a JDBC connection
     * to the local SQLite database file.
     *
     * @throws SQLException if a database access error occurs
     */
    Connection getConnection() throws SQLException;
}
