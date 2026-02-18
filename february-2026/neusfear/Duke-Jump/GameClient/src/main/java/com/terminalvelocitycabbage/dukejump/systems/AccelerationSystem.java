package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.ConfettiComponent;
import com.terminalvelocitycabbage.dukejump.components.PlayerComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import com.terminalvelocitycabbage.templates.ecs.components.VelocityComponent;

public class AccelerationSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {
        manager.getEntitiesWith(VelocityComponent.class, TransformationComponent.class).forEach(entity -> {
            if (DukeGameClient.isPaused()) return;
            if (entity.hasComponent(ConfettiComponent.class)) return;
            var velocity = entity.getComponent(VelocityComponent.class).getVelocity();
            var transformationComponent = entity.getComponent(TransformationComponent.class);
            transformationComponent.translate(velocity.x * deltaTime, velocity.y * deltaTime, velocity.z * deltaTime);
            if (entity.hasComponent(PlayerComponent.class) && transformationComponent.getPosition().y < DukeGameClient.GROUND_Y) {
                transformationComponent.setPosition(transformationComponent.getPosition().x, DukeGameClient.GROUND_Y, transformationComponent.getPosition().z);
            }
        });
    }
}
