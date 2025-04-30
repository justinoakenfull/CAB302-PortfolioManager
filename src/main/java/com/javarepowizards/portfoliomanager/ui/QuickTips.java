package com.javarepowizards.portfoliomanager.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.sql.Time;
import java.util.List;


/**
 * Handles the logic for displaying investment tips in the dashboard
 * Tips fade out and update every 8 seconds
 */
public class QuickTips {

    // List of investment tips to be shown in rotation
    private final List<String> tips = List.of(
            "Harness the Power of Compounding: begin investing early to maximise returns through the exponential growth effect of compounding interest.",
            "Achieve Diversification Across Asset Classes: allocate your investment across equities, fixed income, real assets, and cash equivalents to minimise unsystematic risk.",
            "Utilise Dollar-Cost Averaging: Regularly invest fixed amounts over time to mitigate the impact of market volatility and reduce timing risk.",
            "Align Investments with Your Risk Profile: Understand your personal risk tolerance and ensure your asset allocation reflects your investment horizon and financial goals.",
            "Maintain a Long-Term Investment Horizon: Avoid market timing and remain invested to capitalise on historical trends of long-term market appreciation",
            "Conduct Periodic Portfolio Rebalancing: Rebalance your portfolio systematically to maintain your target asset allocation and control risk exposure",
            "Focus on Fundamentals, Not Market Noise: Prioritise fundamental analysis over short-term price movements and avoid decisions based on sensationalised media headlines",
            "Conduct Due Diligence Before Allocating Capital: Ensure you thoroughly understand an asset's underlying risks, return drivers, and market behaviour before investing",
            "Stay Invested During Market Fluctuations: Withdrawing during downturns often locks in losses - remaining invested allows for recovery and growth",
            "Think in Terms of Risk-Adjusted Returns: A high return isn't always ideal if it comes with extreme volatility; evaluate both risk and reward"

    );

    // Tracks the currently displayed tip
    private int tipIndex = 0;

    // Reference to the Label on the UI where tips are displayed
    private final Label quickTipsLabel;

    // Timeline that schedules the tip to update every 8 seconds
    private final Timeline timeline;


    /**
     * Initialises the QuickTips component with the given label
     * @param quickTipsLabel - the JavaFX Label where tips will be displaed
     */
    public QuickTips(Label quickTipsLabel){
        this.quickTipsLabel = quickTipsLabel;

        // Applies the CSS style class and wraps long tips
        quickTipsLabel.getStyleClass().add("quick-tip-label");
        quickTipsLabel.setWrapText(true);

        // Set the initial tip
        updateTip();

        //Set up timeline to update the tip every 8 seconds - ensuring it repeats indefinitely
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(8), event -> updateTip())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Updates the tip on screen with a fade out and in effect
     */
    private void updateTip() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), quickTipsLabel);
        // Fade out current tip
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            // After fade out completes, update the label text and cycle to next tip
            tipIndex = (tipIndex + 1) % tips.size();
            quickTipsLabel.setText(tips.get(tipIndex));

            // Fade in new tip
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), quickTipsLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });


        fadeOut.play(); //Begin fade out
    }

    /**
     * Starts automatic rotation of tips
     */
    public void start() {
        timeline.play();
    }
}
