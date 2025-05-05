package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.StockName;
import java.sql.SQLException;
import java.util.List;

public interface IWatchlistDAO {
    List<StockName> listForUser(int userId) throws SQLException;
    void addForUser(int userId, StockName symbol) throws SQLException;
    void removeForUser(int userId, StockName symbol) throws SQLException;
}