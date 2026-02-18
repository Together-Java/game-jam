package com.solutiongameofficial.game.hud;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class HudDynamicTextElement {

    private final Font font;
    private final Color tintColor;

    private String text = "";

    public BufferedImage tintedTextImage;
    public BufferedImage bloomTextImage;

    public HudDynamicTextElement(Color tintColor, int fontSize) {
        this.tintColor = tintColor;
        font = new Font(HudConfig.HUD_TEXT_FONT_NAME, Font.PLAIN, fontSize);

        rebuildImages();
    }

    public void setText(String newText) {
        if (newText == null) {
            newText = "";
        }
        if (newText.equals(text)) {
            return;
        }
        text = newText;
        rebuildImages();
    }

    private void rebuildImages() {
        if (text.isEmpty()) {
            tintedTextImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            bloomTextImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            return;
        }

        BufferedImage measurementImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D measurementGraphics = measurementImage.createGraphics();
        FontMetrics fontMetrics;
        try {
            measurementGraphics.setFont(font);
            fontMetrics = measurementGraphics.getFontMetrics();
        } finally {
            measurementGraphics.dispose();
        }

        int textWidth = Math.max(1, fontMetrics.stringWidth(text));
        int textHeight = Math.max(1, fontMetrics.getHeight());

        BufferedImage rawTextImage = new BufferedImage(textWidth, textHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D textGraphics = rawTextImage.createGraphics();
        try {
            textGraphics.setComposite(AlphaComposite.Src);
            textGraphics.setColor(new Color(0, 0, 0, 0));
            textGraphics.fillRect(0, 0, textWidth, textHeight);

            textGraphics.setComposite(AlphaComposite.SrcOver);
            textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            textGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            textGraphics.setFont(font);
            textGraphics.setColor(Color.WHITE);

            int baselineY = fontMetrics.getAscent();
            textGraphics.drawString(text, 0, baselineY);
        } finally {
            textGraphics.dispose();
        }

        tintedTextImage = HudTint.tintWhiteWireframe(rawTextImage, tintColor, 1.0f);
        bloomTextImage = HudBloom.buildBloomLayer(rawTextImage, tintColor);
    }
}