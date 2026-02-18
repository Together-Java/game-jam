package com.solutiongameofficial.game.hud;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HudTint {

    public static BufferedImage tintWhiteWireframe(BufferedImage sourceWireframeImage, Color tintColor, float tintStrength) {
        if (tintStrength <= 0f) {
            return sourceWireframeImage;
        }
        if (tintStrength > 1f) {
            tintStrength = 1f;
        }

        int sourceWidth = sourceWireframeImage.getWidth();
        int sourceHeight = sourceWireframeImage.getHeight();

        int[] sourcePixelsArgb = sourceWireframeImage.getRGB(0, 0, sourceWidth, sourceHeight, null, 0, sourceWidth);
        int[] tintedPixelsArgb = new int[sourcePixelsArgb.length];

        int tintRed = tintColor.getRed();
        int tintGreen = tintColor.getGreen();
        int tintBlue = tintColor.getBlue();

        for (int pixelIndex = 0; pixelIndex < sourcePixelsArgb.length; pixelIndex++) {
            int sourceArgb = sourcePixelsArgb[pixelIndex];

            int alpha = (sourceArgb >>> 24) & 0xFF;
            if (alpha == 0) {
                tintedPixelsArgb[pixelIndex] = 0;
                continue;
            }

            int sourceRed = (sourceArgb >>> 16) & 0xFF;
            int sourceGreen = (sourceArgb >>> 8) & 0xFF;
            int sourceBlue = sourceArgb & 0xFF;

            int luminance = (54 * sourceRed + 183 * sourceGreen + 19 * sourceBlue) >>> 8;

            int tintedRed = (tintRed * luminance) / 255;
            int tintedGreen = (tintGreen * luminance) / 255;
            int tintedBlue = (tintBlue * luminance) / 255;

            if (tintStrength < 1f) {
                tintedRed = (int) (sourceRed + (tintedRed - sourceRed) * tintStrength);
                tintedGreen = (int) (sourceGreen + (tintedGreen - sourceGreen) * tintStrength);
                tintedBlue = (int) (sourceBlue + (tintedBlue - sourceBlue) * tintStrength);
            }

            tintedPixelsArgb[pixelIndex] = (alpha << 24) | (tintedRed << 16) | (tintedGreen << 8) | tintedBlue;
        }

        BufferedImage tintedWireframeImage = new BufferedImage(sourceWidth, sourceHeight, BufferedImage.TYPE_INT_ARGB);
        tintedWireframeImage.setRGB(0, 0, sourceWidth, sourceHeight, tintedPixelsArgb, 0, sourceWidth);
        return tintedWireframeImage;
    }
}