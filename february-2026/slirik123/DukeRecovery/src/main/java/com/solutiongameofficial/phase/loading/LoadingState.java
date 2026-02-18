package com.solutiongameofficial.phase.loading;

public final class LoadingState {

    private final double durationSeconds;
    private double elapsedSeconds;

    public LoadingState(double durationSeconds) {
        this.durationSeconds = Math.max(0.0, durationSeconds);
    }

    public void update(double deltaTimeSeconds) {
        if (elapsedSeconds >= durationSeconds) {
            return;
        }

        elapsedSeconds += Math.max(0.0, deltaTimeSeconds);
        if (elapsedSeconds > durationSeconds) {
            elapsedSeconds = durationSeconds;
        }
    }

    public boolean isFinished() {
        return elapsedSeconds >= durationSeconds;
    }

    public double elapsedSeconds() {
        return elapsedSeconds;
    }
}