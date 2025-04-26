package com.javarepowizards.portfoliomanager.ui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.util.function.Function;

/**
 * Describes one column in a TableView<S>:
 *  - header text
 *  - how to extract an ObservableValue<T> from S
 *  - optional custom cell factory
 */
public class ColumnConfig<S,T> {
    private final String header;
    private final Function<S,ObservableValue<T>> valueProvider;
    private final Callback<TableColumn<S,T>,TableCell<S,T>> cellFactory;

    public ColumnConfig(
            String header,
            Function<S,ObservableValue<T>> valueProvider,
            Callback<TableColumn<S,T>,TableCell<S,T>> cellFactory
    ) {
        this.header        = header;
        this.valueProvider = valueProvider;
        this.cellFactory   = cellFactory;
    }
    public ColumnConfig(
            String header,
            Function<S,ObservableValue<T>> valueProvider
    ) {
        this(header, valueProvider, null);
    }

    public String getHeader() { return header; }
    public Function<S,ObservableValue<T>> getValueProvider() {
        return valueProvider;
    }
    public Callback<TableColumn<S,T>,TableCell<S,T>> getCellFactory() {
        return cellFactory;
    }
}
