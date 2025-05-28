package com.javarepowizards.portfoliomanager.services;



import com.javarepowizards.portfoliomanager.AppContext;
import com.javarepowizards.portfoliomanager.dao.*;
import com.javarepowizards.portfoliomanager.dao.user.IUserDAO;
import com.javarepowizards.portfoliomanager.dao.user.UserDAO;
import com.javarepowizards.portfoliomanager.dao.watchlist.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.dao.watchlist.WatchlistDAO;
import com.javarepowizards.portfoliomanager.services.Auth.AuthService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AppContextTest {
    // Registers a valid service.
    @Test
    void regServiceTest() throws SQLException {
        IDatabaseConnection dbConnection = new DatabaseConnection();
        assertDoesNotThrow(() -> AppContext.registerService(IDatabaseConnection.class, dbConnection));
    }

    // Tries to register a duplicate service.
    @Test
    void regServiceTest2() throws SQLException {
        IDatabaseConnection dbConnection = new DatabaseConnection();
        IWatchlistDAO watchlistDAO = new WatchlistDAO(dbConnection);
        AppContext.registerService(IWatchlistDAO.class, watchlistDAO);

        assertThrows(IllegalStateException.class, () -> AppContext.registerService(IWatchlistDAO.class, watchlistDAO));
    }

    // Gets a registered service.
    @Test
    void getServiceTest() throws SQLException {
        IDatabaseConnection dbConnection = new DatabaseConnection();
        IUserDAO userDAO = new UserDAO(dbConnection);
        AppContext.registerService(IUserDAO.class, userDAO);

        assertEquals(userDAO, AppContext.getService(IUserDAO.class));
    }

    // Tries to get a service that isn't registered.
    @Test
    void getServiceTestIllegal() {
        assertThrows(IllegalStateException.class, () -> AppContext.getService(AuthService.class));
    }
}