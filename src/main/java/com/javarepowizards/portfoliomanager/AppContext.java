package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.*;
import com.javarepowizards.portfoliomanager.domain.IStockRepoReadOnly;
import com.javarepowizards.portfoliomanager.domain.IWatchlistReadOnly;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.infrastructure.InMemoryStockRepository;
import com.javarepowizards.portfoliomanager.services.AuthService;
import com.javarepowizards.portfoliomanager.services.IAuthService;
import com.javarepowizards.portfoliomanager.services.IWatchlistService;
import com.javarepowizards.portfoliomanager.services.WatchlistService;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central service locator for application-wide dependency injection.
 * Manages registration and retrieval of core services such as
 * database connections, DAOs, repositories, and business services.
 */
public final class AppContext {
    private AppContext() {} // Prevent instantiation

    /**
     * Initializes all core application services in the correct order.
     * This includes database services, stock repository, watchlist services,
     * and authentication service.
     *
     * @throws SQLException if database initialization fails
     * @throws IOException if stock data files cannot be read
     * @throws URISyntaxException if resource URIs are malformed
     * @throws CsvValidationException if CSV parsing fails
     */
    public static void initAll()
            throws SQLException, IOException, URISyntaxException, CsvValidationException
    {
        initializeDatabaseServices();
        initializeStockRepository();
        initializeWatchlist();
        initializeAuthService();
    }

    /**
     * Registry mapping service interface classes to their implementations.
     */
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Registers a service instance under the specified interface or superclass.
     * Ensures that no duplicate registrations occur.
     *
     * @param type the service interface or superclass
     * @param instance the concrete implementation to register
     * @param <T> the type of the service
     * @throws IllegalStateException if a service is already registered for the given type
     */
    public static <T> void registerService(Class<T> type, T instance) {
        if (services.containsKey(type)) {
            throw new IllegalStateException(type.getSimpleName() + " already registered");
        }
        services.put(type, instance);
    }

    /**
     * Retrieves a previously registered service by its interface or superclass.
     *
     * @param type the service interface or superclass
     * @param <T> the type of the service
     * @return the registered service implementation
     * @throws IllegalStateException if no service is registered for the given type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getSimpleName());
        }
        return (T) service;
    }

    /**
     * Registers a custom StockRepository instance for use by the application.
     *
     * @param repo the StockRepository to register
     */
    public static void initStockRepository(StockRepository repo) {
        registerService(StockRepository.class, repo);
    }

    /**
     * Retrieves the registered StockRepository instance.
     *
     * @return the StockRepository used by the application
     */
    public static StockRepository getStockRepository() {
        return getService(StockRepository.class);
    }

    /**
     * Retrieves the registered IWatchlistDAO instance.
     *
     * @return the IWatchlistDAO implementation
     */
    public static IWatchlistDAO getWatchlistDAO() {
        return getService(IWatchlistDAO.class);
    }

    /**
     * Retrieves the registered IUserDAO instance.
     *
     * @return the IUserDAO implementation
     */
    public static IUserDAO getUserDAO() {
        return getService(IUserDAO.class);
    }

    /**
     * Creates and registers database-related services:
     * the JDBC connection, IUserDAO, and IPortfolioDAO.
     *
     * @throws SQLException if connection or DAO initialization fails
     */
    private static void initializeDatabaseServices() throws SQLException {
        // 1) DB connection, user DAO
        IDatabaseConnection dbConnection = new DatabaseConnection();
        AppContext.registerService(IDatabaseConnection.class, dbConnection);

        // Initialize UserDAO since other services might depend on it
        IUserDAO userDAO = new UserDAO(dbConnection);
        AppContext.registerService(IUserDAO.class, userDAO);


        // register the PortfolioDAO under its interface
        PortfolioDAO portfolioDAO = new PortfolioDAO(dbConnection);
        AppContext.registerService(IPortfolioDAO.class, portfolioDAO);
        System.out.println("DB connection: " + dbConnection.getConnection());
    }


    /**
     * Creates and registers the authentication service using BCrypt.
     */
    private static void initializeAuthService() {
        PasswordEncoder pwEncoder = new BCryptPasswordEncoder();
        IAuthService authService = new AuthService(pwEncoder);
        AppContext.registerService(IAuthService.class, authService);
    }

    /**
     * Loads stock data from CSV files, initializes the in-memory repository,
     * and registers both StockRepository and StockDAO instances.
     *
     * @throws URISyntaxException if resource URIs are malformed
     * @throws CsvValidationException if CSV parsing fails
     * @throws IOException if reading the CSV files fails
     */
    private static void initializeStockRepository() throws URISyntaxException, CsvValidationException, IOException {
        URL priceCsvUrl = AppContext.class.getResource("/com/javarepowizards/portfoliomanager/data/asx_data_with_index2.csv");
        URL descUrl   = AppContext.class.getResource("/com/javarepowizards/portfoliomanager/data/descriptions.csv");
        if (priceCsvUrl == null || descUrl == null) {
            throw new IllegalStateException("CSV file(s) not found");
        }
        Path csvPath = Paths.get(priceCsvUrl.toURI());
        Path csvDescPath = Paths.get(descUrl.toURI());
        StockRepository repo = new InMemoryStockRepository(csvPath, csvDescPath);
        AppContext.initStockRepository(repo);

        StockDAO stockDAO = StockDAO.getInstance();
        AppContext.registerService(StockDAO.class, stockDAO);

        AppContext.registerService(IStockRepoReadOnly.class, repo);
    }

    /**
     * Creates and registers watchlist-related services:
     * IWatchlistDAO and IWatchlistService.
     *
     * @throws SQLException if DAO initialization fails
     */
    private static void initializeWatchlist() throws SQLException {
        //construct & register the DAO
        IWatchlistDAO watchlistDAO = new WatchlistDAO(AppContext.getService(IDatabaseConnection.class));
        AppContext.registerService(IWatchlistDAO.class, watchlistDAO);

        //construct & register the application service
        WatchlistService watchlistService =
                new WatchlistService(
                        AppContext.getService(StockRepository.class),
                        watchlistDAO,
                        AppContext.getService(IUserDAO.class),
                        AppContext.getService(IPortfolioDAO.class));
        AppContext.registerService(IWatchlistService.class, watchlistService);

        AppContext.registerService(IWatchlistReadOnly.class,watchlistService);
    }
}