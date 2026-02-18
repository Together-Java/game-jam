package com.solutiongameofficial.graphics.parser;

import java.awt.image.BufferedImage;

public record ChromaticAberrationParser(int baseShiftPixels, int extraShiftPixels) implements Parser {

    /**
     * @param baseShiftPixels  shift applied everywhere
     * @param extraShiftPixels additional shift near edges
     */
    public ChromaticAberrationParser {
        if (baseShiftPixels < 0) {
            throw new IllegalArgumentException("baseShiftPixels must be >= 0");
        }
        if (extraShiftPixels < 0) {
            throw new IllegalArgumentException("extraShiftPixels must be >= 0");
        }

    }

    @Override
    public BufferedImage parse(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] inputPixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] outputPixels = new int[inputPixels.length];

        int centerX = (width - 1) / 2;
        int centerY = (height - 1) / 2;

        int maxDistance = Math.max(centerX, centerY);
        if (maxDistance <= 0) {
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            output.setRGB(0, 0, width, height, inputPixels, 0, width);
            return output;
        }

        for (int y = 0; y < height; y++) {
            int dy = y - centerY;
            int absDy = abs(dy);

            int rowIndex = y * width;

            for (int x = 0; x < width; x++) {
                int dx = x - centerX;
                int absDx = abs(dx);

                int distance = Math.max(absDx, absDy);
                int shift = baseShiftPixels + (extraShiftPixels * distance) / maxDistance;

                int dirX = sign(dx);
                int dirY = sign(dy);

                int redX = clampInt(x + dirX * shift, width - 1);
                int redY = clampInt(y + dirY * shift, height - 1);

                int blueX = clampInt(x - dirX * shift, width - 1);
                int blueY = clampInt(y - dirY * shift, height - 1);

                int greenIndex = rowIndex + x;
                int redIndex = redY * width + redX;
                int blueIndex = blueY * width + blueX;

                int greenArgb = inputPixels[greenIndex];
                int redArgb = inputPixels[redIndex];
                int blueArgb = inputPixels[blueIndex];

                int a = (greenArgb >>> 24) & 0xFF;
                int r = (redArgb >>> 16) & 0xFF;
                int g = (greenArgb >>> 8) & 0xFF;
                int b = blueArgb & 0xFF;

                outputPixels[greenIndex] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        output.setRGB(0, 0, width, height, outputPixels, 0, width);
        return output;
    }

    private static int abs(int value) {
        return value < 0 ? -value : value;
    }

    private static int sign(int value) {
        return Integer.compare(value, 0);
    }

    private static int clampInt(int value, int max) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, max);
    }
}