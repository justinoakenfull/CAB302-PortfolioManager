package com.javarepowizards.portfoliomanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple application context (service locator) for sharing common services.
 * <p>
 * Initialize once at startup, then retrieve from anywhere in the app.
 */
public final class AppContext {
    private AppContext() {
        // prevent instantiation
    }

    // Generic registry for any service by its class type
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Registers a service instance under its class type.
     * @param type the interface or class to register
     * @param instance the implementation instance
     * @param <T> the service type
     * @throws IllegalStateException if a service is already registered for this type
     */
    public static <T> void registerService(Class<T> type, T instance) {
        if (services.containsKey(type)) {
            throw new IllegalStateException(type.getSimpleName() + " already registered");
        }
        services.put(type, instance);
    }

    /**
     * Retrieves a registered service by its class type.
     * @param type the interface or class of the service
     * @param <T> the service type
     * @return the registered service instance
     * @throws IllegalStateException if no service is registered for this type
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getSimpleName());
        }
        return (T) service;
    }

    // Convenience methods for common types, if desired

    /**
     * Register the StockRepository for easy lookup.
     */
    public static void initStockRepository(com.javarepowizards.portfoliomanager.domain.stock.StockRepository repo) {
        registerService(com.javarepowizards.portfoliomanager.domain.stock.StockRepository.class, repo);
    }

    /**
     * Retrieve the shared StockRepository.
     */
    public static com.javarepowizards.portfoliomanager.domain.stock.StockRepository getStockRepository() {
        return getService(com.javarepowizards.portfoliomanager.domain.stock.StockRepository.class);
    }
}
