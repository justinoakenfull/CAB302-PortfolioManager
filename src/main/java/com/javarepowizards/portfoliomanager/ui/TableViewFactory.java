package com.javarepowizards.portfoliomanager.ui;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

import java.util.List;

/**
 * Builds a TableView{@literal <S>} given a list of ColumnConfig{@literal <S,?>}.
 */
public class TableViewFactory {
    @SuppressWarnings("unchecked")
    public static <S> TableView<S> create(List<ColumnConfig<S,?>> configs) {
        TableView<S> table = new TableView<>();
        configure(table, configs);
        return table;
    }

    @SuppressWarnings("unchecked")
    public static <S> void configure(
            TableView<S> table,
            List<ColumnConfig<S,?>> configs
    ) {
        table.getColumns().clear();
        for (ColumnConfig<S,?> cfg : configs) {
            TableColumn<S,Object> col = new TableColumn<>(cfg.getHeader());
            col.setCellValueFactory(cd ->
                    (ObservableValue<Object>)cfg.getValueProvider().apply(cd.getValue())
            );
            if (cfg.getCellFactory() != null) {
                col.setCellFactory((Callback)cfg.getCellFactory());
            }
            table.getColumns().add(col);
        }
    }
}
