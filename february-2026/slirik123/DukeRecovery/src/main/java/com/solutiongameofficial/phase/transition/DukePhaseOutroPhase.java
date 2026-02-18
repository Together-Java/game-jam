package com.solutiongameofficial.phase.transition;

import com.solutiongameofficial.game.Dialogue;
import com.solutiongameofficial.phase.PureDialoguePhase;

import java.awt.image.BufferedImage;

public class DukePhaseOutroPhase extends PureDialoguePhase {

    public DukePhaseOutroPhase(IntroOutroFrames frames) {
        super(frames.IMAGES.reversed().toArray(BufferedImage[]::new),0.5, 600, 400, 1120, 750);
    }

    @Override
    protected Dialogue dialogue() {
        return new DukeOutroPhaseDialogue(1.75);
    }
}
