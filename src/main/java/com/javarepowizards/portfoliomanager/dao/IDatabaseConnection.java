package com.javarepowizards.portfoliomanager.dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseConnection {
    Connection getConnection() throws SQLException;
}
