package com.solutiongameofficial.game.hud;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HudConfig {

    public static final int HUD_WIDTH = 1920;
    public static final int HUD_HEIGHT = 1080;

    public static final int BLOOM_LEVELS = 5;
    public static final float BLOOM_INTENSITY = 50f;
    public static final int BLOOM_THRESHOLD_ALPHA = 10;
    public static final int BLOOM_BLUR_RADIUS = 5;

    public static final Color WIREFRAME_TINT = new Color(255, 176, 92, 255);
    public static final float WIREFRAME_TINT_STRENGTH = 1.0f;

    public static final String HUD_TEXT_FONT_NAME = "Arial";

    public static final char[] NOTIFICATIONS_SPINNER_FRAMES = new char[] { '◴', 0x25F6, '◶', 0x25F3};
    public static final double NOTIFICATIONS_SPINNER_FRAME_SECONDS = 0.06;

    public static final double JITTER_MIN_INTERVAL_SECONDS = 0.25;
    public static final double JITTER_MAX_INTERVAL_SECONDS = 1.20;
    public static final double JITTER_DURATION_SECONDS = 0.045;
    public static final int JITTER_MAX_OFFSET_PIXELS = 4;

    public static final int GLITCH_SLICE_COUNT = 6;
    public static final int GLITCH_SLICE_MAX_OFFSET = 6;
    public static final double GLITCH_SHEAR_MAX = 0.015;
}