package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.dao.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Service responsible for managing the current user's watchlist.
 * This class encapsulates all business rules around listing,
 * adding, and removing stocks from the watchlist.  The UI layer
 * merely calls these methods and renders the returned data.
 */
public class WatchlistService implements IWatchlistService {
    private final StockRepository stockRepo;
    private final IWatchlistDAO watchlistDAO;
    private final IUserDAO userDAO;

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
                            IUserDAO userDAO) {
        this.stockRepo    = stockRepo;
        this.watchlistDAO = watchlistDAO;
        this.userDAO      = userDAO;

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
                System.err.println("No CSV history for " + ticker + ", skipping");
                continue;
            }

            // 3. load the domain object
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
     * @throws IllegalStateException if no user is logged in
     */
    @Override
    public void addStock(StockName symbol) throws SQLException {
        int userId = resolveCurrentUserId();
        watchlistDAO.addForUser(userId, symbol);
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
     * Resolves the current user's numeric ID, or throws if no one is logged in.
     * This might be better to just return 1 for testing but not for now.
     *
     * @return the user ID
     * @throws IllegalStateException if no user is present
     */
    private int resolveCurrentUserId() {
        return Optional.ofNullable(userDAO.getCurrentUser()
                        .orElseThrow(() -> new IllegalStateException("No user logged in")))
                .map(User::getUserId)
                .get();
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
                return extractBetweenTags(raw);
            } catch (IOException e) {
                // LLM failed; fall back to stored text
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
                return extractBetweenTags(raw);
            } catch (IOException e) {
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
     * Extracts and returns the substring between the first occurrence of
     * the markers "<start>" and "<finish>" in the given text.
     *
     * @param text the full text containing the tags
     * @return the trimmed content between the start and finish tags
     * @throws IllegalArgumentException if either tag is missing or in the wrong order
     */
    private String extractBetweenTags(String text) {
        final String START = "<start>";
        final String FINISH = "<finish>";

        System.out.println(text);
        int i1 = text.indexOf(START);
        int i2 = text.indexOf(FINISH);

        if (i1 < 0 || i2 < 0 || i1 + START.length() > i2) {
            throw new IllegalArgumentException(
                    "Text does not contain valid <start>…<finish> markers"
            );
        }

        return text.substring(i1 + START.length(), i2).trim();
    }
}
