package com.solutiongameofficial.phase.transition;

import com.solutiongameofficial.game.Dialogue;
import com.solutiongameofficial.phase.PureDialoguePhase;

import java.awt.image.BufferedImage;

public class DukePhaseIntroPhase extends PureDialoguePhase {

    public DukePhaseIntroPhase(IntroOutroFrames frames) {
        super(frames.IMAGES.toArray(BufferedImage[]::new),0.5, 600, 400, 1120, 750);
    }

    @Override
    protected Dialogue dialogue() {
        return new DukeIntroPhaseDialogue(1.75);
    }
}
