package com.solutiongameofficial.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ScreenCompositor {

    private final int screenWidth;
    private final int screenHeight;
    private final FitMode fitMode;

    private final BufferedImage backBuffer;
    private final Graphics2D backGraphics;

    public ScreenCompositor(int screenWidth, int screenHeight, FitMode fitMode) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.fitMode = fitMode;

        backBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        backGraphics = backBuffer.createGraphics();

        backGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        backGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    public BufferedImage compose(BufferedImage phaseImage, BufferedImage hudImage) {
        if (phaseImage == null) {
            throw new IllegalArgumentException("phaseImage is null");
        }
        if (hudImage == null) {
            throw new IllegalArgumentException("hudImage is null");
        }

        backGraphics.setComposite(AlphaComposite.Src);
        backGraphics.setColor(Color.BLACK);
        backGraphics.fillRect(0, 0, screenWidth, screenHeight);

        drawPhase(phaseImage);

        backGraphics.setComposite(AlphaComposite.SrcOver);
        backGraphics.drawImage(hudImage, 0, 0, null);

        return backBuffer;
    }

    private void drawPhase(BufferedImage phaseImage) {
        int srcW = phaseImage.getWidth();
        int srcH = phaseImage.getHeight();

        switch (fitMode) {
            case TOP_LEFT -> backGraphics.drawImage(phaseImage, 0, 0, null);
            case STRETCH -> backGraphics.drawImage(phaseImage, 0, 0, screenWidth, screenHeight, null);
            case FIT_LETTERBOX -> {
                double scale = Math.min(screenWidth / (double) srcW, screenHeight / (double) srcH);
                int dstW = (int) Math.round(srcW * scale);
                int dstH = (int) Math.round(srcH * scale);

                int dstX = (screenWidth - dstW) / 2;
                int dstY = (screenHeight - dstH) / 2;

                backGraphics.drawImage(phaseImage, dstX, dstY, dstW, dstH, null);
            }
        }
    }

    // Not really using other apart from fit letterbox, but for experimenting it's great :p
    public enum FitMode {
        STRETCH,
        FIT_LETTERBOX,
        TOP_LEFT
    }
}