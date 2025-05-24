package com.javarepowizards.portfoliomanager.services;


import com.javarepowizards.portfoliomanager.operations.simulation.PortfolioSimulation;

public interface ISimulationServices {

    PortfolioSimulation buildSimEngine(int days);

    String extractCore(String raw);

    String buildPrompt(PortfolioStatistics.Metrics m, double finalBalance);
}
