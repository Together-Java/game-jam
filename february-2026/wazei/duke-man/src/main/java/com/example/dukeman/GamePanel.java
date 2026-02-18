package com.example.dukeman;

import com.example.dukeman.entity.Entity;
import com.example.dukeman.entity.Ghost;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private static final int PLAYER_START_X = 1;
    private static final int PLAYER_START_Y = 1;
    private static final int FRIGHTENED_DURATION = 50;

    private final Entity player;
    private final Maze maze;
    private final List<Ghost> ghosts;

    private int score;
    private boolean gameOver;
    private boolean gameWon ;
    private int frightenedTimer;

    public GamePanel(Entity player, Maze maze) {
        this.player = player;
        this.maze = maze;
        this.ghosts = new ArrayList<>();
        this.setBackground(Color.BLACK);

        ghosts.add(new Ghost(11, 11, Color.RED, 3));
        ghosts.add(new Ghost(13, 11, Color.PINK, 4));
        ghosts.add(new Ghost(14, 11, Color.CYAN, 4));
        ghosts.add(new Ghost(16, 11, Color.ORANGE, 5));
    }

    public boolean isGameEnded() {
        return gameOver || gameWon;
    }

    public void restart() {
        player.setX(PLAYER_START_X * Maze.TILE_SIZE);
        player.setY(PLAYER_START_Y * Maze.TILE_SIZE);
        player.setDirection(Direction.NONE);
        maze.reset();

        for (Ghost ghost : ghosts) {
            ghost.reset();
        }

        score = 0;
        gameOver = false;
        gameWon = false;
        frightenedTimer = 0;
    }

    public void update() {
        if (gameOver || gameWon) {
            return;
        }

        if (frightenedTimer > 0) {
            frightenedTimer--;
            if (frightenedTimer == 0) {
                for (Ghost ghost : ghosts) {
                    ghost.setFrightened(false);
                }
            }
        }

        player.update(maze);

        int tileX = player.getX() / Maze.TILE_SIZE;
        int tileY = player.getY() / Maze.TILE_SIZE;
        int foodType = maze.eatFood(tileX, tileY);

        if (foodType == 1) {
            score += 10;
        } else if (foodType == 2) {
            score += 50;
            frightenedTimer = FRIGHTENED_DURATION;
            for (Ghost ghost : ghosts) {
                ghost.setFrightened(true);
            }
        }

        for (Ghost ghost : ghosts) {
            ghost.update(maze, player);

            if (ghost.collidesWith(player)) {
                if (ghost.isFrightened()) {
                    score += 200;
                    ghost.respawn();
                } else {
                    gameOver = true;
                }
            }
        }

        if (maze.getFoodCount() == 0) {
            gameWon = true;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        maze.draw(g2d);
        player.draw(g2d);

        for (Ghost ghost : ghosts) {
            ghost.draw(g2d);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, maze.getHeight() * Maze.TILE_SIZE + 25);

        if (gameOver) {
            drawGameEndMessage(g2d, "GAME OVER", Color.RED);
        } else if (gameWon) {
            drawGameEndMessage(g2d, "YOU WIN!", Color.GREEN);
        }
    }

    private void drawGameEndMessage(Graphics2D g2d, String mainMsg, Color color) {
        int centerX = maze.getWidth() * Maze.TILE_SIZE / 2;
        int centerY = maze.getHeight() * Maze.TILE_SIZE / 2;

        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        int msgWidth = g2d.getFontMetrics().stringWidth(mainMsg);
        g2d.drawString(mainMsg, centerX - msgWidth / 2, centerY);

        String restartMsg = "Press R to restart";
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        int restartWidth = g2d.getFontMetrics().stringWidth(restartMsg);
        g2d.drawString(restartMsg, centerX - restartWidth / 2, centerY + 40);
    }
}
