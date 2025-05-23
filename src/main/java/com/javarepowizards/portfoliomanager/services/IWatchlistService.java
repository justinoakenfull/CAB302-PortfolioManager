package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface IWatchlistService {
    List<IStock> getWatchlist() throws IOException, SQLException;

    void addStock(StockName symbol) throws SQLException;

    void addStock(IStock stock) throws SQLException;

    void removeStock(StockName symbol) throws SQLException;

    void removeStock(IStock stock) throws SQLException;

    List<StockName> getAddableSymbols() throws IOException, SQLException;

    String getShortDescription(StockName symbol) throws IOException;

    String getLongDescription(StockName sym) throws IOException;

    IStock getStock(StockName sym) throws IOException;

    List<StockName> getWatchlistSymbols() throws SQLException;

    String formatPercent(double pct);

    double computeChangePercent(PortfolioEntry entry);
}
