package com.javarepowizards.portfoliomanager.services.simulation;

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
    private final double smoothingFactor; // for updating momentum dynamically.

    private final Random random; // for generating random shocks

    public MarketSimulator(double initialPrice, double drift, double volatility, double momentum, double kMultiplier, double maxDailyMovement, double smoothingFactor) {
        this.initialPrice = initialPrice;
        this.drift = drift;
        this.volatility = volatility;
        this.momentum = momentum;
        this.kMultiplier = kMultiplier;
        this.maxDailyMovement = maxDailyMovement;
        this.smoothingFactor = smoothingFactor;
        this.random = new Random();
    }

    /**
     * Simulates the market for a given number of days.
     *
     * @param days The number of days to simulate.
     * @return A list of simulated prices. (starting with initial price
     */
    public List<Double> simulate(int days) {
        List<Double> prices = new ArrayList<>();
        double currentPrice = initialPrice;
        prices.add(currentPrice);

        // Initialize dynamic momentum with the baseline value.
        double currentMomentum = momentum;


        // For each day, compute the next price.
        for (int i = 0; i < days; i++) {
            // Generate a random shock from a standard normal distribution.
            double epsilon = random.nextGaussian();

            // Effective drift is the sum of the historical drift and the dynamic momentum.
            double effectiveDrift = (drift * 0.3) + currentMomentum;

            // mean Reversion
           // double meanReversion = Math.log(initialPrice / currentPrice) * 0.04; // Adjust 0.01 as needed
           // effectiveDrift += meanReversion;

            // Use the GBM-style formula to calculate the new price.
            double simulatedFactor = Math.exp((effectiveDrift - (volatility * volatility / 2)) + volatility * epsilon);
            double newPrice = currentPrice * simulatedFactor;

            // Enforce maximum daily movement limits.
            double maxIncrease = currentPrice * (1 + maxDailyMovement);
            double maxDecrease = currentPrice * (1 - maxDailyMovement);
            newPrice = Math.min(maxIncrease, Math.max(maxDecrease, newPrice));

            // Calculate dynamic boundaries based on current price and volatility.
            double upperBound = currentPrice * (1 + kMultiplier * volatility);
            double lowerBound = currentPrice * (1 - kMultiplier * volatility);
            newPrice = Math.min(upperBound, Math.max(lowerBound, newPrice));

            // Calculate today’s return.
            double todayReturn = (newPrice - currentPrice) / currentPrice;

            // Update dynamic momentum using a smoothing function.
            currentMomentum = (1 - smoothingFactor) * currentMomentum + smoothingFactor * todayReturn;
            currentMomentum = Math.max(-0.005, Math.min(0.005, currentMomentum));
            currentMomentum *= 0.9; // momentum decay by 10% each day

            // Set the current price to the new price.
            currentPrice = newPrice;
            prices.add(currentPrice);
        }

        return prices;
    }


}
