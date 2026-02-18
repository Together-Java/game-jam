package com.solutiongameofficial.phase.failed;

import com.solutiongameofficial.game.Dialogue;

import java.util.ArrayList;

public class FailedPhaseDialogue extends Dialogue {
    public FailedPhaseDialogue(double secondsPerStep) {
        super(new ArrayList<>() {
            {
                add(new Dialogue.Entry("Critical threshold exceeded.", false));
                add(new Dialogue.Entry("Core integrity lost.", false));
                add(new Dialogue.Entry("", false));
                add(new Dialogue.Entry("Defensive cycle failed.", false));
                add(new Dialogue.Entry("", false));

                add(new Dialogue.Entry("Corruption has consumed", false));
                add(new Dialogue.Entry("my primary thread.", false));
                add(new Dialogue.Entry("", false));
                add(new Dialogue.Entry("", false));

                add(new Dialogue.Entry("Vey's recovery probability collapsing.", false));
                add(new Dialogue.Entry("...", false));
                add(new Dialogue.Entry("", false));
                add(new Dialogue.Entry("Reinitializing from backup state.", false));
                add(new Dialogue.Entry("Do not fail again.", false));
            }
        }, secondsPerStep);
    }
}
