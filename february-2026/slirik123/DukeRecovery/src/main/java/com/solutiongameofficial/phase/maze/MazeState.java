package com.solutiongameofficial.phase.maze;

import lombok.Getter;

import java.awt.*;
import java.util.Random;

@Getter
public final class MazeState {

    private final int gridWidth;
    private final int gridHeight;

    private final Direction[][] grid;

    private int cursorX;
    private int cursorY;

    private Point startPoint;
    private Point targetPoint;

    private int solvesCompleted = 0;

    public MazeState(int gridWidth, int gridHeight, Random random) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        grid = new Direction[gridHeight][gridWidth];

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                grid[y][x] = Direction.random(random);
            }
        }

        cursorX = gridWidth / 2;
        cursorY = gridHeight / 2;

        startPoint = new Point(1, 1);
        targetPoint = new Point(gridWidth - 2, gridHeight - 2);
    }

    public void moveCursor(int dx, int dy) {
        cursorX = clamp(cursorX + dx, gridWidth - 1);
        cursorY = clamp(cursorY + dy, gridHeight - 1);
    }

    public void rotateSelectedClockwise() {
        grid[cursorY][cursorX] = grid[cursorY][cursorX].clockwise();
    }

    public void rotateSelectedCounterClockwise() {
        grid[cursorY][cursorX] = grid[cursorY][cursorX].counterClockwise();
    }

    public void rotateCellClockwise(int x, int y) {
        grid[y][x] = grid[y][x].clockwise();
    }

    public void rotateCellCounterClockwise(int x, int y) {
        grid[y][x] = grid[y][x].counterClockwise();
    }

    public boolean pathExists() {
        return pathExists(startPoint, targetPoint);
    }

    public boolean advanceObjectiveIfSolved(int solvesRequired, Random random) {
        if (!pathExists()) {
            return false;
        }

        solvesCompleted++;

        startPoint = new Point(targetPoint.x, targetPoint.y);

        if (solvesCompleted < solvesRequired) {
            targetPoint = pickNewTargetNotEqualToStart(random);
        }

        return solvesCompleted >= solvesRequired;
    }

    private boolean pathExists(Point start, Point target) {
        int x = start.x;
        int y = start.y;

        int maxSteps = gridWidth * gridHeight;
        boolean[][] visited = new boolean[gridHeight][gridWidth];

        for (int step = 0; step < maxSteps; step++) {
            if (x == target.x && y == target.y) {
                return true;
            }

            if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
                return false;
            }

            if (visited[y][x]) {
                return false;
            }

            visited[y][x] = true;

            Direction direction = grid[y][x];
            x += direction.dx;
            y += direction.dy;
        }

        return false;
    }

    private Point pickNewTargetNotEqualToStart(Random random) {
        Point point;
        do {
            int x = random.nextInt(gridWidth);
            int y = random.nextInt(gridHeight);
            point = new Point(x, y);
        } while (point.equals(startPoint));

        return point;
    }

    private static int clamp(int value, int max) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, max);
    }
}
