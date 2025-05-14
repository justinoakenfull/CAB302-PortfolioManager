package com.javarepowizards.portfoliomanager.operations.simulation;

import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.PortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class PortfolioSimulationTest {

    @Test
    void simulatePortfolio_emptyHoldings_returnsConstantBalanceSeries() {
        // GIVEN an empty portfolio with a cash balance of 5,000
        double initialCash = 5_000.0;
        PortfolioDAO emptyPortfolio = new PortfolioDAO(new ArrayList<>(), initialCash);

        // AND a “simulation engine” configured with zero-day parameters
        // (we only care about aggregation logic, so drift/volatility/momentum don’t matter)
        int simulationDays = 7; // simulate one week
        double kMultiplier      = 0.0;
        double maxDailyMovement = 0.0;
        double smoothingFactor  = 0.0;
        LocalDate dummyDate = null; // not used when no holdings
        StockDAO stockDAO   = null;

        PortfolioSimulation engine = new PortfolioSimulation(
                emptyPortfolio,
                stockDAO,
                dummyDate,
                simulationDays,
                kMultiplier,
                maxDailyMovement,
                smoothingFactor
        );

        // WHEN we run the simulation
        List<Double> results = engine.simulatePortfolio();

        // THEN we should get simulationDays + 1 entries (day 0 through day 7),
        // and each entry equals the original cash balance (no holdings to change it).
        assertEquals(simulationDays + 1, results.size(),
                "Result length should be days+1");
        results.forEach(value ->
                assertEquals(initialCash, value, 1e-9,
                        "Each day's portfolio value should equal initial cash")
        );
    }
}