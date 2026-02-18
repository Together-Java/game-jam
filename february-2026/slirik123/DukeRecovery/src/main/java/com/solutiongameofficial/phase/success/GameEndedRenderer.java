package com.solutiongameofficial.phase.success;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class GameEndedRenderer {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 540;

    private static final int BLACK = 0x000000;
    private static final int WHITE = 0xFFFFFF;

    private final BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    private final Font titleFont = new Font("Monospaced", Font.BOLD, 44);
    private final Font subtitleFont = new Font("Monospaced", Font.PLAIN, 16);
    private final Font storyFont = new Font("Monospaced", Font.PLAIN, 18);

    public BufferedImage render(GameEndedState state) {
        Graphics2D graphics = frame.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            clear(graphics);
            drawVignette(graphics, state.elapsedSeconds());
            drawScanlines(graphics, state.elapsedSeconds());
            drawCoreEmblem(graphics, state.elapsedSeconds());
            drawTextBlock(graphics, state);
            drawFadeOutIfFinished(graphics, state);
        } finally {
            graphics.dispose();
        }
        return frame;
    }

    private void clear(Graphics2D graphics) {
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setColor(new Color(BLACK));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawVignette(Graphics2D graphics, double timeSeconds) {
        int layers = 22;
        for (int index = 0; index < layers; index++) {
            float t = (float) index / (float) (layers - 1);
            float alpha = 0.03f + (t * 0.06f);
            int inset = (int) (t * 80.0);

            float pulse = (float) (0.5 + 0.5 * Math.sin(timeSeconds * 0.8));
            float finalAlpha = clamp01(alpha + 0.02f * pulse);

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, finalAlpha));
            graphics.setColor(new Color(0, 0, 0));
            graphics.drawRect(inset, inset, WIDTH - 1 - inset * 2, HEIGHT - 1 - inset * 2);
        }

        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawScanlines(Graphics2D graphics, double timeSeconds) {
        int step = 3;
        double drift = (timeSeconds * 24.0) % step;

        for (int y = 0; y < HEIGHT; y += step) {
            int yy = (int) Math.floor(y + drift) % HEIGHT;
            float alpha = 0.05f;

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            graphics.setColor(new java.awt.Color(0, 0, 0));
            graphics.drawLine(0, yy, WIDTH, yy);
        }

        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawCoreEmblem(Graphics2D graphics, double timeSeconds) {
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2 - 90;

        double rotation = timeSeconds * 0.9;

        int ringRadius = 68;
        int tickCount = 24;

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        graphics.setColor(new java.awt.Color(WHITE));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawOval(centerX - ringRadius, centerY - ringRadius, ringRadius * 2, ringRadius * 2);

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        graphics.fillOval(centerX - 8, centerY - 8, 16, 16);

        AffineTransform old = graphics.getTransform();
        try {
            graphics.translate(centerX, centerY);
            graphics.rotate(rotation);

            for (int count = 0; count < tickCount; count++) {
                double a = (2.0 * Math.PI) * ((double) count / (double) tickCount);

                float emphasis = (float) (0.35 + 0.65 * Math.max(0.0, Math.sin(a * 2.0 + timeSeconds)));
                float alpha = clamp01(0.15f + 0.30f * emphasis);

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                graphics.setColor(new java.awt.Color(WHITE));
                graphics.setStroke(new BasicStroke(2f));

                int x1 = (int) Math.round(Math.cos(a) * (ringRadius + 6));
                int y1 = (int) Math.round(Math.sin(a) * (ringRadius + 6));
                int x2 = (int) Math.round(Math.cos(a) * (ringRadius + 18));
                int y2 = (int) Math.round(Math.sin(a) * (ringRadius + 18));

                graphics.drawLine(x1, y1, x2, y2);
            }
        } finally {
            graphics.setTransform(old);
            graphics.setComposite(AlphaComposite.SrcOver);
        }
    }

    private void drawTextBlock(Graphics2D graphics, GameEndedState state) {
        int centerX = WIDTH / 2;

        graphics.setColor(new java.awt.Color(WHITE));
        graphics.setFont(titleFont);

        String title = "GAME COMPLETE";
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.drawString(title, centerX - titleWidth / 2, 150);

        graphics.setFont(subtitleFont);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));

        String subTitle = "Integrity restored. The machine remembers you.";
        int subWidth = graphics.getFontMetrics().stringWidth(subTitle);
        graphics.drawString(subTitle, centerX - subWidth / 2, 180);

        graphics.setComposite(AlphaComposite.SrcOver);

        graphics.setFont(storyFont);
        int left = 140;
        int top = 240;
        int lineHeight = 24;

        GameEndedStoryText storyText = state.storyText();
        for (int index = 0; index < storyText.lineCount(); index++) {
            String line = storyText.visibleLine(index);

            if (line == null || line.isEmpty()) {
                continue;
            }

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
            graphics.setColor(new java.awt.Color(WHITE));
            graphics.drawString(line, left, top + index * lineHeight);
        }

        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private void drawFadeOutIfFinished(Graphics2D graphics, GameEndedState state) {
        if (state.storyText().isFullyRevealed()) {
            return;
        }

        double seconds = state.storyText().secondsSinceFullyRevealed();
        if (seconds <= 0.0) {
            return;
        }

        float alpha = clamp01((float) (seconds / 0.8));
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        graphics.setColor(new java.awt.Color(0, 0, 0));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setComposite(AlphaComposite.SrcOver);
    }

    private float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        return Math.min(value, 1.0f);
    }
}