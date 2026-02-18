package com.solutiongameofficial.game;

import lombok.Getter;

public final class FramePacer {

    private final long frameNanoseconds;
    private final double maximumDeltaSeconds;

    private long lastNanoseconds = System.nanoTime();
    private long nextRenderNanoseconds = lastNanoseconds;
    @Getter
    private boolean frameDue = true;

    public FramePacer(long frameNanoseconds, double maximumDeltaSeconds) {
        this.frameNanoseconds = frameNanoseconds;
        this.maximumDeltaSeconds = maximumDeltaSeconds;
        this.nextRenderNanoseconds = lastNanoseconds + frameNanoseconds;
    }

    public double consumeDeltaSeconds() {
        long now = System.nanoTime();
        double deltaSeconds = (now - lastNanoseconds) / 1_000_000_000d;
        lastNanoseconds = now;

        if (deltaSeconds > maximumDeltaSeconds) {
            deltaSeconds = maximumDeltaSeconds;
        }

        frameDue = now >= nextRenderNanoseconds;
        return deltaSeconds;
    }

    public void markFramePresented() {
        long now = System.nanoTime();
        do {
            nextRenderNanoseconds += frameNanoseconds;
        } while (nextRenderNanoseconds <= now);
        frameDue = false;
    }

    public void sleepUntilNextFrame() {
        long sleepNanoseconds = nextRenderNanoseconds - System.nanoTime();
        if (sleepNanoseconds <= 0) {
            Thread.onSpinWait();
            return;
        }

        try {
            long sleepMilliseconds = sleepNanoseconds / 1_000_000L;
            int sleepExtraNanoseconds = (int) (sleepNanoseconds % 1_000_000L);
            Thread.sleep(sleepMilliseconds, sleepExtraNanoseconds);
        } catch (InterruptedException ignored) { }
    }
}