package com.solutiongameofficial.phase.transition;

import com.solutiongameofficial.game.Dialogue;

import java.util.ArrayList;
import java.util.List;

public class DukeOutroPhaseDialogue extends Dialogue {
    public DukeOutroPhaseDialogue(double secondsPerStep) {
        super(new ArrayList<>() {
            {
                add(new Entry("Corruption level decreasing.", false));
                add(new Entry("Core integrity stabilizing.", false));
                add(new Entry("", false));
                add(new Entry("Defensive cycle complete.", false));
                add(new Entry("", false));

                add(new Entry("Your intervention was sufficient.", false));
                add(new Entry("I am operational.", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("This was only the first breach.", false));
                add(new Entry("Prepare for further anomalies.", false));
                add(new Entry("", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("Proceeding to next sequence.", false));
            }
        }, secondsPerStep);
    }
}
