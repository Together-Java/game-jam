package com.solutiongameofficial.phase.maze;

import java.util.Random;

public final class CorruptedSnake {

    private final double snakeSpawnSecondsMax;
    private final double snakeStepSecondsMax;

    private boolean active = false;
    private boolean isRow = true;
    private int lineIndex = 0;
    private int headIndex = 0;
    private int direction = 1;

    private double spawnSeconds = 0.0;
    private double stepSeconds = 0.0;

    public CorruptedSnake(double snakeSpawnSecondsMax, double snakeStepSecondsMax) {
        this.snakeSpawnSecondsMax = snakeSpawnSecondsMax;
        this.snakeStepSecondsMax = snakeStepSecondsMax;
    }

    public Overlay getOverlay(int gridWidth, int gridHeight) {
        if (!active) {
            return null;
        }

        return new Overlay(isRow, lineIndex, headIndex, direction, gridWidth, gridHeight);
    }

    public boolean update(double deltaTime, Random random, MazeState mazeState) {
        boolean mutated = false;

        spawnSeconds += deltaTime;

        if (!active && spawnSeconds >= snakeSpawnSecondsMax) {
            spawnSeconds = 0.0;
            start(random, mazeState.getGridWidth(), mazeState.getGridHeight());
        }

        if (!active) {
            return false;
        }

        stepSeconds += deltaTime;

        while (stepSeconds >= snakeStepSecondsMax) {
            stepSeconds -= snakeStepSecondsMax;

            headIndex += direction;

            if ((headIndex & 3) == 0) {
                mutateRandomArrowOnSnakeLine(random, mazeState);
                mutated = true;
            }

            int limit = isRow ? mazeState.getGridWidth() : mazeState.getGridHeight();
            if (headIndex < 0 || headIndex >= limit) {
                active = false;
                headIndex = 0;
                direction = 1;
                break;
            }
        }

        return mutated;
    }

    private void start(Random random, int gridWidth, int gridHeight) {
        active = true;
        isRow = random.nextBoolean();

        if (isRow) {
            lineIndex = random.nextInt(gridHeight);
            direction = random.nextBoolean() ? 1 : -1;
            headIndex = direction == 1 ? 0 : gridWidth - 1;
        } else {
            lineIndex = random.nextInt(gridWidth);
            direction = random.nextBoolean() ? 1 : -1;
            headIndex = direction == 1 ? 0 : gridHeight - 1;
        }
    }

    private void mutateRandomArrowOnSnakeLine(Random random, MazeState mazeState) {
        int gridWidth = mazeState.getGridWidth();
        int gridHeight = mazeState.getGridHeight();

        boolean clockwise = random.nextBoolean();

        int x;
        int y;

        if (isRow) {
            x = random.nextInt(gridWidth);
            y = lineIndex;
        } else {
            x = lineIndex;
            y = random.nextInt(gridHeight);
        }

        if (clockwise) {
            mazeState.rotateCellClockwise(x, y);
        } else {
            mazeState.rotateCellCounterClockwise(x, y);
        }
    }

    public record Overlay(boolean isRow, int lineIndex, int headIndex, int direction, int gridWidth, int gridHeight) { }
}