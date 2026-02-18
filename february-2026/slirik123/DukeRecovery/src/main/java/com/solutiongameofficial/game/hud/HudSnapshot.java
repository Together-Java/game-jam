package com.solutiongameofficial.game.hud;

public record HudSnapshot(
        String objectiveText,
        String stealthText,
        String integrityText,
        char notificationsSpinnerChar,
        int offsetX,
        int offsetY,
        boolean glitch,
        double shearX,
        int integrityPercent,
        int glitchSeed
) { }