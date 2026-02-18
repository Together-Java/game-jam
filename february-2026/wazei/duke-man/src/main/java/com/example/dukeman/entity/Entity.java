package com.example.dukeman.entity;

import com.example.dukeman.Direction;
import com.example.dukeman.Maze;

import java.awt.*;

public abstract class Entity {
    public abstract void draw(Graphics2D graphics2D);

    private Direction direction = Direction.NONE;
    private Direction nextDirection = Direction.NONE;

    private int x;
    private int y;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setDirection(Direction direction) {
        this.nextDirection = direction;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void update(Maze maze) {
        int tileX = x / Maze.TILE_SIZE;
        int tileY = y / Maze.TILE_SIZE;

        if (nextDirection != Direction.NONE) {
            int[] nextTile = getNextTile(tileX, tileY, nextDirection);
            if (!maze.isWall(nextTile[0], nextTile[1])) {
                direction = nextDirection;
            }
        }

        if (direction != Direction.NONE) {
            int[] nextTile = getNextTile(tileX, tileY, direction);
            if (!maze.isWall(nextTile[0], nextTile[1])) {
                x = nextTile[0] * Maze.TILE_SIZE;
                y = nextTile[1] * Maze.TILE_SIZE;
            }
        }
    }


    private int[] getNextTile(int tileX, int tileY, Direction dir) {
        int nextX = tileX;
        int nextY = tileY;
        switch (dir) {
            case UP -> nextY--;
            case DOWN -> nextY++;
            case LEFT -> nextX--;
            case RIGHT -> nextX++;
        }
        return new int[]{nextX, nextY};
    }

}
