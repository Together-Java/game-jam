package com.solutiongameofficial.phase.failed;

import com.solutiongameofficial.game.Dialogue;
import com.solutiongameofficial.io.ResourceLoader;
import com.solutiongameofficial.phase.PureDialoguePhase;

import java.awt.image.BufferedImage;

public class FailedPhase extends PureDialoguePhase {
    public FailedPhase() {
        super(new BufferedImage[]{
                ResourceLoader.loadImage("ascii/Duke_0002.png"),
        });
    }

    @Override
    protected Dialogue dialogue() {
        return new FailedPhaseDialogue(1.75);
    }
}
