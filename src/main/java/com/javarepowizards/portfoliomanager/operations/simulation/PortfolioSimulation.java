package com.javarepowizards.portfoliomanager.operations.simulation;

import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import java.util.ArrayList;
import java.util.List;

public class PortfolioSimulation {

    private final PortfolioDAO portfolio;
    private final int simulationDays;
    private final double kMultiplier;      // e.g., 2.0 to set upper/lower dynamic boundaries
    private final double maxDailyMovement; // Maximum allowed daily movement (e.g., 0.05 for ±5%)

    /**
     * Constructor for PortfolioSimulation.
     *
     * @param portfolio         The portfolio to simulate.
     * @param simulationDays    The number of days to simulate.
     * @param kMultiplier       Multiplier for dynamic boundaries.
     * @param maxDailyMovement  Maximum allowed daily movement.
     */
    public PortfolioSimulation(PortfolioDAO portfolio, int simulationDays, double kMultiplier, double maxDailyMovement) {
        this.portfolio = portfolio;
        this.simulationDays = simulationDays;
        this.kMultiplier = kMultiplier;
        this.maxDailyMovement = maxDailyMovement;
    }

    public List<Double> simulatePortfolio(double drift, double volatility, double momentum){

        // List to hold the simulated price paths for each stock in the portfolio.
        List<List<Double>> individualSimulations = new ArrayList<>();

        // For each portfolio entry, simulate its price path.
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            // Use purchasePrice as the initial price
            double initialPrice = entry.getPurchasePrice();
            MarketSimulator engine = new MarketSimulator(initialPrice, drift, volatility, momentum, kMultiplier, maxDailyMovement);
            List<Double> simulationPrices = engine.simulate(simulationDays);
            individualSimulations.add(simulationPrices);
        }

        // aggregate the portfolio value for each simulated day.
        List<Double> portfolioValues = new ArrayList<>();

        // Calculate initial portfolio value (day 0) which is available cash + sum(value of each stock)
        double initialPortfolioValue = portfolio.getAvailableBalance();
        for (PortfolioEntry entry : portfolio.getHoldings()) {
            initialPortfolioValue += entry.getAmountHeld() * entry.getPurchasePrice();
        }
        portfolioValues.add(initialPortfolioValue);

        // For each simulation day (from 1 to simulationDays), aggregate the current value of each stock.
        for (int day = 1; day < simulationDays + 1; day++) {
            double dayPortfolioValue = portfolio.getAvailableBalance(); // available cash remains constant
            for (int j = 0; j < portfolio.getHoldings().size(); j++) {
                PortfolioEntry entry = portfolio.getHoldings().get(j);
                List<Double> simPrices = individualSimulations.get(j);
                // Each simulation should produce simulationDays+1 entries (day 0 is initial price)
                double simulatedPrice = simPrices.get(day);
                dayPortfolioValue += simulatedPrice * entry.getAmountHeld();
            }
            portfolioValues.add(dayPortfolioValue);
        }

        return portfolioValues;
    }
}
