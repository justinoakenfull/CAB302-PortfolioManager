package com.javarepowizards.portfoliomanager.services.watchlist;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.ui.table.TableRow.WatchlistRow;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing the user’s watchlist and related portfolio data.
 * Provides methods to list, add, and remove stocks from the watchlist,
 * retrieve available symbols, generate descriptions, and produce UI data.
 */
public interface IWatchlistService {

    List<IStock> getWatchlist() throws SQLException, IOException;

    void addStock(StockName symbol) throws SQLException;

    void addStock(IStock stock) throws SQLException;

    void removeStock(StockName symbol) throws SQLException;

    void removeStock(IStock stock) throws SQLException;

    List<StockName> getAddableSymbols() throws SQLException;

    String getShortDescription(StockName symbol) throws IOException;

    String getLongDescription(StockName sym) throws IOException;

    IStock getStock(StockName sym) throws IOException;

    List<StockName> getWatchlistSymbols() throws SQLException;

    String formatPercent(double pct);

    double computeChangePercent(PortfolioEntry entry);

    List<WatchlistRow> getWatchlistRows() throws IOException, SQLException;

    List<PieChart.Data> getPortfolioPieData() throws SQLException;

    List<Optional<PortfolioEntry>> getBalancePicks() throws SQLException;
}
