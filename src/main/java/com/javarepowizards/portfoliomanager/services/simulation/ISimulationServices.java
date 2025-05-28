package com.javarepowizards.portfoliomanager.services.simulation;


public interface ISimulationServices {

    PortfolioSimulation buildSimEngine(int days);

    String extractCore(String raw);

    String buildPrompt(PortfolioStatistics.Metrics m, double finalBalance);
}
