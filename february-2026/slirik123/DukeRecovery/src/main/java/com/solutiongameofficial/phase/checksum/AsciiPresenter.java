package com.solutiongameofficial.phase.checksum;

import com.solutiongameofficial.graphics.AsciiColorMode;
import com.solutiongameofficial.graphics.parser.ImageToAsciiParser;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;

@RequiredArgsConstructor
public final class AsciiPresenter {

    private static final double ASCII_REFRESH_SECONDS = 1.0 / 20.0;

    private final ImageToAsciiParser asciiParser;

    private BufferedImage asciiLayer;

    private double refreshAccumulatorSeconds = 0.0;
    private boolean dirty = true;

    public void initialize() {
        dirty = true;
        refreshAccumulatorSeconds = 0.0;
        asciiLayer = null;
    }

    public void markDirty() {
        dirty = true;
    }

    /**
     * Keeps timing for when we are allowed to refresh.
     * @param forceRefresh if true, refresh is allowed immediately
     */
    public void update(double deltaTime, boolean forceRefresh) {
        if (forceRefresh) {
            refreshAccumulatorSeconds = ASCII_REFRESH_SECONDS;
            dirty = true;
            return;
        }

        refreshAccumulatorSeconds += deltaTime;
        if (refreshAccumulatorSeconds >= ASCII_REFRESH_SECONDS) {
            dirty = true;
        }
    }

    public BufferedImage get(BufferedImage normalLayer) {
        if (normalLayer == null) {
            throw new IllegalArgumentException("normalLayer is null");
        }

        if (!dirty && asciiLayer != null) {
            return asciiLayer;
        }

        if (refreshAccumulatorSeconds < ASCII_REFRESH_SECONDS && asciiLayer != null) {
            // Not time yet, keep old
            dirty = false;
            return asciiLayer;
        }

        refreshAccumulatorSeconds = 0.0;
        dirty = false;

        asciiLayer = asciiParser.parse(normalLayer, 4, AsciiColorMode.KEEP_COLORS);
        return asciiLayer;
    }
}