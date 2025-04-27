package com.javarepowizards.portfoliomanager;

import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple service locator for dependency injection.
 * Now supports interface-based registration.
 */
public final class AppContext {
    private AppContext() {} // Prevent instantiation

    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Registers a service under its interface/superclass.
     * @param type The interface/abstract type (e.g., IWatchlistDAO.class)
     * @param instance The concrete implementation (e.g., WatchlistDAO)
     * @throws IllegalStateException If the type is already registered.
     */
    public static <T> void registerService(Class<T> type, T instance) {
        if (services.containsKey(type)) {
            throw new IllegalStateException(type.getSimpleName() + " already registered");
        }
        services.put(type, instance);
    }

    /**
     * Retrieves a service by its interface/superclass.
     * @throws IllegalStateException If the service isn't registered.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getSimpleName());
        }
        return (T) service;
    }

    // --- Convenience Methods (Optional) ---
    public static void initStockRepository(StockRepository repo) {
        registerService(StockRepository.class, repo);
    }

    public static StockRepository getStockRepository() {
        return getService(StockRepository.class);
    }

    // New helper for watchlist (optional)
    public static IWatchlistDAO getWatchlistDAO() {
        return getService(IWatchlistDAO.class);
    }
}