package com.solutiongameofficial.phase.duke;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.graphics.parser.ImageToAsciiParser;
import com.solutiongameofficial.phase.Phase;
import com.solutiongameofficial.phase.checksum.AsciiPresenter;
import lombok.Getter;

import java.awt.image.BufferedImage;

public final class DukePhase implements Phase {

    private final MemoryDefragState state = new MemoryDefragState();
    private final MemoryDefragRenderer renderer = new MemoryDefragRenderer();
    private final AsciiPresenter presenter = new AsciiPresenter(new ImageToAsciiParser());

    @Getter
    private boolean failed;

    @Override
    public boolean update(GameAction action, double deltaTime) {
        boolean changed = state.applyInput(action);
        state.update(deltaTime);

        presenter.update(deltaTime, changed);
        if (changed) {
            presenter.markDirty();
        }

        if (state.isFailed()) {
            failed = true;
        }

        return state.isSolved() || state.isFailed();
    }

    @Override
    public BufferedImage content() {
        BufferedImage normal = renderer.render(state);
        return presenter.get(normal);
    }

    {
        renderer.initialize();
        state.initialize(renderer.getMeshBounds());
        presenter.initialize();
        presenter.markDirty();
    }
}