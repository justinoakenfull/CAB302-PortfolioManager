package com.javarepowizards.portfoliomanager.services.portfolio;

import com.javarepowizards.portfoliomanager.dao.portfolio.IPortfolioDAO;
import com.javarepowizards.portfoliomanager.models.PortfolioEntry;
import com.javarepowizards.portfoliomanager.services.session.Session;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/**
 * Presenter for a stacked‐bar visualization of a portfolio,
 * plus a fully dynamic legend underneath.
 */
public class PortfolioBarPresenter {
    private final Pane chartTarget;
    private final IPortfolioDAO dao;
    private Pane portfolioLegendContainer;   // ← renamed to match FXML

    /**
     * @param chartTarget Pane into which the bar will be rendered
     * @param dao         DAO to fetch current holdings
     */
    public PortfolioBarPresenter(Pane chartTarget, IPortfolioDAO dao) {
        this.chartTarget = chartTarget;
        this.dao         = dao;
    }

    /**
     * Give the presenter the HBox from your FXML.
     *
     * @param portfolioLegendContainer the HBox under the chart
     */
    public void configureLegendContainer(Pane portfolioLegendContainer) {
        this.portfolioLegendContainer = portfolioLegendContainer;
    }

    /**
     * Draws the bar and populates the legend.
     *
     * @throws SQLException if fetching holdings fails
     */
    public void refresh() throws SQLException {
        chartTarget.getChildren().clear();
        if (portfolioLegendContainer != null) {
            portfolioLegendContainer.getChildren().clear();
        }

        List<PortfolioEntry> holdings = dao
                .getHoldingsForUser(Session.getCurrentUser().getUserId())
                .stream()
                .sorted(Comparator.comparingDouble(
                        PortfolioEntry::getMarketValue).reversed())
                .toList();

        if (holdings.isEmpty()) {
            chartTarget.getChildren().add(new Label("No holdings"));
            return;
        }

        double total = holdings.stream()
                .mapToDouble(PortfolioEntry::getMarketValue)
                .sum();

        HBox bar = new HBox();
        bar.setPrefHeight(16);
        bar.setMaxWidth(Double.MAX_VALUE);

        for (PortfolioEntry e : holdings) {
            Region seg = new Region();
            double pct = e.getMarketValue() / total;
            seg.prefWidthProperty()
                    .bind(chartTarget.widthProperty().multiply(pct));
            HBox.setHgrow(seg, Priority.ALWAYS);

            Color c = Color.hsb(
                    Math.abs(e.getStock().getSymbol().hashCode()) % 360,
                    0.6, 0.6
            );
            seg.setBackground(new Background(new BackgroundFill(
                    c, CornerRadii.EMPTY, Insets.EMPTY
            )));

            Tooltip.install(seg, new Tooltip(
                    e.getStock().getSymbol() + ": " +
                            String.format("%.1f%%", pct * 100)
            ));

            bar.getChildren().add(seg);

            if (portfolioLegendContainer != null) {
                Label lbl = new Label(
                        e.getStock().getSymbol() + " " +
                                String.format("%.1f%%", pct * 100)
                );
                lbl.setTextFill(c);
                lbl.setFont(Font.font(12));
                portfolioLegendContainer.getChildren().add(lbl);
            }
        }

        chartTarget.getChildren().add(bar);
    }
}
