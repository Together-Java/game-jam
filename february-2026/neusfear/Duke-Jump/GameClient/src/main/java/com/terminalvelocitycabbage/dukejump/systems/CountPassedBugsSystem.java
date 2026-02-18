package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.BugComponent;
import com.terminalvelocitycabbage.dukejump.components.EnemyComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.engine.state.State;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

public class CountPassedBugsSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {

        if (!(boolean) DukeGameClient.isAlive()) return;

        manager.getEntitiesWith(EnemyComponent.class, TransformationComponent.class).forEach(entity -> {
            if (entity.getComponent(TransformationComponent.class).getPosition().x < (DukeGameClient.PLAYER_POSITION_X) - DukeGameClient.INTERSECTION_RADIUS) {
                if (!entity.getComponent(EnemyComponent.class).isPassed()) {
                    State<Integer> state = DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE);
                    state.setValue(state.getValue() + 1);
                }
                entity.getComponent(EnemyComponent.class).pass();
            }
        });
    }
}
