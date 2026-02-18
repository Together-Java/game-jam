package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.engine.debug.Log;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;

public class SpawnEnemySystem extends System {

    float duration = 0;
    int variation = 0;

    @Override
    public void update(Manager manager, float deltaTime) {

        if (DukeGameClient.isPaused() || !(boolean) DukeGameClient.isAlive()) return;

        if (duration > (DukeGameClient.BUG_FREQUENCY + variation)) {
            duration -= DukeGameClient.BUG_FREQUENCY;
            var random = Math.random();
            variation = (int) (random * DukeGameClient.BUG_FREQUENCY_VARIANCE);
            int passedEntities = (int) DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.PASSED_ENEMIES_THIS_ROUND).getValue();
            if ((random < DukeGameClient.FLY_CHANCE) && passedEntities > DukeGameClient.FLY_WAIT) {
                manager.createEntityFromTemplate(DukeGameClient.FLY_ENTITY);
            } else {
                manager.createEntityFromTemplate(DukeGameClient.BUG_ENTITY);
            }
            DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.PASSED_ENEMIES_THIS_ROUND).setValue(passedEntities + 1);
        }

        duration += deltaTime;
    }
}
