package com.solutiongameofficial.phase.checksum;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.graphics.parser.ImageToAsciiParser;
import com.solutiongameofficial.phase.Phase;

import java.awt.image.BufferedImage;

public final class ChecksumPhase implements Phase {

    private final ChecksumState state = new ChecksumState();
    private final ChecksumRenderer renderer = new ChecksumRenderer();
    private final AsciiPresenter presenter = new AsciiPresenter(new ImageToAsciiParser());

    @Override
    public boolean update(GameAction action, double deltaTime) {
        boolean changed = state.applyInput(action);
        state.update(deltaTime);

        presenter.update(deltaTime, changed);
        if (changed) {
            presenter.markDirty();
        }

        return state.isSolved();
    }

    @Override
    public BufferedImage content() {
        BufferedImage normal = renderer.render(state);
        return presenter.get(normal);
    }

    {
        renderer.initialize();
        presenter.initialize();
        presenter.markDirty();
    }
}