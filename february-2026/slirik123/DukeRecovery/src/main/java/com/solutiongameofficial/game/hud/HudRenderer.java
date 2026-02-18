package com.solutiongameofficial.game.hud;

import com.solutiongameofficial.graphics.AdditiveComposite;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public final class HudRenderer {

    private final Random random = new Random(1337);

    private final HudSpriteSet name;
    private final HudSpriteSet objective;
    private final HudSpriteSet stealth;
    private final HudSpriteSet integrity;
    private final HudSpriteSet integrityLow;
    private final HudSpriteSet notifications;

    private final HudDynamicTextElement objectiveTextElement = new HudDynamicTextElement(HudConfig.WIREFRAME_TINT, 32);
    private final HudDynamicTextElement stealthTextElement = new HudDynamicTextElement(HudConfig.WIREFRAME_TINT, 42);
    private final HudDynamicTextElement integrityTextElement = new HudDynamicTextElement(HudConfig.WIREFRAME_TINT, 50);
    private final HudDynamicTextElement notificationsSpinnerTextElement = new HudDynamicTextElement(HudConfig.WIREFRAME_TINT, 128);

    private final BufferedImage[] buffers = new BufferedImage[] {
            new BufferedImage(HudConfig.HUD_WIDTH, HudConfig.HUD_HEIGHT, BufferedImage.TYPE_INT_ARGB),
            new BufferedImage(HudConfig.HUD_WIDTH, HudConfig.HUD_HEIGHT, BufferedImage.TYPE_INT_ARGB)
    };

    private int bufferIndex = 0;

    public HudRenderer() {
        HudAssets assets = new HudAssets();

        name = new HudSpriteSet(assets.name);
        objective = new HudSpriteSet(assets.objective);
        stealth = new HudSpriteSet(assets.stealth);
        integrity = new HudSpriteSet(assets.integrity);
        integrityLow = new HudSpriteSet(assets.integrityLow);
        notifications = new HudSpriteSet(assets.notifications);
    }

    public BufferedImage render(HudSnapshot snapshot) {
        objectiveTextElement.setText(snapshot.objectiveText());
        stealthTextElement.setText(snapshot.stealthText());
        integrityTextElement.setText(snapshot.integrityText());
        notificationsSpinnerTextElement.setText(String.valueOf(snapshot.notificationsSpinnerChar()));

        BufferedImage target = buffers[bufferIndex];
        bufferIndex = (bufferIndex + 1) & 1;

        Graphics2D graphics2D = target.createGraphics();
        try {
            graphics2D.setComposite(AlphaComposite.Src);
            graphics2D.setColor(new Color(0, 0, 0, 0));
            graphics2D.fillRect(0, 0, HudConfig.HUD_WIDTH, HudConfig.HUD_HEIGHT);

            graphics2D.setComposite(AlphaComposite.SrcOver);

            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            AffineTransform originalTransform = graphics2D.getTransform();

            AffineTransform hudTransform = new AffineTransform();
            hudTransform.translate(snapshot.offsetX(), snapshot.offsetY());
            if (snapshot.glitch()) {
                hudTransform.shear(snapshot.shearX(), 0.0);
            }

            graphics2D.transform(hudTransform);

            if (snapshot.glitch()) {
                random.setSeed(snapshot.glitchSeed());
                drawGlitched(graphics2D);
            } else {
                drawNormal(graphics2D);
            }

            graphics2D.setTransform(originalTransform);
        } finally {
            graphics2D.dispose();
        }

        return target;
    }

    private void drawNormal(Graphics2D graphics2D) {
        Composite oldComposite = graphics2D.getComposite();

        graphics2D.setComposite(new AdditiveComposite(1));
        graphics2D.drawImage(name.bloom(), HudLayout.NAME_X, HudLayout.NAME_Y, HudLayout.NAME_W, HudLayout.NAME_H, null);
        graphics2D.drawImage(objective.bloom(), HudLayout.OBJECTIVE_X, HudLayout.OBJECTIVE_Y, null);
        graphics2D.drawImage(stealth.bloom(), HudLayout.STEALTH_X, HudLayout.STEALTH_Y, null);
        graphics2D.drawImage(integrity.bloom(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y, null);
        graphics2D.drawImage(integrityLow.bloom(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y, null);
        graphics2D.drawImage(notifications.bloom(), HudLayout.NOTIFICATIONS_X, HudLayout.NOTIFICATIONS_Y, null);

        graphics2D.drawImage(objectiveTextElement.bloomTextImage, HudLayout.OBJECTIVE_TEXT_X, HudLayout.OBJECTIVE_TEXT_Y, null);
        graphics2D.drawImage(stealthTextElement.bloomTextImage, HudLayout.STEALTH_TEXT_X, HudLayout.STEALTH_TEXT_Y, null);
        graphics2D.drawImage(integrityTextElement.bloomTextImage, HudLayout.INTEGRITY_TEXT_X, HudLayout.INTEGRITY_TEXT_Y, null);
        graphics2D.drawImage(notificationsSpinnerTextElement.bloomTextImage, HudLayout.NOTIFICATIONS_SPINNER_TEXT_X, HudLayout.NOTIFICATIONS_SPINNER_TEXT_Y, null);

        graphics2D.setComposite(oldComposite);
        graphics2D.drawImage(name.tinted(), HudLayout.NAME_X, HudLayout.NAME_Y, HudLayout.NAME_W, HudLayout.NAME_H, null);
        graphics2D.drawImage(objective.tinted(), HudLayout.OBJECTIVE_X, HudLayout.OBJECTIVE_Y, null);
        graphics2D.drawImage(stealth.tinted(), HudLayout.STEALTH_X, HudLayout.STEALTH_Y, null);
        graphics2D.drawImage(integrity.tinted(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y, null);
        graphics2D.drawImage(integrityLow.tinted(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y, null);
        graphics2D.drawImage(notifications.tinted(), HudLayout.NOTIFICATIONS_X, HudLayout.NOTIFICATIONS_Y, null);

        graphics2D.drawImage(objectiveTextElement.tintedTextImage, HudLayout.OBJECTIVE_TEXT_X, HudLayout.OBJECTIVE_TEXT_Y, null);
        graphics2D.drawImage(stealthTextElement.tintedTextImage, HudLayout.STEALTH_TEXT_X, HudLayout.STEALTH_TEXT_Y, null);
        graphics2D.drawImage(integrityTextElement.tintedTextImage, HudLayout.INTEGRITY_TEXT_X, HudLayout.INTEGRITY_TEXT_Y, null);
        graphics2D.drawImage(notificationsSpinnerTextElement.tintedTextImage, HudLayout.NOTIFICATIONS_SPINNER_TEXT_X, HudLayout.NOTIFICATIONS_SPINNER_TEXT_Y, null);
    }

    private void drawGlitched(Graphics2D graphics2D) {
        Composite oldComposite = graphics2D.getComposite();

        graphics2D.setComposite(new AdditiveComposite(1));
        HudGlitch.drawGlitched(random, graphics2D, name.bloom(), HudLayout.NAME_X, HudLayout.NAME_Y, HudLayout.NAME_W, HudLayout.NAME_H);
        HudGlitch.drawGlitched(random, graphics2D, objective.bloom(), HudLayout.OBJECTIVE_X, HudLayout.OBJECTIVE_Y);
        HudGlitch.drawGlitched(random, graphics2D, stealth.bloom(), HudLayout.STEALTH_X, HudLayout.STEALTH_Y);
        HudGlitch.drawGlitched(random, graphics2D, integrity.bloom(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y);
        HudGlitch.drawGlitched(random, graphics2D, integrityLow.bloom(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y);
        HudGlitch.drawGlitched(random, graphics2D, notifications.bloom(), HudLayout.NOTIFICATIONS_X, HudLayout.NOTIFICATIONS_Y);

        HudGlitch.drawGlitched(random, graphics2D, objectiveTextElement.bloomTextImage, HudLayout.OBJECTIVE_TEXT_X, HudLayout.OBJECTIVE_TEXT_Y);
        HudGlitch.drawGlitched(random, graphics2D, stealthTextElement.bloomTextImage, HudLayout.STEALTH_TEXT_X, HudLayout.STEALTH_TEXT_Y);
        HudGlitch.drawGlitched(random, graphics2D, integrityTextElement.bloomTextImage, HudLayout.INTEGRITY_TEXT_X, HudLayout.INTEGRITY_TEXT_Y);
        HudGlitch.drawGlitched(random, graphics2D, notificationsSpinnerTextElement.bloomTextImage, HudLayout.NOTIFICATIONS_SPINNER_TEXT_X, HudLayout.NOTIFICATIONS_SPINNER_TEXT_Y);

        graphics2D.setComposite(oldComposite);
        HudGlitch.drawGlitched(random, graphics2D, name.tinted(), HudLayout.NAME_X, HudLayout.NAME_Y, HudLayout.NAME_W, HudLayout.NAME_H);
        HudGlitch.drawGlitched(random, graphics2D, objective.tinted(), HudLayout.OBJECTIVE_X, HudLayout.OBJECTIVE_Y);
        HudGlitch.drawGlitched(random, graphics2D, stealth.tinted(), HudLayout.STEALTH_X, HudLayout.STEALTH_Y);
        HudGlitch.drawGlitched(random, graphics2D, integrity.tinted(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y);
        HudGlitch.drawGlitched(random, graphics2D, integrityLow.tinted(), HudLayout.INTEGRITY_X, HudLayout.INTEGRITY_Y);
        HudGlitch.drawGlitched(random, graphics2D, notifications.tinted(), HudLayout.NOTIFICATIONS_X, HudLayout.NOTIFICATIONS_Y);

        HudGlitch.drawGlitched(random, graphics2D, objectiveTextElement.tintedTextImage, HudLayout.OBJECTIVE_TEXT_X, HudLayout.OBJECTIVE_TEXT_Y);
        HudGlitch.drawGlitched(random, graphics2D, stealthTextElement.tintedTextImage, HudLayout.STEALTH_TEXT_X, HudLayout.STEALTH_TEXT_Y);
        HudGlitch.drawGlitched(random, graphics2D, integrityTextElement.tintedTextImage, HudLayout.INTEGRITY_TEXT_X, HudLayout.INTEGRITY_TEXT_Y);
        HudGlitch.drawGlitched(random, graphics2D, notificationsSpinnerTextElement.tintedTextImage, HudLayout.NOTIFICATIONS_SPINNER_TEXT_X, HudLayout.NOTIFICATIONS_SPINNER_TEXT_Y);
    }
}