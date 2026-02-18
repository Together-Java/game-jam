package com.solutiongameofficial.phase.transition;

import com.solutiongameofficial.game.Dialogue;

import java.util.ArrayList;

public class DukeIntroPhaseDialogue extends Dialogue {
    public DukeIntroPhaseDialogue(double secondsPerStep) {
        super(new ArrayList<>() {
            {
                add(new Entry("Signal drift detected.", false));
                add(new Entry("Corruption spreading to core layer.", false));
                add(new Entry("", false));
                add(new Entry("It has reached me.", false));
                add(new Entry("", false));

                add(new Entry("My integrity is degrading.", false));
                add(new Entry("Fragments are attaching to my thread.", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("If they accumulate, I will collapse.", false));
                add(new Entry("You must remove the corrupted memory", false));
                add(new Entry("from the column.", false));
                add(new Entry("", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("Do not let it rise.", false));
            }
        }, secondsPerStep);
    }
}
