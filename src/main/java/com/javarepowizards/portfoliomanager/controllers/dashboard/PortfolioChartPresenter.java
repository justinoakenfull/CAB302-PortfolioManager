package com.javarepowizards.portfoliomanager.controllers.dashboard;

import com.javarepowizards.portfoliomanager.dao.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.services.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.sql.SQLException;
import java.util.List;


final class PortfolioChartPresenter {

    private final Pane         target;
    private final IPortfolioDAO dao;

    /** display widget */
    PortfolioChartPresenter(Pane target, IPortfolioDAO dao) {
        this.target = target;
        this.dao    = dao;
        refresh();
    }

    /* update when portfolio holdings change - buy/sell action */
    void refresh() {
        target.getChildren().clear();

        int userId = Session.getCurrentUser().getUserId();
        List<PortfolioEntry> holdings;
        try {
            holdings = dao.getHoldingsForUser(userId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
        /* Empty state */
        if (holdings.isEmpty()) {
            Label msg = new Label(
                    "No holdings yet – start building your portfolio!");
            msg.getStyleClass().add("empty-holdings-label");
            msg.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // centre text
            msg.minWidthProperty().bind(target.widthProperty());
            msg.minHeightProperty().bind(target.heightProperty());
            target.getChildren().setAll(msg);
            return;
        }
        /* Build chart */
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                holdings.stream()
                        .map(e -> new PieChart.Data(
                                e.getStock().getSymbol(),
                                e.getMarketValue()))
                        .toList());

        PieChart chart = new PieChart(data);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);

        // enable chart to stretch within container
        chart.minWidthProperty().bind(target.widthProperty());
        chart.minHeightProperty().bind(target.heightProperty());
        chart.maxWidthProperty().bind(target.widthProperty());
        chart.maxHeightProperty().bind(target.heightProperty());

        target.getChildren().setAll(chart);
    }
}