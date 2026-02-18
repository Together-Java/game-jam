package com.solutiongameofficial.graphics.parser;

import com.solutiongameofficial.graphics.AsciiColorMode;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ImageToAsciiParser implements Parser {

    private static final char[] SYMBOLS = new char[] {
            ' ', '.', ',', '-', ':', ';', '*', '+', 'g', '&', '$', '#', '%', 'A', '@'
    };

    private static final String[] SYMBOL_STRINGS = new String[SYMBOLS.length];
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 6);

    private int cellSize = 6;
    private AsciiColorMode asciiColorMode = AsciiColorMode.MONOCHROME;

    private int cachedCellSize = -1;
    private FontMetrics cachedFontMetrics;
    private int[] cachedDrawXOffsetBySymbolIndex;
    private int cachedDrawYOffset;

    public BufferedImage parse(BufferedImage image, int cellSize) {
        this.cellSize = cellSize;
        return parse(image);
    }

    public BufferedImage parse(BufferedImage image, int cellSize, AsciiColorMode asciiColorMode) {
        this.cellSize = cellSize;
        this.asciiColorMode = asciiColorMode;
        return parse(image);
    }

    public BufferedImage parse(BufferedImage image, AsciiColorMode asciiColorMode) {
        this.asciiColorMode = asciiColorMode;
        return parse(image);
    }

    @Override
    public BufferedImage parse(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] inputPixels = image.getRGB(0, 0, width, height, null, 0, width);

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = output.createGraphics();
        try {
            graphics2D.setColor(Color.BLACK);
            graphics2D.fillRect(0, 0, width, height);

            graphics2D.setFont(FONT);

            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            ensureMetricsCached(graphics2D);

            int cellsX = (width + cellSize - 1) / cellSize;
            int cellsY = (height + cellSize - 1) / cellSize;

            boolean keepColors = asciiColorMode == AsciiColorMode.KEEP_COLORS;

            for (int cellY = 0; cellY < cellsY; cellY++) {
                int y0 = cellY * cellSize;
                int y1 = Math.min(y0 + cellSize, height);

                for (int cellX = 0; cellX < cellsX; cellX++) {
                    int x0 = cellX * cellSize;
                    int x1 = Math.min(x0 + cellSize, width);

                    long sumLuminance = 0;
                    long sumR = 0;
                    long sumG = 0;
                    long sumB = 0;
                    int count = 0;

                    for (int y = y0; y < y1; y++) {
                        int rowIndex = y * width;
                        for (int x = x0; x < x1; x++) {
                            int argb = inputPixels[rowIndex + x];

                            int r = (argb >>> 16) & 0xFF;
                            int g = (argb >>> 8) & 0xFF;
                            int b = argb & 0xFF;

                            int luminance = (54 * r + 183 * g + 19 * b) >>> 8;

                            sumLuminance += luminance;

                            if (keepColors) {
                                sumR += r;
                                sumG += g;
                                sumB += b;
                            }

                            count++;
                        }
                    }

                    if (count <= 0) {
                        continue;
                    }

                    int avgLuminance = (int) (sumLuminance / count); // 0..255
                    int symbolIndex = mapLumaToSymbolIndex(avgLuminance);

                    if (keepColors) {
                        int avgR = (int) (sumR / count);
                        int avgG = (int) (sumG / count);
                        int avgB = (int) (sumB / count);
                        graphics2D.setColor(new Color(avgR, avgG, avgB));
                    } else {
                        graphics2D.setColor(Color.WHITE);
                    }

                    int drawX = x0 + cachedDrawXOffsetBySymbolIndex[symbolIndex];
                    int drawY = y0 + cachedDrawYOffset;

                    graphics2D.drawString(SYMBOL_STRINGS[symbolIndex], drawX, drawY);
                }
            }

            return output;
        } finally {
            graphics2D.dispose();
        }
    }

    private void ensureMetricsCached(Graphics2D graphics2D) {
        if (cachedCellSize == cellSize && cachedFontMetrics != null) {
            return;
        }

        cachedCellSize = cellSize;

        cachedFontMetrics = graphics2D.getFontMetrics();
        int cachedFontHeight = cachedFontMetrics.getHeight();
        int cachedTextAscent = cachedFontMetrics.getAscent();

        cachedDrawXOffsetBySymbolIndex = new int[SYMBOLS.length];

        for (int index = 0; index < SYMBOLS.length; index++) {
            int width = cachedFontMetrics.stringWidth(SYMBOL_STRINGS[index]);
            cachedDrawXOffsetBySymbolIndex[index] = (cellSize - width) / 2;
        }

        cachedDrawYOffset = (cellSize - cachedFontHeight) / 2 + cachedTextAscent;
    }

    private static int mapLumaToSymbolIndex(int luminance0to255) {
        if (luminance0to255 <= 0) {
            return 0;
        }
        if (luminance0to255 >= 255) {
            return SYMBOLS.length - 1;
        }

        return (luminance0to255 * (SYMBOLS.length - 1)) / 255;
    }

    static {
        for (int index = 0; index < SYMBOLS.length; index++) {
            SYMBOL_STRINGS[index] = String.valueOf(SYMBOLS[index]);
        }
    }
}