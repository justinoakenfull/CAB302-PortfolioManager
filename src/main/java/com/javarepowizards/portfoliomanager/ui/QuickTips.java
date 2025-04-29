package com.javarepowizards.portfoliomanager.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.sql.Time;
import java.util.List;

public class QuickTips {
    private final List<String> tips = List.of(
            "Tip 1:",
            "Tip 1:",
            "Tip 2:",
            "Tip 3:",
            "Tip 4:",
            "Tip 5:",
            "Tip 6:",
            "Tip 7:",
            "Tip 8:",
            "Tip 9:",
            "Tip 10:"

    );

    private int tipIndex = 0;
    private final Label quickTipsLabel;
    private final Timeline timeline;

    public QuickTips(Label quickTipsLabel){
        this.quickTipsLabel = quickTipsLabel;

        quickTipsLabel.setText(tips.get(tipIndex));

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(10), event -> updateTip())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateTip() {
        tipIndex = (tipIndex + 1) % tips.size();
        quickTipsLabel.setText(tips.get(tipIndex));
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }
}
