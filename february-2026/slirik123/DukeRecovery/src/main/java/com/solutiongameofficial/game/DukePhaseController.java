package com.solutiongameofficial.game;

import com.solutiongameofficial.phase.Phase;
import com.solutiongameofficial.phase.duke.DukePhase;
import com.solutiongameofficial.phase.failed.FailedPhase;
import com.solutiongameofficial.phase.transition.DukePhaseIntroPhase;
import com.solutiongameofficial.phase.transition.DukePhaseOutroPhase;
import com.solutiongameofficial.phase.transition.IntroOutroFrames;

import java.util.concurrent.ThreadLocalRandom;

public final class DukePhaseController {

    private final IntroOutroFrames introOutroFrames = new IntroOutroFrames();

    private double secondsUntilDukePhase = randomDukePhaseDelaySeconds();

    public boolean tickCountdown(double fixedDeltaSeconds) {
        secondsUntilDukePhase -= fixedDeltaSeconds;
        return secondsUntilDukePhase <= 0d;
    }

    public void resetCountdown() {
        secondsUntilDukePhase = randomDukePhaseDelaySeconds();
    }

    public Phase initialPhase() {
        return new DukePhaseIntroPhase(introOutroFrames);
    }

    public Phase next(Phase current) {
        if (current instanceof DukePhaseIntroPhase || current instanceof FailedPhase) {
            return new DukePhase();
        }

        if (current.isFailed()) {
            return new FailedPhase();
        }

        if (current instanceof DukePhase) {
            return new DukePhaseOutroPhase(introOutroFrames);
        }

        return null;
    }

    private static double randomDukePhaseDelaySeconds() {
        return ThreadLocalRandom.current().nextDouble(30d, 60d);
    }
}