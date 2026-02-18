package com.solutiongameofficial.phase.loading;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.phase.Phase;

import java.awt.image.BufferedImage;

public final class LoadingPhase implements Phase {

    private final LoadingState loadingState = new LoadingState(5.0);
    private final LoadingRenderer loadingRenderer = new LoadingRenderer();

    private BufferedImage lastFrame;

    @Override
    public boolean update(GameAction action, double deltaTime) {
        loadingState.update(deltaTime);
        lastFrame = loadingRenderer.render(loadingState);
        return loadingState.isFinished();
    }

    @Override
    public BufferedImage content() {
        if (lastFrame == null) {
            lastFrame = loadingRenderer.render(loadingState);
        }
        return lastFrame;
    }
}