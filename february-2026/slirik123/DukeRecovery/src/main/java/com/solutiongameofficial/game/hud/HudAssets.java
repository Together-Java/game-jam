package com.solutiongameofficial.game.hud;

import com.solutiongameofficial.io.ResourceLoader;

import java.awt.*;

public final class HudAssets {

    public final HudElement name;
    public final HudElement objective;
    public final HudElement stealth;
    public final HudElement integrity;
    public final HudElement integrityLow;
    public final HudElement notifications;

    public HudAssets() {
        name = load("hud/VeyName.png", HudConfig.WIREFRAME_TINT);
        objective = load("hud/Objective.png", HudConfig.WIREFRAME_TINT);
        stealth = load("hud/Stealth.png", HudConfig.WIREFRAME_TINT);
        integrity = load("hud/Integrity.png", HudConfig.WIREFRAME_TINT);
        integrityLow = load("hud/IntegrityLow.png", new Color(117, 74, 42));
        notifications = load("hud/Notifications.png", HudConfig.WIREFRAME_TINT);
    }

    private static HudElement load(String path, Color tintColor) {
        return new HudElement(ResourceLoader.loadImage(path), tintColor);
    }
}