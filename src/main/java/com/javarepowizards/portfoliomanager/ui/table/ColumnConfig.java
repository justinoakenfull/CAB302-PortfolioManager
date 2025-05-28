package com.javarepowizards.portfoliomanager.ui.table;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.util.function.Function;

/**
 * Configuration for a single column in a TableView.
 * Defines the column header text, how to extract a cell value from a row,
 * and an optional cell factory for custom rendering.
 *
 * @param <S> the type of the items contained within the TableView rows
 * @param <T> the type of the cell values in this column
 */
public class ColumnConfig<S,T> {
    private final String header;
    private final Function<S,ObservableValue<T>> valueProvider;
    private final Callback<TableColumn<S,T>,TableCell<S,T>> cellFactory;


    /**
     * Creates a ColumnConfig with a header, value extractor, and custom cell factory.
     *
     * @param header the text to display in the column header
     * @param valueProvider a function that maps a row item to an ObservableValue for the cell
     * @param cellFactory a factory for creating TableCell instances, or null to use the default
     */
    public ColumnConfig(
            String header,
            Function<S,ObservableValue<T>> valueProvider,
            Callback<TableColumn<S,T>,TableCell<S,T>> cellFactory
    ) {
        this.header        = header;
        this.valueProvider = valueProvider;
        this.cellFactory   = cellFactory;
    }

    /**
     * Creates a ColumnConfig with a header and value extractor.
     * Uses the default cell factory provided by TableView.
     *
     * @param header the text to display in the column header
     * @param valueProvider a function that maps a row item to an ObservableValue for the cell
     */
    public ColumnConfig(
            String header,
            Function<S,ObservableValue<T>> valueProvider
    ) {
        this(header, valueProvider, null);
    }

    /**
     * Returns the header text for this column.
     *
     * @return the header string
     */
    public String getHeader() { return header; }

    /**
     * Returns the function used to extract cell values from a row item.
     *
     * @return the value provider function
     */
    public Function<S,ObservableValue<T>> getValueProvider() {
        return valueProvider;
    }

    /**
     * Returns the cell factory for creating TableCell instances.
     * May be null to indicate that the default factory should be used.
     *
     * @return the cell factory callback or null
     */
    public Callback<TableColumn<S,T>,TableCell<S,T>> getCellFactory() {
        return cellFactory;
    }
}
