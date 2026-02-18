package com.solutiongameofficial.phase.success;

public final class GameEndedState {

    private final GameEndedStoryText storyText = new GameEndedStoryText();
    private final double extraHoldSecondsAfterStory;

    private double elapsedSeconds;

    public GameEndedState(double extraHoldSecondsAfterStory) {
        this.extraHoldSecondsAfterStory = Math.max(0.0, extraHoldSecondsAfterStory);
    }

    public void update(double deltaTimeSeconds) {
        double deltaTime = Math.max(0.0, deltaTimeSeconds);
        elapsedSeconds += deltaTime;
        storyText.update(deltaTime);
    }

    public boolean isFinished() {
        if (storyText.isFullyRevealed()) {
            return false;
        }
        return storyText.secondsSinceFullyRevealed() >= extraHoldSecondsAfterStory;
    }

    public double elapsedSeconds() {
        return elapsedSeconds;
    }

    public GameEndedStoryText storyText() {
        return storyText;
    }
}