package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.ConfettiComponent;
import com.terminalvelocitycabbage.engine.debug.Log;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import com.terminalvelocitycabbage.templates.ecs.components.VelocityComponent;

public class UpdateConfettiSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {
        manager.getEntitiesWith(ConfettiComponent.class).forEach(entity -> {
            var velocityComponent = entity.getComponent(VelocityComponent.class);
            var velocity = velocityComponent.getVelocity();
            var confettiComponent = entity.getComponent(ConfettiComponent.class);
            var transformationComponent = entity.getComponent(TransformationComponent.class);

            velocityComponent.setVelocity(velocity.x, velocity.y - (DukeGameClient.GRAVITY * deltaTime), velocity.z);

            transformationComponent.translate(velocity.x * deltaTime, velocity.y * deltaTime, 0);
            transformationComponent.rotate(0, 0, confettiComponent.getRotationalVelocity() * deltaTime);

            if (transformationComponent.getPosition().y < -300) {
                entity.free();
            }
        });
    }
}
