package com.solutiongameofficial.phase.duke;

import com.solutiongameofficial.graphics.MeshBounds;
import com.solutiongameofficial.graphics.MeshSimplifier;
import com.solutiongameofficial.graphics.Vector3;
import com.solutiongameofficial.graphics.WireMesh;
import com.solutiongameofficial.io.StlLoader;
import lombok.Getter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public final class MemoryDefragRenderer {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int YELLOW = 0xFFFFFF00;
    private static final int DARK_GRAY = 0xFF141414;
    private static final int MID_GRAY = 0xFF2A2A2A;

    private BufferedImage frame;
    private int[] pixels;

    private WireMesh mesh;
    @Getter private MeshBounds meshBounds;

    public void initialize() {
        frame = new BufferedImage(MemoryDefragState.WIDTH, MemoryDefragState.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();

        WireMesh rawMesh = StlLoader.loadFromResource("/stl/duke.stl");

        MeshBounds rawMeshBounds = MeshBounds.from(rawMesh);
        double rawMeshSize = Math.max(rawMeshBounds.sizeX(), Math.max(rawMeshBounds.sizeY(), rawMeshBounds.sizeZ()));
        if (rawMeshSize <= 0.0) {
            rawMeshSize = 1.0;
        }

        double quantizationCellSize = rawMeshSize * 0.012;

        this.mesh = MeshSimplifier.quantizeAndRebuild(rawMesh, quantizationCellSize);
        this.meshBounds = MeshBounds.from(mesh);
    }

    public BufferedImage render(MemoryDefragState state) {
        clear();

        renderWireframeDuke(state);
        renderMemoryColumn(state);

        return frame;
    }

    private void clear() {
        Arrays.fill(pixels, MemoryDefragRenderer.BLACK);
    }

    private void renderWireframeDuke(MemoryDefragState state) {
        double rotationY = state.timeAliveSeconds() * 0.65;
        double rotationX = -1;
        double rotationZ = 0;

        int screenCenterX = 950;
        int screenCenterY = 500;

        double maxMeshDimension = Math.max(meshBounds.sizeX(), Math.max(meshBounds.sizeY(), meshBounds.sizeZ()));
        double meshScale = 420.0 / (maxMeshDimension == 0.0 ? 1.0 : maxMeshDimension);

        // Corruption rising from feet
        // corruptionCut is a Y threshold in [minY...maxY] below which edges become yellow
        double originalMeshMinY = state.meshMinY();
        double originalMeshMaxY = state.meshMaxY();
        double corruptionProgress = state.modelCorruption();
        double corruptionCutOriginalSpace = originalMeshMinY + (originalMeshMaxY - originalMeshMinY) * corruptionProgress;

        int vertexCount = mesh.vertexCount();
        int[] projectedScreenXByVertex = new int[vertexCount];
        int[] projectedScreenYByVertex = new int[vertexCount];
        double[] rotatedYByVertex = new double[vertexCount];

        double meshBoundsSize = Math.max(meshBounds.sizeX(), Math.max(meshBounds.sizeY(), meshBounds.sizeZ()));
        double cameraDistanceZ = (meshBoundsSize == 0.0 ? 1.0 : meshBoundsSize) * 2.5;
        double focalLength = 900.0;

        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
            Vector3 vertex = mesh.vertex(vertexIndex);

            double centeredScaledX = (vertex.x() - meshBounds.centerX()) * meshScale;
            double centeredScaledY = (vertex.y() - meshBounds.centerY()) * meshScale;
            double centeredScaledZ = (vertex.z() - meshBounds.centerZ()) * meshScale;

            Vector3 rotatedVertex = rotateXYZ(centeredScaledX, centeredScaledY, centeredScaledZ, rotationX, rotationY, rotationZ);

            rotatedYByVertex[vertexIndex] = rotatedVertex.y();

            double cameraSpaceZ = rotatedVertex.z() + cameraDistanceZ;

            if (cameraSpaceZ <= 1.0) {
                projectedScreenXByVertex[vertexIndex] = Integer.MIN_VALUE;
                projectedScreenYByVertex[vertexIndex] = Integer.MIN_VALUE;
                continue;
            }

            int projectedX = (int) (screenCenterX + (rotatedVertex.x() * focalLength) / cameraSpaceZ);
            int projectedY = (int) (screenCenterY - (rotatedVertex.y() * focalLength) / cameraSpaceZ);

            projectedScreenXByVertex[vertexIndex] = projectedX;
            projectedScreenYByVertex[vertexIndex] = projectedY;
        }

        // CorruptionCut (which is in original mesh y-space) into rotated y-space approximately
        double corruptionCutRotatedSpaceY = getCorruptionCutRotatedSpaceY(originalMeshMaxY, originalMeshMinY, corruptionCutOriginalSpace);

        int edgeCount = mesh.edgeCount();
        for (int edgeIndex = 0; edgeIndex < edgeCount; edgeIndex++) {
            int vertexAIndex = mesh.edgeA(edgeIndex);
            int vertexBIndex = mesh.edgeB(edgeIndex);

            int screenAX = projectedScreenXByVertex[vertexAIndex];
            int screenAY = projectedScreenYByVertex[vertexAIndex];
            int screenBX = projectedScreenXByVertex[vertexBIndex];
            int screenBY = projectedScreenYByVertex[vertexBIndex];

            if (screenAX == Integer.MIN_VALUE || screenBX == Integer.MIN_VALUE) {
                continue;
            }

            // Prevent insane screen spanning lines (guards against over-merged simplification)
            int deltaX = screenAX - screenBX;
            int deltaY = screenAY - screenBY;
            if ((deltaX * deltaX + deltaY * deltaY) > (900 * 900)) {
                continue;
            }

            int edgeColor = (rotatedYByVertex[vertexAIndex] <= corruptionCutRotatedSpaceY || rotatedYByVertex[vertexBIndex] <= corruptionCutRotatedSpaceY)
                    ? YELLOW
                    : WHITE;

            drawLineClipped(screenAX, screenAY, screenBX, screenBY, edgeColor);
        }
    }

    private static double getCorruptionCutRotatedSpaceY(double originalMeshMaxY, double originalMeshMinY, double corruptionCutOriginalSpace) {
        double normalizedCutT = (originalMeshMaxY - originalMeshMinY) == 0.0
                ? 0.0
                : (corruptionCutOriginalSpace - originalMeshMinY) / (originalMeshMaxY - originalMeshMinY);

        // Approx rotated y range from bounds and current scale
        // This is not exact but works well for some corruptive visuals
        double approximateRotatedMinY = -420.0;
        double approximateRotatedMaxY = 420.0;
        return approximateRotatedMinY + (approximateRotatedMaxY - approximateRotatedMinY) * normalizedCutT;
    }

    private void renderMemoryColumn(MemoryDefragState state) {
        int columnX = 300;
        int columnY = 300;

        int columnWidth = 400;
        int columnHeight = 600;

        fillRect(columnX, columnY, columnWidth, columnHeight, DARK_GRAY);
        drawRect(columnX, columnY, columnWidth, columnHeight, MID_GRAY);

        int rowHeight = columnHeight / MemoryDefragState.ROW_COUNT;

        MemoryRow[] rows = state.rows();
        int selectedIndex = state.selectedIndex();

        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            int rowY = columnY + rowIndex * rowHeight;
            MemoryRow row = rows[rowIndex];

            if (row.isRemoved()) {
                fillRect(columnX + 6, rowY + 2, columnWidth - 12, rowHeight - 4, 0xFF0B0B0B);
                drawRect(columnX + 6, rowY + 2, columnWidth - 12, rowHeight - 4, 0xFF1A1A1A);
                continue;
            }

            boolean isSelected = rowIndex == selectedIndex;

            int baseColor = isSelected ? 0xFF1E1E1E : 0xFF101010;
            fillRect(columnX + 6, rowY + 2, columnWidth - 12, rowHeight - 4, baseColor);

            if (row.isCorrupted()) {
                fillRect(columnX + 6, rowY + 2, columnWidth - 12, rowHeight - 4, 0xFF3A3A00);
                drawRect(columnX + 6, rowY + 2, columnWidth - 12, rowHeight - 4, 0xFFFFFF00);

                drawCenteredGlyph(row.symbol(), columnX + columnWidth / 2, rowY + rowHeight / 2);
            } else {
                drawRect(columnX + 6, rowY + 2, columnWidth - 12, rowHeight - 4, 0xFF2A2A2A);
            }

            if (isSelected) {
                fillRect(columnX + 8, rowY + 4, 10, rowHeight - 8, 0xFF5C5C5C);
            }
        }
    }

    private static Vector3 rotateXYZ(double x, double y, double z, double angleX, double angleY, double angleZ) {
        // Rotate X
        double cosX = Math.cos(angleX);
        double sinX = Math.sin(angleX);
        double rotatedY1 = y * cosX - z * sinX;
        double rotatedZ1 = y * sinX + z * cosX;

        // Rotate Y
        double cosY = Math.cos(angleY);
        double sinY = Math.sin(angleY);
        double rotatedX2 = x * cosY + rotatedZ1 * sinY;
        double rotatedZ2 = -x * sinY + rotatedZ1 * cosY;

        // Rotate Z
        double cosZ = Math.cos(angleZ);
        double sinZ = Math.sin(angleZ);
        double rotatedX3 = rotatedX2 * cosZ - rotatedY1 * sinZ;
        double rotatedY3 = rotatedX2 * sinZ + rotatedY1 * cosZ;

        return new Vector3(rotatedX3, rotatedY3, rotatedZ2);
    }

    private void fillRect(int x, int y, int w, int h, int argb) {
        int clampedX0 = clamp(x, MemoryDefragState.WIDTH);
        int clampedY0 = clamp(y, MemoryDefragState.HEIGHT);
        int clampedX1 = clamp(x + w, MemoryDefragState.WIDTH);
        int clampedY1 = clamp(y + h, MemoryDefragState.HEIGHT);

        for (int pixelY = clampedY0; pixelY < clampedY1; pixelY++) {
            int rowBaseIndex = pixelY * MemoryDefragState.WIDTH;
            for (int pixelX = clampedX0; pixelX < clampedX1; pixelX++) {
                pixels[rowBaseIndex + pixelX] = argb;
            }
        }
    }

    private void drawRect(int x, int y, int w, int h, int argb) {
        int x1 = x + w - 1;
        int y1 = y + h - 1;

        drawLineClipped(x, y, x1, y, argb);
        drawLineClipped(x1, y, x1, y1, argb);
        drawLineClipped(x1, y1, x, y1, argb);
        drawLineClipped(x, y1, x, y, argb);
    }

    private void drawLineClipped(int x0, int y0, int x1, int y1, int argb) {
        if ((x0 < -200 && x1 < -200) || (x0 > MemoryDefragState.WIDTH + 200 && x1 > MemoryDefragState.WIDTH + 200)) {
            return;
        }
        if ((y0 < -200 && y1 < -200) || (y0 > MemoryDefragState.HEIGHT + 200 && y1 > MemoryDefragState.HEIGHT + 200)) {
            return;
        }

        int deltaXAbs = Math.abs(x1 - x0);
        int deltaYAbs = Math.abs(y1 - y0);

        int stepX = x0 < x1 ? 1 : -1;
        int stepY = y0 < y1 ? 1 : -1;

        int error = deltaXAbs - deltaYAbs;

        int currentX = x0;
        int currentY = y0;

        while (true) {
            if ((currentX | currentY) >= 0 && currentX < MemoryDefragState.WIDTH && currentY < MemoryDefragState.HEIGHT) {
                pixels[currentY * MemoryDefragState.WIDTH + currentX] = argb;
            }

            if (currentX == x1 && currentY == y1) {
                break;
            }

            int doubledError = error << 1;
            if (doubledError > -deltaYAbs) {
                error -= deltaYAbs;
                currentX += stepX;
            }
            if (doubledError < deltaXAbs) {
                error += deltaXAbs;
                currentY += stepY;
            }
        }
    }

    private void drawCenteredGlyph(char c, int centerX, int centerY) {
        // Minimal 5x7 glyphs for a few symbols. Unknown -> box
        boolean[][] glyph = Glyphs.get(c);
        int glyphHeight = glyph.length;
        int glyphWidth = glyph[0].length;

        int startX = centerX - glyphWidth / 2;
        int startY = centerY - glyphHeight / 2;

        for (int glyphY = 0; glyphY < glyphHeight; glyphY++) {
            int targetY = startY + glyphY;
            if (targetY < 0 || targetY >= MemoryDefragState.HEIGHT) {
                continue;
            }

            int rowBaseIndex = targetY * MemoryDefragState.WIDTH;

            for (int glyphX = 0; glyphX < glyphWidth; glyphX++) {
                if (!glyph[glyphY][glyphX]) {
                    continue;
                }

                int targetX = startX + glyphX;
                if (targetX < 0 || targetX >= MemoryDefragState.WIDTH) {
                    continue;
                }

                pixels[rowBaseIndex + targetX] = -256;
            }
        }
    }

    private static int clamp(int value, int hi) {
        return value < 0 ? 0 : Math.min(value, hi);
    }
}