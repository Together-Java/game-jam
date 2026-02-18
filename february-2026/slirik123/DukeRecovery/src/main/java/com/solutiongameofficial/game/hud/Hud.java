package com.solutiongameofficial.game.hud;

import lombok.Setter;

import java.util.Random;

public final class Hud {

    private final Random random = new Random(1337);
    private final HudJitter jitter = new HudJitter(random);

    private double notificationsSpinnerTimerSeconds = 0.0;
    private int notificationsSpinnerFrameIndex = 0;

    @Setter
    private String objectiveText = "Undefined";

    private char notificationsSpinnerChar = HudConfig.NOTIFICATIONS_SPINNER_FRAMES[0];

    public Hud() {
        updateNotificationsSpinnerChar();
    }

    public void update(double deltaTime) {
        jitter.update(deltaTime);

        notificationsSpinnerTimerSeconds += deltaTime;
        if (notificationsSpinnerTimerSeconds >= HudConfig.NOTIFICATIONS_SPINNER_FRAME_SECONDS) {
            notificationsSpinnerTimerSeconds = 0.0;
            notificationsSpinnerFrameIndex = (notificationsSpinnerFrameIndex + 1) % HudConfig.NOTIFICATIONS_SPINNER_FRAMES.length;
            updateNotificationsSpinnerChar();
        }
    }

    public HudSnapshot snapshot() {
        boolean glitch = jitter.isActive();

        double shearX = 0.0;
        if (glitch) {
            shearX = randomGlitchShearX();
        }

        int glitchSeed = random.nextInt();

        String stealthText = "-- Detected";
        String integrityText = "23%";
        return new HudSnapshot(
                objectiveText,
                stealthText,
                integrityText,
                notificationsSpinnerChar,
                jitter.getOffsetX(),
                jitter.getOffsetY(),
                glitch,
                shearX,
                23,
                glitchSeed
        );
    }

    private void updateNotificationsSpinnerChar() {
        notificationsSpinnerChar = HudConfig.NOTIFICATIONS_SPINNER_FRAMES[notificationsSpinnerFrameIndex];
    }

    private double randomGlitchShearX() {
        return -HudConfig.GLITCH_SHEAR_MAX + (HudConfig.GLITCH_SHEAR_MAX * 2.0) * random.nextDouble();
    }
}