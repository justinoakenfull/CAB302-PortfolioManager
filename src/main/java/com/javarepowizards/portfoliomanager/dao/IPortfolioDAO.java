package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;

import java.sql.SQLException;
import java.util.List;

public interface IPortfolioDAO {

    List<PortfolioEntry> getHoldings();
    double getAvailableBalance();
    void addToHoldings(PortfolioEntry entry);
    double getTotalPortfolioValue();
    void upsertHolding(int userId, StockName stock, int quantity, double totalValue);
    List<PortfolioEntry> getHoldingsForUser(int userId) throws SQLException;

}
