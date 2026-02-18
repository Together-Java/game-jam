package com.solutiongameofficial.game;

import com.solutiongameofficial.phase.Phase;
import lombok.Getter;

public final class PhaseFlow {

    private final Phase[] phases;
    private final DukePhaseController duke;

    @Getter
    private boolean inDukePhase = false;
    private Phase currentDukePhase;
    private int phaseIndex = 0;

    public PhaseFlow(Phase[] phases, DukePhaseController duke) {
        this.phases = phases;
        this.duke = duke;
        this.currentDukePhase = duke.initialPhase();
    }

    public Phase currentPhase() {
        if (inDukePhase) {
            return currentDukePhase;
        }
        return phases[phaseIndex];
    }

    public void tick(double fixedDeltaSeconds) {
        if (!inDukePhase && !GameLoopRunner.isIntroOrLoadingPhase(phases[phaseIndex])) {
            if (duke.tickCountdown(fixedDeltaSeconds)) {
                startDukePhase();
            }
        }
    }

    public boolean advance() {
        if (inDukePhase) {
            currentDukePhase = duke.next(currentDukePhase);
            if (currentDukePhase == null) {
                inDukePhase = false;
            }
            return false;
        }

        phaseIndex++;
        return phaseIndex >= phases.length;
    }

    private void startDukePhase() {
        inDukePhase = true;
        currentDukePhase = duke.initialPhase();
        duke.resetCountdown();
    }
}