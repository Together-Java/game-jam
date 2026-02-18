package com.solutiongameofficial.phase.intro;

import com.solutiongameofficial.game.Dialogue;
import com.solutiongameofficial.io.ResourceLoader;
import com.solutiongameofficial.phase.PureDialoguePhase;

import java.awt.image.BufferedImage;

public final class IntroPhase extends PureDialoguePhase {

    public IntroPhase() {
        super(new BufferedImage[] {
                ResourceLoader.loadImage("ascii/Duke_0001.png"),
                ResourceLoader.loadImage("ascii/Duke_0001.png"),
                ResourceLoader.loadImage("ascii/Duke_0001.png"),
                ResourceLoader.loadImage("ascii/Duke_0002.png"),
                ResourceLoader.loadImage("ascii/Duke_0003.png"),
                ResourceLoader.loadImage("ascii/Duke_0004.png"),
                ResourceLoader.loadImage("ascii/Duke_0005.png"),
                ResourceLoader.loadImage("ascii/Duke_0006.png"),
        });
    }

    @Override
    protected Dialogue dialogue() {
        return new IntroDialogue(1.75);
    }
}