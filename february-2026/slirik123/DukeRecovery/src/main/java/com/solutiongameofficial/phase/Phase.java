package com.solutiongameofficial.phase;

import com.solutiongameofficial.game.GameAction;

import java.awt.image.BufferedImage;

public interface Phase {

    /**
     * @return true - if the phase has completed.
     */
    boolean update(GameAction action, double deltaTime);
    BufferedImage content();

    default boolean isFailed() {
        return false;
    }
}
