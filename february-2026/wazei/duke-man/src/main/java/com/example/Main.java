package com.example;

import com.example.dukeman.GamePanel;
import com.example.dukeman.KeyboardListener;
import com.example.dukeman.Maze;
import com.example.dukeman.entity.Duke;
import com.example.dukeman.entity.Entity;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final int UPDATE_DELAY = 150;

    public static void main(String[] args) {
        Maze maze = new Maze();
        Entity player = new Duke(1, 1);
        GamePanel panel = new GamePanel(player, maze);

        int width = maze.getWidth() * Maze.TILE_SIZE + 16;
        int height = maze.getHeight() * Maze.TILE_SIZE + 80;

        JFrame frame = new JFrame("DukeMan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(width, height));
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.add(panel);
        frame.addKeyListener(new KeyboardListener(player, panel));
        frame.setVisible(true);

        new Thread(() -> {
            while (true) {
                panel.update();
                panel.repaint();
                try {
                    Thread.sleep(UPDATE_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
