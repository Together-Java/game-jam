package com.solutiongameofficial.phase.checksum;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ChecksumRenderer {

    private static final int IMAGE_WIDTH = 1000;
    private static final int IMAGE_HEIGHT = 1000;

    private static final int BASELINE_Y = 500;
    private static final int START_X = 100;

    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SELECTED_COLOR = new Color(0x8f8646);
    private static final Color UNDERLINE_COLOR = new Color(255, 255, 255, 60);
    private static final Color HUD_COLOR = new Color(255, 255, 255, 160);

    private static final Font MAIN_FONT = new Font("Arial", Font.BOLD, 128);
    private static final Font HUD_FONT = new Font("Arial", Font.PLAIN, 64);

    private BufferedImage normalLayer;

    private FontMetrics mainFontMetrics;
    private int characterHeight;
    private int characterSpacing;
    private int cellWidth;

    private final AlphaComposite[] alphaCompositeCache = new AlphaComposite[256];

    public void initialize() {
        normalLayer = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = normalLayer.createGraphics();
        try {
            graphics2D.setFont(MAIN_FONT);
            mainFontMetrics = graphics2D.getFontMetrics();

            characterHeight = mainFontMetrics.getAscent();
            cellWidth = mainFontMetrics.charWidth('W') + 6;
            characterSpacing = cellWidth;

            for (int alpha = 0; alpha < 256; alpha++) {
                alphaCompositeCache[alpha] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f);
            }
        } finally {
            graphics2D.dispose();
        }
    }

    public BufferedImage render(ChecksumState state) {
        if (normalLayer == null) {
            initialize();
        }

        Graphics2D graphics2D = normalLayer.createGraphics();
        try {
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            graphics2D.setColor(BACKGROUND_COLOR);
            graphics2D.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

            graphics2D.setFont(MAIN_FONT);

            int selectedIndex = state.getSelectedIndex();
            int columns = state.getColumnCount();

            for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
                int characterX = START_X + (columnIndex * characterSpacing);

                drawQuantumCharacter(graphics2D, state, columnIndex, characterX, selectedIndex);

                if (columnIndex == selectedIndex) {
                    graphics2D.setColor(UNDERLINE_COLOR);
                    int underlineY = BASELINE_Y + 10;
                    graphics2D.fillRect(characterX - 2, underlineY, characterSpacing - 2, 4);
                }
            }

            graphics2D.setColor(HUD_COLOR);
            graphics2D.setFont(HUD_FONT);
            graphics2D.drawString("Expected: " + state.getExpectedSequence(), 400, 920);

            return normalLayer;
        } finally {
            graphics2D.dispose();
        }
    }

    private void drawQuantumCharacter(Graphics2D graphics2D,
                                      ChecksumState state,
                                      int columnIndex,
                                      int characterX,
                                      int selectedIndex)
    {
        char[] columnSymbols = state.getSymbols(columnIndex);
        int symbolCount = columnSymbols.length;

        double position = state.getPosition(columnIndex);

        double floored = Math.floor(position);
        int baseIndex = floorMod((int) floored, symbolCount);
        double fraction = position - floored;
        int nextIndex = (baseIndex + 1) % symbolCount;

        char baseCharacter = columnSymbols[baseIndex];
        char nextCharacter = columnSymbols[nextIndex];

        int baseAlpha255 = clampAlpha255((int) Math.round((1.0 - fraction) * 255.0));
        int nextAlpha255 = 255 - baseAlpha255;

        int offsetPixels = (int) Math.round(fraction * characterHeight);

        int baseOffsetX = (cellWidth - mainFontMetrics.charWidth(baseCharacter)) / 2;
        int nextOffsetX = (cellWidth - mainFontMetrics.charWidth(nextCharacter)) / 2;

        Composite originalComposite = graphics2D.getComposite();

        graphics2D.setColor(columnIndex == selectedIndex ? SELECTED_COLOR : TEXT_COLOR);

        graphics2D.setComposite(alphaCompositeCache[baseAlpha255]);
        graphics2D.drawString(String.valueOf(baseCharacter), characterX + baseOffsetX, BASELINE_Y - offsetPixels);

        graphics2D.setComposite(alphaCompositeCache[nextAlpha255]);
        graphics2D.drawString(String.valueOf(nextCharacter), characterX + nextOffsetX, BASELINE_Y + (characterHeight - offsetPixels));

        graphics2D.setComposite(originalComposite);
    }

    private static int clampAlpha255(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 255);
    }

    private static int floorMod(int value, int mod) {
        int result = value % mod;
        return result < 0 ? result + mod : result;
    }
}