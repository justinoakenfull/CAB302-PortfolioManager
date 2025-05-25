package com.javarepowizards.portfoliomanager.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.util.List;


/**
 * Handles the logic for displaying investment tips in the dashboard
 * Tips fade out and update every 5 seconds
 */
public class QuickTips {

    // List of investment tips to be shown in rotation
    private final List<String> tips = List.of(
            "Profits require risk—use volatility as your reality-check for just how much risk you’re taking.",
            "Shares with steady returns and low volatility suit investors who prefer stability over thrill-seeking.",
            "Chasing quick trades? High volatility stocks can offer the swings you need-just be ready for the ride.",
            "Watch the recent price action: momentum matters. Winners often keep winning, and laggard keep lagging.",
            "Use the Watchlist to track a share for a few days before committing cash",
            "Set a dollar limit per trade and stick to it; avoid impluse buys",
            "Diversify: hold positions in several different sectors to smooth swings",
            "Selling is a strategy too; lock in profits or cut losses at a pre-set price",
            "Record the reason for every trade - it teaches you more than the price chart",
            "Think in terms of Risk-Adjusted Returns: A high return isn't always ideal if it comes with extreme volatility; evaluate both risk and reward"
    );

    // Tracks the currently displayed tip
    private int tipIndex = 0;

    // Reference to the Label on the UI where tips are displayed
    private final Label quickTipsLabel;

    // Timeline that schedules the tip to update every 5 seconds
    private final Timeline timeline;


    /**
     * Initialises the QuickTips component with the given label
     * @param quickTipsLabel - the JavaFX Label where tips will be displayed
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
                new KeyFrame(Duration.seconds(5), event -> updateTip())
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

        fadeOut.play();
    }

    /**
     * Starts automatic rotation of tips
     */
    public void start() {
        timeline.play();
    }
}
