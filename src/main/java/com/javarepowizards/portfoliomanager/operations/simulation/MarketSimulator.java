package com.javarepowizards.portfoliomanager.operations.simulation;

import java.time.LocalDate;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketSimulator {

    private final double initialPrice;
    private final double drift;
    private final double volatility;
    private final double momentum;

    // additional params for boundries
    private final double kMultiplier; // e.g. 2 or 3 to set upper/lower bounds
    private final double maxDailyMovement; // maximum allowed daily movement.

    private final Random random; // for generating random shocks

    public MarketSimulator(double initialPrice, double drift, double volatility, double momentum, double kMultiplier, double maxDailyMovement) {
        this.initialPrice = initialPrice;
        this.drift = drift;
        this.volatility = volatility;
        this.momentum = momentum;
        this.kMultiplier = kMultiplier;
        this.maxDailyMovement = maxDailyMovement;
        this.random = new Random();
    }

    /**
     * Simulates the market for a given number of days.
     *
     * @param days The number of days to simulate.
     * @return A list of simulated prices. (starting with initial price
     */
    public List<Double> simulate(int days){

        List<Double> prices = new ArrayList<>();
        double currentPrice = initialPrice;
        prices.add(currentPrice);

        for (int i = 0; i < days; i++) {
            // Generate a random shock from a standard normal distribution.
            double epsilon = random.nextGaussian();

            // For now, we use a static drift; later can update this using dynamic momentum.
            double effectiveDrift = drift; // + momentum adjustment if needed.

            // Calculate the new price using the GBM formula (Δt = 1 day).
            double simulatedFactor = Math.exp((effectiveDrift - (volatility * volatility / 2)) + volatility * epsilon);
            double newPrice = currentPrice * simulatedFactor;

            // Compute dynamic boundaries.
            double upperBound = currentPrice * (1 + kMultiplier * volatility);
            double lowerBound = currentPrice * (1 - kMultiplier * volatility);

            // Enforce boundaries.
            if (newPrice > upperBound) {
                newPrice = upperBound;
            } else if (newPrice < lowerBound) {
                newPrice = lowerBound;
            }

            // Cap maximum daily movement if necessary.
            double maxIncrease = currentPrice * (1 + maxDailyMovement);
            double maxDecrease = currentPrice * (1 - maxDailyMovement);
            if (newPrice > maxIncrease) {
                newPrice = maxIncrease;
            } else if (newPrice < maxDecrease) {
                newPrice = maxDecrease;
            }

            // Update currentPrice and add to list.
            currentPrice = newPrice;
            prices.add(currentPrice);
        }

        return prices;

    }


}
