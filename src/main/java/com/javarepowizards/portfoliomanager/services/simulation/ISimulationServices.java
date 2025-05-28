package com.javarepowizards.portfoliomanager.services.simulation;

/**
 * Defines operations for creating and processing portfolio simulations.
 * Implementations build simulation engines, generate AI prompts based on results,
 * and extract concise summaries from raw AI output.
 */
public interface ISimulationServices {

    PortfolioSimulation buildSimEngine(int days);

    String extractCore(String raw);

    String buildPrompt(PortfolioStatistics.Metrics m, double finalBalance);
}
