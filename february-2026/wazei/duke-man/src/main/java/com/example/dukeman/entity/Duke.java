package com.example.dukeman.entity;

import com.example.dukeman.Maze;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Duke extends Entity {
    private final BufferedImage image;

    public Duke(int tileX, int tileY) {
        super(tileX * Maze.TILE_SIZE, tileY * Maze.TILE_SIZE);

        try {
            image = ImageIO.read(new File("duke.png"));
        } catch (IOException e) {
           throw new RuntimeException("duke.png not found");
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        int size = Maze.TILE_SIZE - 2;
        g2d.drawImage(image, this.getX() + 1, this.getY() + 1, size, size, null);
    }
}
