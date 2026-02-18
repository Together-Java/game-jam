package com.example.dukeman;

import com.example.dukeman.entity.Entity;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener {
    private final Entity entity;
    private final GamePanel panel;

    public KeyboardListener(Entity entity, GamePanel panel) {
        this.entity = entity;
        this.panel = panel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> entity.setDirection(Direction.UP);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> entity.setDirection(Direction.DOWN);
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> entity.setDirection(Direction.LEFT);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> entity.setDirection(Direction.RIGHT);
            case KeyEvent.VK_R -> {
                if (panel.isGameEnded()) {
                    panel.restart();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}
