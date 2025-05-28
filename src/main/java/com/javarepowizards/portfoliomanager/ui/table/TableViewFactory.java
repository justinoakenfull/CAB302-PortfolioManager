package com.javarepowizards.portfoliomanager.ui.table;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.value.ObservableValue;
import javafx.util.Callback;

import java.util.List;


/**
 * Factory for creating and configuring TableView instances based on a list of ColumnConfig objects.
 * Each ColumnConfig defines the header text, value extraction, and optional cell factory for one column.
 */
public class TableViewFactory {

    /**
     * Creates a new TableView populated with columns defined by the given configurations.
     * The returned table has no items until setItems is called separately.
     *
     * @param configs the list of ColumnConfig instances defining each column
     * @param <S> the row item type
     * @return a new TableView with columns configured
     */
    public static <S> TableView<S> create(List<ColumnConfig<S,?>> configs) {
        TableView<S> table = new TableView<>();
        configure(table, configs);
        return table;
    }

    /**
     * Configures the given TableView by clearing existing columns and adding new ones
     * based on the provided ColumnConfig list. Sets cell value factories and optional
     * cell factories for custom rendering.
     *
     * @param table the TableView to configure
     * @param configs the list of ColumnConfig instances defining each column
     * @param <S> the row item type
     */
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
