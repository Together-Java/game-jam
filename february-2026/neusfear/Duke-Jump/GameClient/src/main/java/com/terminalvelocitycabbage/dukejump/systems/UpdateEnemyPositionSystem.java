package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.BugComponent;
import com.terminalvelocitycabbage.dukejump.components.EnemyComponent;
import com.terminalvelocitycabbage.dukejump.components.FlyComponent;
import com.terminalvelocitycabbage.dukejump.components.SquashedComponent;
import com.terminalvelocitycabbage.engine.client.ClientBase;
import com.terminalvelocitycabbage.engine.debug.Log;
import com.terminalvelocitycabbage.engine.ecs.Entity;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

public class UpdateEnemyPositionSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {

        if (DukeGameClient.isPaused()) return;
        boolean alive = DukeGameClient.isAlive();

        manager.getEntitiesWith(EnemyComponent.class).forEach(entity -> {
            var transformation = entity.getComponent(TransformationComponent.class);
            if (!entity.hasComponent(SquashedComponent.class)) {
                if (entity.hasComponent(BugComponent.class)) {
                    transformation.translate(deltaTime * DukeGameClient.MOVEMENT_SPEED * DukeGameClient.BUG_SPEED_MULTIPLIER * (alive ? 1 : 0.2f), 0, 0);
                }
                if (entity.hasComponent(FlyComponent.class)) {
                    transformation.translate(
                            deltaTime * DukeGameClient.MOVEMENT_SPEED * DukeGameClient.FLY_SPEED_MULTIPLIER * (alive ? 1 : 0.2f),
                            (float) (Math.sin(ClientBase.getInstance().getRuntime() * (DukeGameClient.FLY_HEIGHT_SPEED/10000)) * DukeGameClient.FLY_HEIGHT_VARIANCE),
                            0);
                }
            } else {
                animateDeadEnemy(transformation, deltaTime);
            }
            freeIfOutOfBounds(transformation, manager, entity);
        });
    }

    private void freeIfOutOfBounds(TransformationComponent transformation, Manager manager, Entity entity) {
        if (transformation.getPosition().x < -600 || transformation.getPosition().y < -600) manager.freeEntity(entity);
    }

    private void animateDeadEnemy(TransformationComponent transform, float deltaTime) {
        transform.rotate(0, 0, deltaTime * 0.6f);
    }
}
