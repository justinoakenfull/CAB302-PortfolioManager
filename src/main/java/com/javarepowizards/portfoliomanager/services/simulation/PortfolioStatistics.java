package com.javarepowizards.portfoliomanager.services.simulation;



import java.util.Arrays;
import java.util.List;

/**
 * Utility class for computing key portfolio performance metrics
 * from a series of simulated portfolio values.
 */
public class PortfolioStatistics {

    /** Immutable container for the three key metrics. */
    public static record Metrics(double cumulativeReturnPct,
                                 double annualisedVolatilityPct,
                                 double annualisedSharpe) { }

    private PortfolioStatistics() { }    // static helpers only

    /**
     * Compute cumulative return, annualised σ (volatility) and Sharpe.
     *
     * @param values  portfolio value for each simulated day (index 0 = start)
     * @param days    number of simulated days (slider value)
     */
    public static Metrics compute(List<Double> values, int days) {
        if (values.size() < 2) {
            return new Metrics(0, 0, 0);
        }

        double start = values.get(0);
        double end   = values.get(values.size() - 1);
        double cumRet = (end - start) / start * 100.0;

        double[] daily = new double[values.size() - 1];
        for (int i = 1; i < values.size(); i++) {
            daily[i - 1] = (values.get(i) - values.get(i - 1)) / values.get(i - 1);
        }

        double mean = Arrays.stream(daily).average().orElse(0);
        double var  = Arrays.stream(daily)
                .map(r -> (r - mean) * (r - mean))
                .average().orElse(0);
        double σ      = Math.sqrt(var);
        double annVol = σ * Math.sqrt(days) * 100.0;      // %
        double annSharpe = (mean * days) / σ;

        return new Metrics(cumRet, annVol, annSharpe);
    }
    }







