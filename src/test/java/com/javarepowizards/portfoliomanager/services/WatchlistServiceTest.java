package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.*;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.infrastructure.InMemoryStockRepository;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.User;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WatchlistServiceTest {

    private IWatchlistService watchlistService;
    private IWatchlistDAO     watchlistDAO;
    private IUserDAO          userDAO;
    private IPortfolioDAO     portfolioDAO;
    private Connection        connection;
    private StockRepository   stockRepo;

    @BeforeAll
    void initAll() throws SQLException, URISyntaxException, IOException, CsvValidationException, NoSuchFieldException, IllegalAccessException {

        // Clear pre-registered services
        Field servicesField = AppContext.class.getDeclaredField("services");
        servicesField.setAccessible(true);
        Map<?,?> svcMap = (Map<?,?>) servicesField.get(null);
        svcMap.clear();

        var dbConn = new DatabaseConnection();
        AppContext.registerService(IDatabaseConnection.class, dbConn);
        connection = dbConn.getConnection();

        userDAO = new UserDAO(dbConn);
        AppContext.registerService(IUserDAO.class, userDAO);

        watchlistDAO = new WatchlistDAO(dbConn);
        AppContext.registerService(IWatchlistDAO.class, watchlistDAO);

        portfolioDAO = new PortfolioDAO(dbConn);
        AppContext.registerService(IPortfolioDAO.class, portfolioDAO);

        URL priceCsv = getClass().getResource(
                "/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv"
        );
        URL descCsv = getClass().getResource(
                "/com/javarepowizards/portfoliomanager/data/descriptions.csv"
        );
        Path pricePath = Paths.get(priceCsv.toURI());
        Path descPath = Paths.get(descCsv.toURI());
        stockRepo = new InMemoryStockRepository(pricePath, descPath);
        AppContext.initStockRepository(stockRepo);

        WatchlistService svc = new WatchlistService(stockRepo, watchlistDAO, userDAO, portfolioDAO);
        AppContext.registerService(IWatchlistService.class, svc);
        watchlistService = AppContext.getService(IWatchlistService.class);
    }

    @BeforeEach
    void cleanAndLogin() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM user_watchlist");
            stmt.executeUpdate("DELETE FROM user_auth");
        }
        User u = new User("test-user", "test@example.com", "passwordHash");
        userDAO.createUser(u);
        Session.setCurrentUser(u);
    }

    @Test
    @DisplayName("New user has empty watchlist")
    void emptyWatchlist() throws SQLException, IOException {
        assertTrue(watchlistService.getWatchlist().isEmpty());
        assertTrue(watchlistService.getWatchlistSymbols().isEmpty());
    }

    @Test
    @DisplayName("addStock(StockName) persists via DAO")
    void addBySymbol() throws SQLException, IOException {

        StockName sym = StockName.values()[0];
        watchlistService.addStock(sym);

        List<StockName> stored = watchlistDAO.listForUser(
                Session.getCurrentUser().getUserId()
        );
        assertEquals(1, stored.size());
        assertEquals(sym, stored.get(0));
    }

    @Test
    @DisplayName("getWatchlist() loads domain stocks for saved tickers")
    void getWatchlistLoadsStocks() throws SQLException, IOException {
        StockName sym = StockName.values()[1];
        watchlistDAO.addForUser(Session.getCurrentUser().getUserId(), sym);

        List<IStock> list = watchlistService.getWatchlist();
        assertEquals(1, list.size());

        assertEquals(sym.getSymbol(), list.getFirst().getTicker());
    }

    @Test
    @DisplayName("addStock(IStock) & removeStock(IStock)")
    void addRemoveByIStock() throws SQLException, IOException {
        Set<String> avail = stockRepo.availableTickers();
        assertFalse(avail.isEmpty(), "CSV repo must have at least one ticker");

        String ticker = avail.iterator().next();
        IStock stock = stockRepo.getByTicker(ticker);
        StockName sym = StockName.fromString(ticker);

        watchlistService.addStock(stock);
        assertTrue(watchlistDAO.listForUser(Session.getCurrentUser().getUserId()).contains(sym));

        watchlistService.removeStock(stock);
        assertFalse(watchlistDAO.listForUser(Session.getCurrentUser().getUserId()).contains(sym));
    }

    @Test
    @DisplayName("removeStock(StockName) deletes entry")
    void removeBySymbol() throws SQLException {
        StockName sym = StockName.values()[2];
        watchlistService.addStock(sym);
        watchlistService.removeStock(sym);

        assertTrue(watchlistDAO.listForUser(Session.getCurrentUser().getUserId()).isEmpty());
    }

    @Test
    @DisplayName("getAddableSymbols() excludes those already added")
    void getAddableSymbolsFilters() throws SQLException, IOException {
        StockName sym = StockName.values()[3];
        watchlistService.addStock(sym);

        var addable = watchlistService.getAddableSymbols();
        assertFalse(addable.contains(sym));
        assertEquals(StockName.values().length - 1, addable.size());
    }

    @Test
    @DisplayName("Methods throw when no user is logged in")
    void throwsWhenNoUser() throws SQLException {
        Session.setCurrentUser(null);
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM user_auth");
        }

        assertAll(
                () -> assertThrows(NullPointerException.class, watchlistService::getWatchlist),
                () -> assertThrows(NullPointerException.class,
                        () -> watchlistService.addStock(StockName.BHP_AX)),
                () -> assertThrows(NullPointerException.class,
                        () -> watchlistService.removeStock(StockName.BHP_AX)),
                () -> assertThrows(NullPointerException.class,
                        watchlistService::getWatchlistSymbols),
                () -> assertThrows(NullPointerException.class,
                        () -> watchlistService.getAddableSymbols())
        );
    }
}
