package com.javarepowizards.portfoliomanager.ui.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class providing factory methods for creating TableCell instances
 * tailored to display numeric, currency, and long values in TableView columns.
 * Class is non-instantiable.
 */
public final class TableCellFactories {
    private TableCellFactories() {}

    /**
     * Returns a cell factory that formats Double values to the specified number
     * of decimal places. If colourIfPositive is true, text color is set to green
     * when value is greater than or equal to zero and red when value is negative.
     *
     * @param decimals number of decimal places to display
     * @param colourIfPositive true to apply positive/negative colouring
     * @param <S> the row item type
     * @return a Callback suitable for use as a TableColumn cell factory
     */
    public static <S>
    Callback<TableColumn<S,Double>,TableCell<S,Double>>
    numericFactory(int decimals, boolean colourIfPositive) {
        final String fmt = "%,."+ decimals +"f";
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setTextFill(null);    // reset to default
                } else {
                    setText(String.format(fmt, v));
                    if (colourIfPositive) {
                        String colour = v>=0 ? "green" : "red";
                        setStyle(String.format(
                                "-fx-text-fill: %s;", colour));
                    }
                }
            }
        };
    }

    /**
     * Returns a cell factory that formats Double values as currency
     * using the specified Locale and number of decimal places.
     * Cells are centered by default in the TableView.
     *
     * @param locale the Locale for currency formatting
     * @param decimals number of decimal places to display
     * @param <S> the row item type
     * @return a Callback suitable for use as a TableColumn cell factory
     */
    public static <S>
    Callback<TableColumn<S,Double>,TableCell<S,Double>>
    currencyFactory(Locale locale, int decimals) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        nf.setMaximumFractionDigits(decimals);
        nf.setMinimumFractionDigits(decimals);
        return col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v==null) {
                    setText(null);
                } else {
                    setText(nf.format(v));
                }
            }
        };
    }

    /**
     * Returns a cell factory that formats Long values with thousands separators.
     *
     * @param <S> the row item type
     * @return a Callback suitable for use as a TableColumn cell factory
     */
    public static <S>
    Callback<TableColumn<S,Long>,TableCell<S,Long>>
    longFactory() {
        return col -> new TableCell<>() {
            @Override protected void updateItem(Long v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v==null) {
                    setText(null);
                } else {
                    setText(String.format("%,d", v));
                }
            }
        };
    }
}
