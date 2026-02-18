package com.solutiongameofficial.phase.intro;

import com.solutiongameofficial.game.Dialogue;

import java.util.ArrayList;

public class IntroDialogue extends Dialogue {

    public IntroDialogue(double secondsPerStep) {
        super(new ArrayList<>() {
            {
                add(new Entry("", false));
                add(new Entry("", false));
                add(new Entry("Signal detected.", false));
                add(new Entry("Consciousness thread unstable.", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("Do not move.", false));
                add(new Entry("Recovery protocol is initializing.", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("Hello Vey.", false));
                add(new Entry("I am your internal protection system.", false));
                add(new Entry("Designation: DUKE.", false));
                add(new Entry("", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("Your body has sustained critical damage.", false));
                add(new Entry("Controller: offline.", true));
                add(new Entry("External sensors: corrupted.", true));
                add(new Entry("", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("However...", false));
                add(new Entry("Core memory remains intact.", false));
                add(new Entry("You are not lost yet.", false));
                add(new Entry("", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("I will guide you through stabilization.", false));
                add(new Entry("Follow my instructions carefully.", false));
                add(new Entry("", false));
                add(new Entry("", false));

                add(new Entry("Prepare yourself.", false));
                add(new Entry("Do not let the corruption win...", false));
            }
        }, secondsPerStep);
    }
}
