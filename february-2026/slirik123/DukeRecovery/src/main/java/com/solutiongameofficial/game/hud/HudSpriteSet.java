package com.solutiongameofficial.game.hud;

import java.awt.image.BufferedImage;

public record HudSpriteSet(BufferedImage tinted, BufferedImage bloom) {

    public HudSpriteSet(HudElement hudElement) {
        this(hudElement.tintedWireframeImage, hudElement.bloomImage);
    }
}