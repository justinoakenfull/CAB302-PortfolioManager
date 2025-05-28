package com.javarepowizards.portfoliomanager.services.watchlist;

import com.javarepowizards.portfoliomanager.services.utility.OllamaService;
import com.javarepowizards.portfoliomanager.ui.table.TableRow.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.dao.user.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.watchlist.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.IWatchlistReadOnly;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.User;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Service responsible for managing the current user's watchlist.
 * This class encapsulates all business rules around listing,
 * adding, and removing stocks from the watchlist.  The UI layer
 * merely calls these methods and renders the returned data.
 */
public class WatchlistService implements IWatchlistService, IWatchlistReadOnly {
    private final StockRepository stockRepo;
    private final IWatchlistDAO watchlistDAO;
    private final IUserDAO userDAO;
    private final IPortfolioDAO portfolioDAO;

    private final OllamaService ollamaService;
    private final boolean ollamaAvailable;

    /**
     * Creates a new WatchlistService.
     *
     * @param stockRepo    repository for loading and validating stocks
     * @param watchlistDAO DAO for persisting user watchlist entries
     * @param userDAO      DAO for retrieving the current user
     */
    public WatchlistService(StockRepository stockRepo,
                            IWatchlistDAO watchlistDAO,
                            IUserDAO userDAO,
                            IPortfolioDAO portfolioDAO) {
        this.stockRepo    = stockRepo;
        this.watchlistDAO = watchlistDAO;
        this.userDAO      = userDAO;
        this.portfolioDAO = portfolioDAO;

        // try and init ollama

        OllamaService tmpService;
        boolean available;

        try {
            tmpService = new OllamaService();
            available = true;
        } catch (Exception e)
        {
            tmpService = null;
            available = false;
        }

        this.ollamaAvailable = available;
        this.ollamaService = tmpService;
    }

    /**
     * Returns the list of stocks currently on the user's watchlist.
     * Stocks for which no price history is available are silently skipped.
     * @return list of fully-loaded IStock instances
     * @throws IOException   if there is an error reading stock data
     * @throws SQLException  if there is an error querying the watchlist table
     * @throws IllegalStateException if no user is logged in
     */
    @Override
    public List<IStock> getWatchlist() throws IOException, SQLException {
        int userId = resolveCurrentUserId();

        List<StockName> symbols = watchlistDAO.listForUser(userId);

        Set<String> available = stockRepo.availableTickers();
        List<IStock> result = new ArrayList<>();

        for (StockName sym : symbols) {
            String ticker = sym.getSymbol();
            if (!available.contains(ticker)) {
                continue;
            }

            IStock stock = stockRepo.getByTicker(ticker);
            result.add(stock);
        }
        return result;
    }

    /**
     * Adds the given stock symbol to the current user's watchlist.
     * If the symbol is already present, this is a no-op.
     *
     * @param symbol the StockName to add
     * @throws SQLException if there is an error persisting the new entry
     */
    @Override
    public void addStock(StockName symbol) throws SQLException {
        int userId = resolveCurrentUserId();
        watchlistDAO.addForUser(userId, symbol);
    }

    /**
     * Adds the given stock symbol to the current user's watchlist.
     * If the symbol is already present, this is a no-op.
     *
     * @param Stock the IStock to add
     */
    @Override
    public void addStock(IStock Stock) throws SQLException {
        int userId = resolveCurrentUserId();
        StockName sym = StockName.fromString(Stock.getTicker());
        watchlistDAO.addForUser(userId, sym);
    }

    /**
     * Removes the given stock symbol from the current user's watchlist.
     * If the symbol was not present, this is a no-op.
     *
     * @param symbol the StockName to remove
     * @throws SQLException if there is an error deleting the entry
     * @throws IllegalStateException if no user is logged in
     */
    @Override
    public void removeStock(StockName symbol) throws SQLException {
        int userId = resolveCurrentUserId();
        watchlistDAO.removeForUser(userId, symbol);
    }

    /**
     * Removes the given stock symbol from the current user's watchlist.
     * If the symbol was not present, this is a no-op.
     *
     * @param Stock the StockName to remove
     */
    @Override
    public void removeStock(IStock Stock) {
        int userId = resolveCurrentUserId();

        try {
            StockName sym = StockName.fromString(Stock.getTicker());
            watchlistDAO.removeForUser(userId, sym);
        }
        catch (Exception e) {
            /* Consume exception */
        }
    }

