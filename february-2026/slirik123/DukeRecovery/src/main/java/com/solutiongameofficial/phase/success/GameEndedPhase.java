package com.solutiongameofficial.phase.success;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.phase.Phase;

import java.awt.image.BufferedImage;

public final class GameEndedPhase implements Phase {

    private final GameEndedState state = new GameEndedState(5.0);
    private final GameEndedRenderer renderer = new GameEndedRenderer();

    private BufferedImage lastFrame;

    @Override
    public boolean update(GameAction action, double deltaTime) {
        state.update(deltaTime);
        lastFrame = renderer.render(state);

        if (state.isFinished()) {
            System.exit(0);
        }
        return false;
    }

    @Override
    public BufferedImage content() {
        if (lastFrame == null) {
            lastFrame = renderer.render(state);
        }
        return lastFrame;
    }
}