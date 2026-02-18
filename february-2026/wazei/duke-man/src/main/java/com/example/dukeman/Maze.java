package com.example.dukeman;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Maze {
    public static final int TILE_SIZE = 25;

    /**
     * 0 = path with food
     * 1 = wall
     * 2 = empty path
     * 3 = power food
     */
    private static final int[][] MAP = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
        {1,3,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,3,1},
        {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
        {1,0,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,0,1,1,1,1,1,2,1,1,2,1,1,1,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,1,1,1,2,1,1,2,1,1,1,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,2,2,2,2,2,2,2,2,2,2,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,2,1,1,1,2,2,1,1,1,2,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,2,2,2,1,2,2,2,2,2,2,1,2,2,2,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,2,1,2,2,2,2,2,2,1,2,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,2,1,1,1,1,1,1,1,1,2,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,2,2,2,2,2,2,2,2,2,2,1,1,0,1,1,1,1,1,1},
        {1,1,1,1,1,1,0,1,1,2,1,1,1,1,1,1,1,1,2,1,1,0,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
        {1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,1,1,1,1,0,1},
        {1,3,0,0,1,1,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,1,1,0,0,3,1},
        {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
        {1,1,1,0,1,1,0,1,1,0,1,1,1,1,1,1,1,1,0,1,1,0,1,1,0,1,1,1},
        {1,0,0,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,0,1,1,1,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    private final Set<Point> food = new HashSet<>();
    private final Set<Point> powerPellets = new HashSet<>();

    public Maze() {
        reset();
    }

    public void reset() {
        food.clear();
        powerPellets.clear();

        for (int row = 0; row < MAP.length; row++) {
            for (int col = 0; col < MAP[row].length; col++) {
                if (MAP[row][col] == 0) {
                    food.add(new Point(col, row));
                } else if (MAP[row][col] == 3) {
                    powerPellets.add(new Point(col, row));
                }
            }
        }
    }

    public int getWidth() {
        return MAP[0].length;
    }

    public int getHeight() {
        return MAP.length;
    }

    public boolean isWall(int tileX, int tileY) {
        if (tileX < 0 || tileX >= getWidth() || tileY < 0 || tileY >= getHeight()) {
            return true;
        }
        return MAP[tileY][tileX] == 1;
    }

    /**
     * 0 = nothing eaten
     * 1 = regular food
     * 2 = power pellet
     */
    public int eatFood(int tileX, int tileY) {
        Point p = new Point(tileX, tileY);
        if (food.remove(p)) {
            return 1;
        }
        if (powerPellets.remove(p)) {
            return 2;
        }
        return 0;
    }

    public int getFoodCount() {
        return food.size() + powerPellets.size();
    }

    public void draw(Graphics2D g) {
        drawWalls(g);
        drawFood(g);
        drawPowerFood(g);
    }

    private void drawWalls(Graphics2D g) {
        g.setColor(Color.BLUE);

        for (int row = 0; row < MAP.length; row++) {
            for (int col = 0; col < MAP[row].length; col++) {
                if (MAP[row][col] == 1) {
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        g.setColor(new Color(66, 66, 255));

        for (int row = 0; row < MAP.length; row++) {
            for (int col = 0; col < MAP[row].length; col++) {
                if (MAP[row][col] == 1) {
                    g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE - 1, TILE_SIZE - 1);
                }
            }
        }
    }

    private void drawFood(Graphics2D g) {
        g.setColor(Color.WHITE);

        for (Point p : food) {
            int centerX = p.x * TILE_SIZE + TILE_SIZE / 2;
            int centerY = p.y * TILE_SIZE + TILE_SIZE / 2;
            g.fillOval(centerX - 3, centerY - 3, 6, 6);
        }
    }

    private void drawPowerFood(Graphics2D g) {
        g.setColor(Color.WHITE);

        for (Point p : powerPellets) {
            int centerX = p.x * TILE_SIZE + TILE_SIZE / 2;
            int centerY = p.y * TILE_SIZE + TILE_SIZE / 2;
            g.fillOval(centerX - 7, centerY - 7, 14, 14);
        }
    }
}
