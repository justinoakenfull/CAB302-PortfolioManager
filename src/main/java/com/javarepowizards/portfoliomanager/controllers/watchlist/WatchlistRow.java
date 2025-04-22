package com.javarepowizards.portfoliomanager.controllers.watchlist;

import com.javarepowizards.portfoliomanager.models.StockData;
import com.javarepowizards.portfoliomanager.models.StockName;
import javafx.beans.property.*;
import javafx.scene.control.Button;

public class WatchlistRow {
    private final StringProperty shortName     = new SimpleStringProperty();
    private final StringProperty displayName   = new SimpleStringProperty();
    private final DoubleProperty open          = new SimpleDoubleProperty();
    private final DoubleProperty close         = new SimpleDoubleProperty();
    private final DoubleProperty change        = new SimpleDoubleProperty();
    private final DoubleProperty changePercent = new SimpleDoubleProperty();
    private final DoubleProperty price         = new SimpleDoubleProperty();
    private final LongProperty   volume        = new SimpleLongProperty();
    private final ObjectProperty<Button> remove = new SimpleObjectProperty<>();

    public WatchlistRow(StockName symbol, StockData data, Button removeBtn) {
        shortName.set(symbol.name());
        displayName.set(symbol.getSymbol());
        open.set(data.getOpen());
        close.set(data.getClose());
        change.set(data.getClose() - data.getOpen());
        changePercent.set(open.get() == 0
                ? 0
                : ((change.get() / open.get()) * 100));
        price.set(data.getPrice() != null ? data.getPrice() : data.getClose());
        volume.set(data.getVolume());
        remove.set(removeBtn);
    }

    public StringProperty shortNameProperty()     { return shortName; }
    public StringProperty displayNameProperty()   { return displayName; }
    public DoubleProperty openProperty()          { return open; }
    public DoubleProperty closeProperty()         { return close; }
    public DoubleProperty changeProperty()        { return change; }
    public DoubleProperty changePercentProperty() { return changePercent; }
    public DoubleProperty priceProperty()         { return price; }
    public LongProperty   volumeProperty()        { return volume; }
    public ObjectProperty<Button> removeProperty(){ return remove; }
}
