package com.solutiongameofficial.phase.maze;

import java.util.Random;

public enum Direction {
    LEFT('<', -1, 0),
    RIGHT('>', 1, 0),
    UP('^', 0, -1),
    DOWN('v', 0, 1);

    public final char symbol;
    public final int dx;
    public final int dy;

    Direction(char symbol, int dx, int dy) {
        this.symbol = symbol;
        this.dx = dx;
        this.dy = dy;
    }

    public Direction clockwise() {
        return switch (this) {
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }

    public Direction counterClockwise() {
        return switch (this) {
            case UP -> LEFT;
            case LEFT -> DOWN;
            case DOWN -> RIGHT;
            case RIGHT -> UP;
        };
    }

    public static Direction random(Random random) {
        Direction[] values = values();
        return values[random.nextInt(values.length)];
    }
}