    /**
     * Resolves the current user's numeric ID, or throws if no one is logged in.
     * This might be better to just return 1 for testing but not for now.
     *
     * @return the user ID
     * @throws IllegalStateException if no user is present
     */
    private int resolveCurrentUserId() {
        return userDAO.getCurrentUser()
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalStateException("No user logged in"));
    }

    /**
     * Returns the list of all stock symbols that are not yet in the current user's watchlist.
     * This method fetches the set of symbols the user has already added, then
     * filters out those from the complete enum list.
     *
     * @return a list of StockName values that can still be added to the watchlist
     * @throws SQLException if there is an error querying the watchlist persistence
     */
    @Override
    public List<StockName> getAddableSymbols() throws SQLException {
        Set<StockName> added = new HashSet<>(watchlistDAO.listForUser(resolveCurrentUserId()));
        return Arrays.stream(StockName.values())
                .filter(s -> !added.contains(s))
                .toList();
    }

    /**
     * Provides the short description text for a given stock symbol.
     * This will load the IStock from the repository and return its
     * short description for display in the UI.
     *
     * @param sym the StockName enum whose description should be retrieved
     * @return the stock’s short description text
     * @throws IOException if there is an error loading the stock data
     */
    @Override
    public String getShortDescription(StockName sym) throws IOException {
        IStock stock = stockRepo.getByTicker(sym.getSymbol());

        // If Ollama initialized successfully, try LLM summary first
        if (ollamaAvailable) {
            String prompt = String.format(
                    """
                            Write a company snapshot and summary of %s (%s).
                            It must be 80–100 words total.
                            Your ONLY output must follow this exact format (including tags), with no extra text before or after, in english:
                            <start>
                            [Your summary here]
                            <finish>
                            For example:
                            <start>This is an example summary...<finish>
                            Now generate the real summary.""",
                    stock.getCompanyName(),
                    stock.getTicker()
            );
            try {
                String raw = ollamaService.generateResponse(prompt).trim();
                return extractBetweenTags(raw)
                        .orElseGet(stock::getLongDescription);
            } catch (IOException | IllegalStateException e) {
                // LLM failed; fall back to stored text
                //e.printStackTrace();
            }
        }

        // Default prewritten description
        return stock.getShortDescription();
    }

    /**
     * Provides the long description text for a given stock symbol.
     * This method loads the IStock from the repository and returns its
     * long description for display in the UI.
     *
     * @param sym the StockName enum whose description should be retrieved
     * @return the stock’s long description text
     * @throws IOException if there is an error loading the stock data
     */
    @Override
    public String getLongDescription(StockName sym) throws IOException {
        IStock stock = stockRepo.getByTicker(sym.getSymbol());

        // If Ollama initialized successfully, try LLM generation first
        if (ollamaAvailable) {
            String prompt = String.format(
                    """
                            Write a company snapshot and summary of %s (%s).
                            It must be 150–300 words total.
                            Your ONLY output must follow this exact format (including tags), with no extra text before or after, in english:
                            <start>
                            [Your summary here]
                            <finish>
                            For example:
                            <start>This is an example summary...<finish>
                            Now generate the real summary.""",
                    stock.getCompanyName(),
                    stock.getTicker()
            );
            try {
                String raw = ollamaService.generateResponse(prompt).trim();
                return extractBetweenTags(raw)
                        .orElseGet(stock::getShortDescription);
            } catch (IOException | IllegalStateException e) {
                // LLM failed; fall back to stored text
            }
        }

        // Default prewritten description
        return stock.getLongDescription();
    }

    /**
     * Loads the IStock for the given symbol.
     *
     * @param sym the StockName to load
     * @return the fully-populated IStock
     * @throws IOException if stock data cannot be read
     */
    @Override
    public IStock getStock(StockName sym) throws IOException {
        return stockRepo.getByTicker(sym.getSymbol());
    }

    /**
     * Attempts to extract the substring between the last <start>…<finish> tags.
     *
     * @param text the full text containing one or more pairs of tags
     * @return an Optional containing the trimmed content between the last valid tags,
     *         or Optional.empty() if the tags weren’t found or in the wrong order
     */
    private Optional<String> extractBetweenTags(String text) {
        final String START  = "<start>";
        final String FINISH = "<finish>";

        int end   = text.lastIndexOf(FINISH);
        int start = (end >= 0) ? text.lastIndexOf(START, end - 1) : -1;

        if (start < 0 || start + START.length() > end) {
            return Optional.empty();
        }

        return Optional.of(text.substring(start + START.length(), end).trim());
    }


    /**
     * Returns the raw list of symbols in the user’s watchlist.
     *
     * @return list of StockName values in current watchlist
     * @throws SQLException if there is an error querying the watchlist
     */
    @Override
    public List<StockName> getWatchlistSymbols() throws SQLException {
        return watchlistDAO.listForUser(resolveCurrentUserId());
    }

    /**
     * Formats a raw percent like 1.2345 into “+1.23%” or “-2.50%”.
     *
     * @param pct the raw percent
     * @return formatted percent string
     */
    @Override
    public String formatPercent(double pct) {
        return String.format("%+,.2f%%", pct);
    }

    /**
     * Calculates change % = (close - open) / open * 100 for a given portfolio entry.
     *
     * @param entry the portfolio entry to compute for
     * @return the live change percent
     */
    @Override
    public double computeChangePercent(PortfolioEntry entry) {
        StockName name = StockName.fromString(entry.getStock().getSymbol());
        IStock stock;
        try {
            stock = stockRepo.getByTicker(name.getSymbol());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PriceRecord rec = stock.getCurrentRecord();
        double open  = rec.open();
        double close = rec.close();
        return ((close - open) / open) * 100.0;
    }

    /**
     * Builds WatchlistRow objects for display in the UI table.
     * Stocks without available data are skipped.
     *
     * @return list of WatchlistRow elements
     * @throws IOException  if there is an error loading stock data
     * @throws SQLException if there is an error querying the watchlist
     */
    @Override
    public List<WatchlistRow> getWatchlistRows() throws IOException, SQLException {
        int userId = resolveCurrentUserId();
        List<StockName> symbols = watchlistDAO.listForUser(userId);
        Set<String> available = stockRepo.availableTickers();

        List<WatchlistRow> rows = new ArrayList<>();
        for (StockName sym : symbols) {
            if (!available.contains(sym.getSymbol())) continue;
            IStock stock = stockRepo.getByTicker(sym.getSymbol());
            // Supply remove callback that simply calls back into this service
            Runnable remover = () -> {
                try {
                    removeStock(sym);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            };
            rows.add(new WatchlistRow(stock, remover));
        }
        return rows;
    }

    /**
     * Generates pie chart data for the user's portfolio holdings.
     *
     * @return list of PieChart.Data elements with symbol and market value
     */
    @Override
    public List<PieChart.Data> getPortfolioPieData() {
        List<PortfolioEntry> entries = portfolioDAO.getHoldings();
        return entries.stream()
                .map(e -> new PieChart.Data(e.getStock().getSymbol(), e.getMarketValue()))
                .toList();
    }

    /**
     * Selects four portfolio entries for highlighting based on change criteria:
     * highest gain, closest to zero, smallest non-zero change, and largest loss.
     *
     * @return list of up to four Optional entries in priority order
     */
    @Override
    public List<Optional<PortfolioEntry>> getBalancePicks() {
        List<PortfolioEntry> remaining = new ArrayList<>(portfolioDAO.getHoldings());
        List<Optional<PortfolioEntry>> picks = new ArrayList<>(4);

        Optional<PortfolioEntry> highestPos = remaining.stream()
                .filter(e -> computeChangePercent(e) > 0)
                .max(Comparator.comparingDouble(this::computeChangePercent));
        highestPos.ifPresent(remaining::remove);
        picks.add(highestPos);

        Optional<PortfolioEntry> closestZero = remaining.stream()
                .min(Comparator.comparingDouble(e -> Math.abs(computeChangePercent(e))));
        closestZero.ifPresent(remaining::remove);
        picks.add(closestZero);

        Optional<PortfolioEntry> smallestNonZero = remaining.stream()
                .filter(e -> computeChangePercent(e) != 0)
                .min(Comparator.comparingDouble(e -> Math.abs(computeChangePercent(e))));
        smallestNonZero.ifPresent(remaining::remove);
        picks.add(smallestNonZero);

        Optional<PortfolioEntry> largestNeg = remaining.stream()
                .filter(e -> computeChangePercent(e) < 0)
                .min(Comparator.comparingDouble(this::computeChangePercent));
        picks.add(largestNeg);

        return picks;
    }
}
