package com.javarepowizards.portfoliomanager.services.simulation;


import com.javarepowizards.portfoliomanager.dao.stock.IStockDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.services.utility.StockDataFilter;

import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PortfolioSimulationEngine aggregates simulations for all stocks in a portfolio.
 * For each portfolio holding, it:
 *  - Retrieves historical data using StockDAO,
 *  - Filters the data for the last year (using a given mostRecentDate),
 *  - Computes unique simulation parameters (drift, volatility, momentum) from StockStatistics,
 *  - Creates a SimulationEngine for that stock and simulates its price path,
 *  - Finally, it aggregates the daily simulated values (multiplied by shares held)
 *    along with available cash from the PortfolioDAO.
 */
public class PortfolioSimulation {

    private final IPortfolioDAO portfolio;
    private final IStockDAO stockDAO;          // Needed to fetch historical data for each stock
    private final LocalDate mostRecentDate;   // e.g., 2023-12-29
    private final int simulationDays;         // Number of simulation days to run
    private final double kMultiplier;         // Multiplier for dynamic boundaries (e.g., 2.0)
    private final double maxDailyMovement;    // Maximum allowed daily movement (e.g., 0.05 for ±5%)
    private final double smoothingFactor;     // Smoothing factor for dynamic momentum updates

    /**
     * Constructor for PortfolioSimulationEngine.
     *
     * @param portfolio the PortfolioDAO containing holdings and available cash.
     * @param stockDAO  the StockDAO to retrieve historical stock data.
     * @param mostRecentDate the most recent date (used to filter historical data, e.g., 2023-12-29).
     * @param simulationDays the number of days to simulate.
     * @param kMultiplier multiplier for dynamic boundaries.
     * @param maxDailyMovement maximum daily movement as a decimal.
     * @param smoothingFactor smoothing factor (α) for dynamic momentum updates.
     */
    public PortfolioSimulation(IPortfolioDAO portfolio, IStockDAO stockDAO, LocalDate mostRecentDate,
                                     int simulationDays, double kMultiplier, double maxDailyMovement,
                                     double smoothingFactor) {
        this.portfolio = portfolio;
        this.stockDAO = stockDAO;
        this.mostRecentDate = mostRecentDate;
        this.simulationDays = simulationDays;
        this.kMultiplier = kMultiplier;
        this.maxDailyMovement = maxDailyMovement;
        this.smoothingFactor = smoothingFactor;
    }

    /**
     * Simulates the portfolio’s total value over the set simulation period.
     *
     * For each portfolio entry:
     *  - Its unique historical data is retrieved and filtered to the last year.
     *  - Unique simulation parameters are computed.
     *  - A SimulationEngine instance is created to simulate that stock's price path.
     * Finally, the portfolio value is aggregated day-by-day, incorporating available cash.
     *
     * @return a list of total portfolio values for each simulation day (starting with day 0).
     */
    public List<Double> simulatePortfolio() {
        List<List<Double>> individualSimulations = new ArrayList<>();

        // For each stock in the portfolio, compute its unique simulation parameters and simulate its price path.
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            // Retrieve all historical data for the stock.
            List<StockData> allData = stockDAO.getStockData(entry.getStock());
            // Filter the data for the last year (e.g., from mostRecentDate.minusYears(1) to mostRecentDate).
            List<StockData> filteredData = new StockDataFilter().getDataFromLastYear(allData, mostRecentDate);
            // Compute unique simulation parameters using StockStatistics.
            StockStatistics stats = new StockStatistics(filteredData);
            // Get the latest closing price for the stock at the most recent date.
            StockData latestData = stockDAO.getStockData(entry.getStock(), mostRecentDate);
            double initialPrice = latestData.getClose();
            // Create a SimulationEngine for this stock with its unique parameters.
            MarketSimulator engine = new MarketSimulator(
                    initialPrice,
                    stats.getAverageDailyReturn(),  // Unique drift.
                    stats.getVolatility(),            // Unique volatility.
                    stats.getMomentum(),              // Unique baseline momentum.
                    kMultiplier,
                    maxDailyMovement,
                    smoothingFactor                   // Smoothing factor for dynamic momentum updates.
            );
            // Run the simulation for the specified number of days.
            List<Double> simPrices = engine.simulate(simulationDays);
            individualSimulations.add(simPrices);
        }

        //  aggregate the simulated prices of each stock into a portfolio value per day.
        List<Double> portfolioValues = new ArrayList<>();

        // Calculate Day 0 portfolio value (cash + each stock's initial value).
        double initialPortfolioValue = portfolio.getAvailableBalance();
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            initialPortfolioValue += entry.getPurchasePrice() * entry.getAmountHeld();
        }
        portfolioValues.add(initialPortfolioValue);

        // For each simulation day (1 to simulationDays), aggregate individual stock values.
        for (int day = 1; day <= simulationDays; day++) {
            double dayPortfolioValue = portfolio.getAvailableBalance(); // Cash remains constant.
            for (int i = 0; i < portfolio.getHoldings().size(); i++) {
                PortfolioEntry entry = portfolio.getHoldings().get(i);
                List<Double> simPrices = individualSimulations.get(i);
                // Each simulation returns simulationDays+1 entries (day 0 is initial price).
                double simulatedPrice = simPrices.get(day);
                dayPortfolioValue += simulatedPrice * entry.getAmountHeld();
            }
            portfolioValues.add(dayPortfolioValue);
        }

        return portfolioValues;
    }
}
