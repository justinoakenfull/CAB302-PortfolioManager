package com.javarepowizards.portfoliomanager.models;

import com.javarepowizards.portfoliomanager.controllers.watchlist.WatchlistRow;
import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistTest {

    private static final int USER_ID = 42;
    private StockName sym1;
    private String ticker1;
    private IStock stockStub;
    private StockRepository repoStub;
    private TestDAO daoStub;
    private Watchlist watchlist;

    /** Simple DAO stub to control listForUser and record remove calls */
    private static class TestDAO implements IWatchlistDAO {
        private List<StockName> list;
        private boolean removeCalled = false;

        void setList(List<StockName> list) { this.list = list; removeCalled = false; }

        boolean wasRemoveCalled() { return removeCalled; }

        @Override
        public List<StockName> listForUser(int userId) throws SQLException {
            return list;
        }

        @Override
        public void removeForUser(int userId, StockName symbol) throws SQLException {
            removeCalled = true;
            // simulate removal by clearing list
            list = Collections.emptyList();
        }
    }

    @BeforeEach
    void setup() {
        // pick a valid StockName from enum
        sym1 = StockName.values()[0];
        ticker1 = sym1.getSymbol();

        // stub IStock for given ticker
        stockStub = new IStock() {
            @Override public String getTicker() { return ticker1; }
            @Override public String getCompanyName() { return "Company " + ticker1; }
            @Override public com.javarepowizards.portfoliomanager.domain.price.PriceRecord getCurrentRecord() { return null; }
            @Override public com.javarepowizards.portfoliomanager.domain.price.PriceHistory getHistory() { return null; }
            @Override public String getShortDescription() { return "Short"; }
            @Override public String getLongDescription() { return "Long"; }
        };

        // stub repository matching only ticker1
        repoStub = new StockRepository() {
            @Override
            public Set<String> availableTickers() {
                return Set.of(ticker1);
            }

            @Override
            public IStock getByTicker(String ticker) throws IOException {
                if (!ticker.equals(ticker1)) throw new IOException("Not found");
                return stockStub;
            }

            @Override
            public List<IStock> getAll() throws IOException {
                return List.of(stockStub);
            }
        };

        daoStub = new TestDAO();
        daoStub.setList(List.of(sym1));
    }

    @Test
    void constructorPopulatesRows() {
        watchlist = new Watchlist(repoStub, daoStub, USER_ID);
        var rows = watchlist.getRows();
        assertEquals(1, rows.size(), "Should load one row from DAO");
        WatchlistRow row = rows.get(0);
        assertSame(stockStub, row.getStock(), "Row should wrap the correct IStock");
    }

    @Test
    void removeCallbackInvokesDaoAndRefreshes() {
        watchlist = new Watchlist(repoStub, daoStub, USER_ID);
        WatchlistRow row = watchlist.getRows().get(0);
        // invoke removal
        row.getRemoveCallback().run();
        assertTrue(daoStub.wasRemoveCalled(), "DAO.removeForUser should have been called");
        assertTrue(watchlist.getRows().isEmpty(), "Rows should be empty after removal and refresh");
    }

    @Test
    void refreshSkipsUnavailableTicker() {
        // include sym1 and a fake sym2
        StockName sym2 = StockName.values()[1];
        daoStub.setList(List.of(sym1, sym2));
        watchlist = new Watchlist(repoStub, daoStub, USER_ID);
        var rows = watchlist.getRows();
        assertEquals(1, rows.size(), "Should only include available tickers");
        assertSame(stockStub, rows.get(0).getStock());
    }

    @Test
    void constructorWrapsDaoException() {
        IWatchlistDAO badDao = new IWatchlistDAO() {
            @Override public List<StockName> listForUser(int userId) throws SQLException { throw new SQLException("fail"); }
            @Override public void removeForUser(int userId, StockName symbol) throws SQLException { }
        };
        Exception ex = assertThrows(RuntimeException.class,
                () -> new Watchlist(repoStub, badDao, USER_ID));
        assertTrue(ex.getCause() instanceof SQLException, "Cause should be the original SQLException");
    }

    @Test
    void constructorWrapsRepoIOException() {
        StockRepository badRepo = new StockRepository() {
            @Override public Set<String> availableTickers() { return Set.of(ticker1); }
            @Override public IStock getByTicker(String ticker) throws IOException { throw new IOException("io fail"); }
            @Override public List<IStock> getAll() throws IOException { return List.of(); }
        };
        Exception ex = assertThrows(RuntimeException.class,
                () -> new Watchlist(badRepo, daoStub, USER_ID));
        assertTrue(ex.getCause() instanceof IOException, "Cause should be the original IOException");
    }
}
