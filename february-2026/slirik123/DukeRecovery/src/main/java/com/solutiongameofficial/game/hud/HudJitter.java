package com.solutiongameofficial.game.hud;

import lombok.Getter;

import java.util.Random;

public final class HudJitter {

    private final Random random;

    private double timerSeconds;
    private double nextJitterInSeconds;
    private double activeSeconds;

    @Getter
    private int offsetX;

    @Getter
    private int offsetY;

    public HudJitter(Random random) {
        this.random = random;
        scheduleNext();
    }

    public void update(double deltaTime) {
        timerSeconds += deltaTime;

        if (activeSeconds > 0.0) {
            activeSeconds -= deltaTime;
            if (activeSeconds <= 0.0) {
                offsetX = 0;
                offsetY = 0;
            } else {
                offsetX = randomRangeInt();
                offsetY = randomRangeInt();
            }
            return;
        }

        if (timerSeconds >= nextJitterInSeconds) {
            timerSeconds = 0.0;
            activeSeconds = HudConfig.JITTER_DURATION_SECONDS;

            offsetX = randomRangeInt();
            offsetY = randomRangeInt();

            scheduleNext();
        }
    }

    public boolean isActive() {
        return activeSeconds > 0.0;
    }

    private void scheduleNext() {
        nextJitterInSeconds = randomRangeDouble();
    }

    private int randomRangeInt() {
        int bound = (HudConfig.JITTER_MAX_OFFSET_PIXELS + 4) + 1;
        return -4 + random.nextInt(bound);
    }

    private double randomRangeDouble() {
        return HudConfig.JITTER_MIN_INTERVAL_SECONDS + (HudConfig.JITTER_MAX_INTERVAL_SECONDS - HudConfig.JITTER_MIN_INTERVAL_SECONDS) * random.nextDouble();
    }
}