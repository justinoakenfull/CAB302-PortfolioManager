package com.javarepowizards.portfoliomanager.ui.TableRow;

import com.javarepowizards.portfoliomanager.domain.stock.IStock;
import com.javarepowizards.portfoliomanager.domain.price.PriceRecord;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

/**
 * View-model for a watchlist table row.
 * Extends AbstractStockRow with current price and a remove button.
 */
public class WatchlistRow extends AbstractStockRow {

    private final DoubleProperty price = new SimpleDoubleProperty();
    private final ObjectProperty<Button> remove = new SimpleObjectProperty<>();

    /**
     * Constructs a WatchlistRow for the given stock.
     * Populates common properties, sets the current price,
     * and attaches a remove button callback.
     *
     * @param stock    the IStock to display
     * @param onRemove callback invoked when the remove button is clicked
     */
    public WatchlistRow(IStock stock, Runnable onRemove) {
        initFrom(stock);

        PriceRecord rec = stock.getCurrentRecord();
        price.set(rec.close());

        Button btn = new Button("Remove");
        btn.getStyleClass().add("btn-danger");
        btn.setOnAction(e -> onRemove.run());
        remove.set(btn);
    }

    /**
     * @return observable current price property
     */
    public DoubleProperty priceProperty() {
        return price;
    }

    /**
     * @return observable remove-button property
     */
    public ObjectProperty<Button> removeProperty() {
        return remove;
    }
}
