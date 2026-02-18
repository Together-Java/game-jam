package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.ConfettiComponent;
import com.terminalvelocitycabbage.dukejump.components.SquashedComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import com.terminalvelocitycabbage.templates.ecs.components.VelocityComponent;

public class GravitySystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {
        manager.getEntitiesWith(VelocityComponent.class).forEach(entity -> {
            if (DukeGameClient.isPaused()) return;
            if (entity.hasComponent(ConfettiComponent.class)) return;
            if (!entity.hasComponent(SquashedComponent.class) && entity.getComponent(TransformationComponent.class).getPosition().y < DukeGameClient.GROUND_Y) {
                entity.getComponent(VelocityComponent.class).setVelocity(0, 0, 0);
                entity.getComponent(TransformationComponent.class).setPosition(DukeGameClient.PLAYER_POSITION_X, DukeGameClient.GROUND_Y, 0);
            } else {
                entity.getComponent(VelocityComponent.class).addVelocity(0, -DukeGameClient.GRAVITY * deltaTime, 0);
            }
        });
    }
}
