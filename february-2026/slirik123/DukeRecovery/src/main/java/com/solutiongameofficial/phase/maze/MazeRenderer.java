package com.solutiongameofficial.phase.maze;

import com.solutiongameofficial.graphics.parser.Parser;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class MazeRenderer {

    private final Parser asciiParser;

    private BufferedImage corruptedLayer = null;
    private boolean corruptedLayerDirty = true;

    public MazeRenderer(Parser asciiParser) {
        this.asciiParser = asciiParser;
    }

    public void markCorruptedLayerDirty() {
        corruptedLayerDirty = true;
    }

    public BufferedImage render(MazeState mazeState,
                                boolean solvedFlashActive,
                                double solvedFlashSeconds,
                                int solvesRequired,
                                CorruptedSnake.Overlay snakeOverlay)
    {
        int cellSizePixels = 64;
        int paddingPixels = 80;

        int imageWidth = paddingPixels * 2 + mazeState.getGridWidth() * cellSizePixels;
        int imageHeight = paddingPixels * 2 + mazeState.getGridHeight() * cellSizePixels + 120;

        BufferedImage normalLayer = renderNormalLayer(mazeState, solvedFlashActive, solvedFlashSeconds, solvesRequired, imageWidth, imageHeight, cellSizePixels, paddingPixels);

        if (corruptedLayer == null || corruptedLayerDirty) {
            corruptedLayer = asciiParser.parse(normalLayer);
            corruptedLayerDirty = false;
        }

        BufferedImage finalImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = finalImage.createGraphics();
        try {
            graphics2D.drawImage(normalLayer, 0, 0, null);

            if (snakeOverlay != null) {
                overlaySnakeStrip(graphics2D, mazeState, snakeOverlay, cellSizePixels, paddingPixels);
            }

            return finalImage;
        } finally {
            graphics2D.dispose();
        }
    }

    private BufferedImage renderNormalLayer(MazeState mazeState,
                                            boolean solvedFlashActive,
                                            double solvedFlashSeconds,
                                            int solvesRequired,
                                            int imageWidth,
                                            int imageHeight,
                                            int cellSizePixels,
                                            int paddingPixels)
    {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        try {
            graphics2D.setColor(Color.BLACK);
            graphics2D.fillRect(0, 0, imageWidth, imageHeight);

            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            Font font = new Font(Font.MONOSPACED, Font.BOLD, 32);
            graphics2D.setFont(font);
            FontMetrics fontMetrics = graphics2D.getFontMetrics();

            boolean drawFlash = solvedFlashActive && ((int) (solvedFlashSeconds * 10) % 2 == 0);

            Direction[][] grid = mazeState.getGrid();
            Point startPoint = mazeState.getStartPoint();
            Point targetPoint = mazeState.getTargetPoint();

            for (int y = 0; y < mazeState.getGridHeight(); y++) {
                for (int x = 0; x < mazeState.getGridWidth(); x++) {
                    int pixelX = paddingPixels + x * cellSizePixels;
                    int pixelY = paddingPixels + y * cellSizePixels;

                    boolean isCursor = x == mazeState.getCursorX() && y == mazeState.getCursorY();
                    boolean isStart = x == startPoint.x && y == startPoint.y;
                    boolean isTarget = x == targetPoint.x && y == targetPoint.y;

                    if (isCursor) {
                        graphics2D.setColor(new Color(255, 255, 255, 30));
                        graphics2D.fillRect(pixelX, pixelY, cellSizePixels, cellSizePixels);
                    }

                    if (isStart) {
                        graphics2D.setColor(new Color(0, 255, 0, 35));
                        graphics2D.fillRect(pixelX, pixelY, cellSizePixels, cellSizePixels);
                    }

                    if (isTarget) {
                        graphics2D.setColor(new Color(255, 0, 0, 35));
                        graphics2D.fillRect(pixelX, pixelY, cellSizePixels, cellSizePixels);
                    }

                    graphics2D.setColor(new Color(255, 255, 255, 25));
                    graphics2D.drawRect(pixelX, pixelY, cellSizePixels, cellSizePixels);

                    char symbol = grid[y][x].symbol;

                    graphics2D.setColor(drawFlash ? new Color(255, 255, 255, 200) : Color.WHITE);

                    String text = String.valueOf(symbol);
                    int textWidth = fontMetrics.stringWidth(text);
                    int textAscent = fontMetrics.getAscent();

                    int drawX = pixelX + (cellSizePixels - textWidth) / 2;
                    int drawY = pixelY + (cellSizePixels - fontMetrics.getHeight()) / 2 + textAscent;

                    graphics2D.drawString(text, drawX, drawY);
                }
            }

            graphics2D.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            graphics2D.setColor(new Color(255, 255, 255, 180));

            graphics2D.drawString("Path Routing", paddingPixels, imageHeight - 80);
            graphics2D.drawString("Solves: " + mazeState.getSolvesCompleted() + " / " + solvesRequired, paddingPixels, imageHeight - 60);
            graphics2D.drawString("Controls: Move with arrows. Double tap to rotate.", paddingPixels, imageHeight - 40);

            return image;
        } finally {
            graphics2D.dispose();
        }
    }

    private void overlaySnakeStrip(Graphics2D graphics2D,
                                   MazeState mazeState,
                                   CorruptedSnake.Overlay overlay,
                                   int cellSizePixels,
                                   int paddingPixels)
    {
        if (corruptedLayer == null) {
            return;
        }

        int gridPixelWidth = mazeState.getGridWidth() * cellSizePixels;
        int gridPixelHeight = mazeState.getGridHeight() * cellSizePixels;

        int revealedCells = overlay.isRow()
                ? Math.abs(overlay.headIndex() - (overlay.direction() == 1 ? 0 : overlay.gridWidth() - 1)) + 1
                : Math.abs(overlay.headIndex() - (overlay.direction() == 1 ? 0 : overlay.gridHeight() - 1)) + 1;

        if (overlay.isRow()) {
            int rowY = paddingPixels + overlay.lineIndex() * cellSizePixels;

            int revealWidth = revealedCells * cellSizePixels;
            int revealX = overlay.direction() == 1
                    ? paddingPixels
                    : paddingPixels + gridPixelWidth - revealWidth;

            graphics2D.drawImage(
                    corruptedLayer,
                    revealX, rowY, revealX + revealWidth, revealX + cellSizePixels,
                    revealX, rowY, revealX + revealWidth, rowY + cellSizePixels,
                    null
            );
        } else {
            int colX = paddingPixels + overlay.lineIndex() * cellSizePixels;

            int revealHeight = revealedCells * cellSizePixels;
            int revealY = overlay.direction() == 1
                    ? paddingPixels
                    : paddingPixels + gridPixelHeight - revealHeight;

            graphics2D.drawImage(
                    corruptedLayer,
                    colX, revealY, colX + cellSizePixels, revealY + revealHeight,
                    colX, revealY, colX + cellSizePixels, colX + cellSizePixels,
                    null
            );
        }
    }
}
