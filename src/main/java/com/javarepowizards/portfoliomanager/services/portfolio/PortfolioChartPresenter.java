package com.javarepowizards.portfoliomanager.services.portfolio;

import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.services.session.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.sql.SQLException;
import java.util.List;

/**
 * Presenter for the Portfolio Overview pie-chart
 *
 * Queries the DAO for holdings and renders either an empty-state
 * message or a PieChart
 */
public class PortfolioChartPresenter {

    private final Pane         target;
    private final IPortfolioDAO dao;

    /**
     *
     * @param target where the chart (or message) is displayed
     * @param dao fetches the current user's holdings
     */
    public PortfolioChartPresenter(Pane target, IPortfolioDAO dao) {
        this.target = target;
        this.dao    = dao;
        refresh();
    }

    /** Rebuilds the chart or empty message whenever holdings change */
    public void refresh() {
        target.getChildren().clear();

        int userId = Session.getCurrentUser().getUserId();
        List<PortfolioEntry> holdings;
        try {
            holdings = dao.getHoldingsForUser(userId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }

        if (holdings.isEmpty()) {
            Label msg = new Label("No holdings yet – start building your portfolio!");
            msg.getStyleClass().add("empty-holdings-label");
            msg.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            msg.minWidthProperty().bind(target.widthProperty());
            msg.minHeightProperty().bind(target.heightProperty());
            target.getChildren().setAll(msg);
            return;
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                holdings.stream()
                        .map(e -> new PieChart.Data(
                                e.getStock().getSymbol(),
                                e.getMarketValue()))
                        .toList());

        PieChart chart = new PieChart(data);
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.minWidthProperty().bind(target.widthProperty());
        chart.minHeightProperty().bind(target.heightProperty());
        chart.maxWidthProperty().bind(target.widthProperty());
        chart.maxHeightProperty().bind(target.heightProperty());

        target.getChildren().setAll(chart);
    }
}