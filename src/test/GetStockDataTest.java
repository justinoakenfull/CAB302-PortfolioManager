
package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.domain.stock.Stock;
import com.javarepowizards.portfoliomanager.models.StockName;
import com.javarepowizards.portfoliomanager.models.StockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GetStockDataTest {
    private StockDAO stockDAO;

    @BeforeEach
    public void setUp(){
    // Use the singleton instance which automatically loads the CVS
        stockDAO = StockDAO.getInstance();
    }

    @Test
    public void testGetStockData_returnsNonEmptyList(){
    // Verifies that getStockData returns real data for a known stock symbol
        List<StockData> stockList = stockDAO.getStockData(StockName.WES_AX);

        assertNotNull(stockList);
        assertFalse(stockList.isEmpty());
    }
}
