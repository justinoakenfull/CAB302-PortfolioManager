package com.javarepowizards.portfoliomanager.ui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.sql.Time;
import java.util.List;

public class QuickTips {
    private final List<String> tips = List.of(
            "Tip 1: Harness the Power of Compounding: begin investing early to maximise returns through the exponential growth effect of compounding interest.",
            "Tip 2: Achieve Diversification Across Asset Classes: allocate your investment across equities, fixed income, real assets, and cash equivalents to minimise unsystematic risk.",
            "Tip 3: Utilise Dollar-Cost Averaging: Regularly invest fixed amounts over time to mitigate the impact of market volatility and reduce timing risk.",
            "Tip 4: Align Investments with Your Risk Profile: Understand your personal risk tolerance and ensure your asset allocation reflects your investment horizon and financial goals.",
            "Tip 5: Maintain a Long-Term Investment Horizon: Avoid market timing and remain invested to capitalise on historical trends of long-term market appreciation",
            "Tip 6: Conduct Periodic Portfolio Rebalancing: Rebalance your portfolio systematically to maintain your target asset allocation and control risk exposure",
            "Tip 7: Focus on Fundamentals, Not Market Noise: Prioritise fundamental analysis over short-term price movements and avoid decisions based on sensationalised media headlines",
            "Tip 8: Conduct Due Diligence Before Allocating Capital: Ensure you thoroughly understand an asset's underlying risks, return drivers, and market behaviour before investing",
            "Tip 9: Stay Invested During Market Fluctuations: Withdrawing during downturns often locks in losses - remaining invested allows for recovery and growth",
            "Tip 10: Think in Terms of Risk-Adjusted Returns: A high return isn't always ideal if it comes with extreme volatility; evaluate both risk and reward"

    );

    private int tipIndex = 0;
    private final Label quickTipsLabel;
    private final Timeline timeline;

    public QuickTips(Label quickTipsLabel){
        this.quickTipsLabel = quickTipsLabel;

        quickTipsLabel.setStyle("-fx-font-size:18px; -fx-text-fill: #333; -fx-font-weight: bold;");
        quickTipsLabel.setWrapText(true);

        updateTip();

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(8), event -> updateTip())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateTip() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), quickTipsLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {

            tipIndex = (tipIndex + 1) % tips.size();
            quickTipsLabel.setText(tips.get(tipIndex));

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), quickTipsLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
