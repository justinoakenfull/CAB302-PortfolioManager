package com.javarepowizards.portfoliomanager.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

public final class TableCellFactories {
    private TableCellFactories() {}

    /**
     * Numeric cell: formats to `decimals` places, and if colourIfPositive==true
     * paints text green when >=0, red when <0.
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
     * Currency cell: uses NumberFormat.getCurrencyInstance(locale), set to `decimals` places.
     * Always centered.
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
     * Long cell: formats with thousands separators (%,d).
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
