package com.example.dukeman.entity;

import com.example.dukeman.Direction;
import com.example.dukeman.Maze;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Ghost extends Entity {
    private static final int RESPAWN_DELAY = 20;
    private static final Color FRIGHTENED_COLOR = new Color(50, 50, 200);

    private final Color color;
    private final int moveDelay;
    private final int startTileX;
    private final int startTileY;

    private Direction direction;
    private int moveCounter;

    private boolean frightened;
    private int respawnTimer;

    public Ghost(int tileX, int tileY, Color color, int moveDelay) {
        super(tileX * Maze.TILE_SIZE, tileY * Maze.TILE_SIZE);
        this.startTileX = tileX;
        this.startTileY = tileY;
        this.color = color;
        this.direction = randomDirection();
        this.moveDelay = moveDelay;
    }

    public void reset() {
        setX(startTileX * Maze.TILE_SIZE);
        setY(startTileY * Maze.TILE_SIZE);
        direction = randomDirection();
        moveCounter = 0;
        frightened = false;
        respawnTimer = 0;
    }

    public void setFrightened(boolean frightened) {
        this.frightened = frightened;
    }

    public boolean isFrightened() {
        return frightened;
    }

    public void respawn() {
        setX(startTileX * Maze.TILE_SIZE);
        setY(startTileY * Maze.TILE_SIZE);
        direction = randomDirection();
        frightened = false;
        respawnTimer = RESPAWN_DELAY;
    }

    public void update(Maze maze, Entity player) {
        if (respawnTimer > 0) {
            respawnTimer--;
            return;
        }

        moveCounter++;

        int delay = frightened ? moveDelay + 2 : moveDelay;
        if (moveCounter < delay) {
            return;
        }

        moveCounter = 0;

        int currentTileX = getX() / Maze.TILE_SIZE;
        int currentTileY = getY() / Maze.TILE_SIZE;
        int playerTileX = player.getX() / Maze.TILE_SIZE;
        int playerTileY = player.getY() / Maze.TILE_SIZE;

        if (frightened) {
            if (ThreadLocalRandom.current().nextInt(100) < 70) {
                int dx = playerTileX - currentTileX;
                int dy = playerTileY - currentTileY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    direction = dx > 0 ? Direction.LEFT : Direction.RIGHT;
                } else if (dy != 0) {
                    direction = dy > 0 ? Direction.UP : Direction.DOWN;
                }
            }
        } else if (ThreadLocalRandom.current().nextInt(100) < 60) {
            int dx = playerTileX - currentTileX;
            int dy = playerTileY - currentTileY;
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = dx > 0 ? Direction.RIGHT : Direction.LEFT;
            } else if (dy != 0) {
                direction = dy > 0 ? Direction.DOWN : Direction.UP;
            }
        }

        int nextX = currentTileX;
        int nextY = currentTileY;

        switch (direction) {
            case UP -> nextY--;
            case DOWN -> nextY++;
            case LEFT -> nextX--;
            case RIGHT -> nextX++;
        }

        if (!maze.isWall(nextX, nextY)) {
            setX(nextX * Maze.TILE_SIZE);
            setY(nextY * Maze.TILE_SIZE);
        } else {
            for (int i = 0; i < 4; i++) {
                Direction newDir = randomDirection();
                int testX = currentTileX;
                int testY = currentTileY;

                switch (newDir) {
                    case UP -> testY--;
                    case DOWN -> testY++;
                    case LEFT -> testX--;
                    case RIGHT -> testX++;
                }

                if (!maze.isWall(testX, testY)) {
                    direction = newDir;
                    setX(testX * Maze.TILE_SIZE);
                    setY(testY * Maze.TILE_SIZE);
                    break;
                }
            }
        }
    }

    public boolean collidesWith(Entity other) {
        if (respawnTimer > 0) {
            return false;
        }
        int myTileX = getX() / Maze.TILE_SIZE;
        int myTileY = getY() / Maze.TILE_SIZE;
        int otherTileX = other.getX() / Maze.TILE_SIZE;
        int otherTileY = other.getY() / Maze.TILE_SIZE;
        return myTileX == otherTileX && myTileY == otherTileY;
    }

    @Override
    public void draw(Graphics2D g) {
        if (respawnTimer > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        int size = Maze.TILE_SIZE - 4;

        g.setColor(frightened ? FRIGHTENED_COLOR : color);
        g.fillRect(getX() + 2, getY() + 2, size, size);
        g.setColor(Color.WHITE);
        g.fillOval(getX() + 5, getY() + 6, 6, 6);
        g.fillOval(getX() + 14, getY() + 6, 6, 6);

        if (frightened) {
            g.setColor(Color.WHITE);
            g.fillOval(getX() + 7, getY() + 8, 2, 2);
            g.fillOval(getX() + 16, getY() + 8, 2, 2);
        } else {
            g.setColor(Color.BLACK);
            g.fillOval(getX() + 7, getY() + 8, 3, 3);
            g.fillOval(getX() + 16, getY() + 8, 3, 3);
        }

        if (respawnTimer > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private Direction randomDirection() {
        return Direction.values()[ThreadLocalRandom.current().nextInt(4)];
    }
}
