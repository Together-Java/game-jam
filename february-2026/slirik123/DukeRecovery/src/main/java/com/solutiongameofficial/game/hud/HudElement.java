package com.solutiongameofficial.game.hud;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HudElement {

    private static final Map<BloomKey, BufferedImage> BLOOM_CACHE = new ConcurrentHashMap<>();

    public final BufferedImage sourceWireframeImage;
    public final BufferedImage tintedWireframeImage;
    public final BufferedImage bloomImage;
    public final Color tintColor;

    public HudElement(BufferedImage sourceWireframeImage, Color tintColor) {
        this.sourceWireframeImage = sourceWireframeImage;
        this.tintColor = tintColor;

        tintedWireframeImage = HudTint.tintWhiteWireframe(
                sourceWireframeImage,
                tintColor,
                HudConfig.WIREFRAME_TINT_STRENGTH
        );

        BloomKey key = new BloomKey(sourceWireframeImage, tintColor, HudConfig.BLOOM_BLUR_RADIUS, HudConfig.BLOOM_LEVELS, HudConfig.BLOOM_THRESHOLD_ALPHA);
        bloomImage = BLOOM_CACHE.computeIfAbsent(key, k -> HudBloom.buildBloomLayer(sourceWireframeImage, tintColor));
    }

    private record BloomKey(
            BufferedImage sourceIdentity,
            Color tintColor,
            int blurRadius,
            int levels,
            int alphaThreshold
    ) { }
}