package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.StockDAO;
import com.javarepowizards.portfoliomanager.operations.simulation.PortfolioSimulation;

import java.time.LocalDate;
import java.util.List;


/**
 * SimulationServices manages the business logic for running portfolio simulations.
 * It provides methods to build simulation engines, extract core content from AI responses,
 * and format prompts for AI analysis.
 */
public class SimulationServices implements ISimulationServices{

    private final IPortfolioDAO  portfolioDAO;
    private final StockDAO       stockDAO;
    private final LocalDate      mostRecentDate;

    private static final String PROMPT_TEMPLATE= """
            You are an AI tutor/grader built into a student investment-simulator. After I running the simulation, you use these results:
            
            • Starting balance: $%.2f \s
            • Portfolio holdings:
            %s
            • Portfolio balance: $%.2f \s
            • Sharpe ratio: %.2f \s
            • Volatility: %.2f%% \s
            • Cumulative return: %.2f%% \s
            
            Now respond **exactly** in the structure below, with no extra greetings or commentary, wrapped in <Start> and <Finish> tags:
            
            <Start>
            Performance Summary & Review:
            {Your detailed, creative overview—beyond just stats}
            
            Highlights:
            - {Bullet-point insights}
            
            Tips for Improvement:
            - {Actionable, portfolio-specific advice}
            
            Rating:
            /10 with a brief comment
            <Finish>
            ""\";
            """;


    /**
     * Constructor for SimulationServices.
     *
     * @param portfolioDAO the PortfolioDAO to access portfolio data
     * @param stockDAO the StockDAO to access stock data
     * @param mostRecentDate the most recent date for simulation context (e.g., 2023-12-29)
     */

    public SimulationServices(IPortfolioDAO portfolioDAO,
                             StockDAO stockDAO,
                             LocalDate mostRecentDate) {
        this.portfolioDAO   = portfolioDAO;
        this.stockDAO       = stockDAO;
        this.mostRecentDate = mostRecentDate;

    }


    /**
     * Builds a PortfolioSimulation instance with the specified parameters.
     *
     * @param days the number of days to simulate
     * @return a PortfolioSimulation instance configured with the portfolio, stock data, and simulation parameters
     */

    public PortfolioSimulation buildSimEngine(int days) {
        double k  = 2.0, maxΔ = 0.02, α = 0.3;
        return new PortfolioSimulation(
                portfolioDAO, stockDAO, mostRecentDate,
                days, k, maxΔ, α);
    }

    /**
     * Extracts the core content from a raw string, removing the {@literal <Start> and <Finish>} tags.
     *
     * @param raw the raw string containing the content
     * @return the extracted core content, trimmed of whitespace
     */
    public String extractCore(String raw) {

        String startTag = "<Start>";
        String endTag = "<Finish>";

        int s = raw.indexOf(startTag);
        if (s >= 0){
            String after = raw.substring(s + startTag.length());
            int f = after.indexOf(endTag);
            if (f >= 0){
                return after.substring(0, f).trim();
            }else {
                return after.trim();
            }
        }
        return raw.trim();

    }
    /**
     * Builds a prompt string for the AI tutor/grader, summarizing portfolio statistics and holdings.
     *
     * @param m the portfolio statistics metrics containing cumulative return, annualised volatility, and Sharpe ratio
     * @param finalBalance the final balance of the portfolio after simulation
     * @return a formatted prompt string ready for AI processing
     */

    public String buildPrompt(PortfolioStatistics.Metrics m,
                               double finalBalance) {
        double starting = portfolioDAO.getAvailableBalance()
                + portfolioDAO.getTotalPortfolioValue();

        List<String> rows = portfolioDAO.getHoldings().stream()
                .map(en -> "%s: %d shares @ $%,.2f = $%,.2f".formatted(
                        en.getStock().getSymbol(),
                        (int) en.getAmountHeld(),
                        en.getPurchasePrice(),
                        en.getMarketValue()))
                .toList();

        return PROMPT_TEMPLATE.formatted(
                starting,
                String.join("\n", rows),
                finalBalance,
                m.annualisedSharpe(),
                m.annualisedVolatilityPct(),
                m.cumulativeReturnPct());
    }



}
