package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.StockName;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WatchlistDAOTest {

    private IWatchlistDAO watchlistDAO;
    private Connection dbConnection;
    private static final int TEST_USER_ID = 1;

    @BeforeAll
    void init() throws SQLException {
        IDatabaseConnection dbService = new DatabaseConnection();
        dbConnection = dbService.getConnection();
        watchlistDAO = new WatchlistDAO(dbService);
    }

    @BeforeEach
    void cleanTable() throws SQLException {
        try (Statement stmt = dbConnection.createStatement()) {
            stmt.executeUpdate("DELETE FROM user_watchlist");
        }
    }

    @Test
    @DisplayName("Listing for a new user yields empty list")
    void listForUser_empty() throws SQLException {
        List<StockName> list = watchlistDAO.listForUser(TEST_USER_ID);
        assertTrue(list.isEmpty(), "Expected no entries for a fresh user");
    }

    @Test
    @DisplayName("Adding a symbol appears in the list")
    void addForUser_and_list() throws SQLException {
        watchlistDAO.addForUser(TEST_USER_ID, StockName.WES_AX);
        List<StockName> list = watchlistDAO.listForUser(TEST_USER_ID);

        assertEquals(1, list.size(), "Should have exactly one symbol");
        assertTrue(list.contains(StockName.WES_AX), "List must contain WES_AX");
    }

    @Test
    @DisplayName("Adding the same symbol twice does not duplicate")
    void addForUser_idempotent() throws SQLException {
        watchlistDAO.addForUser(TEST_USER_ID, StockName.AMC_AX);
        watchlistDAO.addForUser(TEST_USER_ID, StockName.AMC_AX);
        List<StockName> list = watchlistDAO.listForUser(TEST_USER_ID);

        assertEquals(1, list.size(), "Duplicate insert should be ignored");
        assertEquals(StockName.AMC_AX, list.getFirst(), "Only one entry of AMC_AX expected");
    }

    @Test
    @DisplayName("Removing a symbol deletes it from the list")
    void removeForUser_existing() throws SQLException {
        watchlistDAO.addForUser(TEST_USER_ID, StockName.TLS_AX);
        watchlistDAO.removeForUser(TEST_USER_ID, StockName.TLS_AX);

        List<StockName> list = watchlistDAO.listForUser(TEST_USER_ID);
        assertFalse(list.contains(StockName.TLS_AX), "Symbol should have been removed");
    }

    @Test
    @DisplayName("Removing a non-existent symbol is a no-op")
    void removeForUser_nonexistent() throws SQLException {
        watchlistDAO.removeForUser(TEST_USER_ID, StockName.ALL_AX);
        List<StockName> list = watchlistDAO.listForUser(TEST_USER_ID);

        assertTrue(list.isEmpty(), "Removing a missing symbol should not error");
    }
}
