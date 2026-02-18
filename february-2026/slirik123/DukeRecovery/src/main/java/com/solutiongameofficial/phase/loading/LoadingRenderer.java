package com.solutiongameofficial.phase.loading;

import com.solutiongameofficial.graphics.parser.ImageToAsciiParser;
import com.solutiongameofficial.graphics.parser.Parser;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public final class LoadingRenderer {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 540;

    private static final int SEGMENTS = 16;
    private static final int VISIBLE_SEGMENTS = 4;

    private static final int BACKGROUND_RGB = 0x000000;
    private static final int OVAL_RGB = 0xFFFFFF;

    private static final double ROTATIONS_PER_SECOND = 0.35;

    private final BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final Parser asciiParser = new ImageToAsciiParser();

    public BufferedImage render(LoadingState state) {
        Graphics2D graphics = frame.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            clear(graphics);
            drawSpinner(graphics, state.elapsedSeconds());
        } finally {
            graphics.dispose();
        }

        return asciiParser.parse(frame);
    }

    private void clear(Graphics2D graphics) {
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setColor(new java.awt.Color(BACKGROUND_RGB));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawSpinner(Graphics2D graphics, double elapsedSeconds) {
        int centerX = WIDTH / 2;
        int centerY = HEIGHT / 2;

        int radiusPixels = Math.min(WIDTH, HEIGHT) / 6;
        int ovalLengthPixels = Math.min(WIDTH, HEIGHT) / 8;
        int ovalThicknessPixels = Math.min(WIDTH, HEIGHT) / 16;

        double baseAngleRadians = elapsedSeconds * (2.0 * Math.PI) * ROTATIONS_PER_SECOND;

        double stepRadians = (2.0 * Math.PI) / (double) SEGMENTS;

        int headIndex = (int) Math.floor((baseAngleRadians / (2.0 * Math.PI)) * SEGMENTS) % SEGMENTS;
        if (headIndex < 0) {
            headIndex += SEGMENTS;
        }

        float[] alphaByTrail = new float[] { 1.0f, 0.70f, 0.42f, 0.18f };

        for (int trail = 0; trail < VISIBLE_SEGMENTS; trail++) {
            int segmentIndex = headIndex - trail;
            while (segmentIndex < 0) {
                segmentIndex += SEGMENTS;
            }
            segmentIndex %= SEGMENTS;

            double theta = (segmentIndex * stepRadians) + baseAngleRadians;

            float alpha = alphaByTrail[Math.min(trail, alphaByTrail.length - 1)];
            drawRadialOval(graphics, centerX, centerY, radiusPixels, theta, ovalThicknessPixels, ovalLengthPixels, alpha);
        }
    }

    private void drawRadialOval(
            Graphics2D graphics,
            int centerX,
            int centerY,
            int radiusPixels,
            double thetaRadians,
            int ovalThicknessPixels,
            int ovalLengthPixels,
            float alpha
    ) {
        AffineTransform oldTransform = graphics.getTransform();
        java.awt.Composite oldComposite = graphics.getComposite();

        try {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp01(alpha)));
            graphics.setColor(new java.awt.Color(OVAL_RGB));

            graphics.translate(centerX, centerY);
            graphics.rotate(thetaRadians);
            graphics.translate(0, -radiusPixels);

            int x = -ovalThicknessPixels / 2;
            int y = -ovalLengthPixels / 2;

            graphics.fillOval(x, y, ovalThicknessPixels, ovalLengthPixels);
        } finally {
            graphics.setTransform(oldTransform);
            graphics.setComposite(oldComposite);
        }
    }

    private float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        return Math.min(value, 1.0f);
    }
}