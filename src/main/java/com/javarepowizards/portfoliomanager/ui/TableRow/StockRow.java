package com.javarepowizards.portfoliomanager.ui.TableRow;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;

/**
 * View-model for a stock table row.
 * Extends AbstractStockRow to expose common stock properties.
 */
public class StockRow extends AbstractStockRow {

    /**
     * Constructs a StockRow backed by the given stock.
     *
     * @param stock the IStock to display
     */
    public StockRow(IStock stock) {
        initFrom(stock);
    }
}
