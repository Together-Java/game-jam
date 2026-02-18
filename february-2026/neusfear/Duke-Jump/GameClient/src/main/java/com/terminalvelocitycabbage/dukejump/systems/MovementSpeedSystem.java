package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;

public class MovementSpeedSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {
        DukeGameClient.MOVEMENT_SPEED -= DukeGameClient.SPEEDUP_MULTIPLIER * deltaTime;
    }
}
