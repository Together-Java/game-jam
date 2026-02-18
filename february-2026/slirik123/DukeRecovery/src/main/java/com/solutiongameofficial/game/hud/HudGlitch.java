package com.solutiongameofficial.game.hud;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HudGlitch {

    public static void drawGlitched(Random random,
                                    Graphics2D graphics2D,
                                    BufferedImage image,
                                    int x,
                                    int y,
                                    int width,
                                    int height)
    {
        int sliceCount = HudConfig.GLITCH_SLICE_COUNT;
        int sliceHeight = Math.max(1, height / sliceCount);

        Shape oldClip = graphics2D.getClip();

        for (int count = 0; count < sliceCount; count++) {
            int sliceY = count * sliceHeight;
            int realSliceHeight = (count == sliceCount - 1) ? (height - sliceY) : sliceHeight;

            int offsetX = randomRangeInt(random);

            graphics2D.setClip(x, y + sliceY, width, realSliceHeight);
            graphics2D.drawImage(image, x + offsetX, y, width, height, null);
        }

        graphics2D.setClip(oldClip);
    }

    public static void drawGlitched(Random random, Graphics2D graphics2D, BufferedImage image, int x, int y) {
        drawGlitched(random, graphics2D, image, x, y, image.getWidth(), image.getHeight());
    }

    private static int randomRangeInt(Random random) {
        int bound = (HudConfig.GLITCH_SLICE_MAX_OFFSET + 6) + 1;

        return random.nextInt(bound) - 6;
    }
}