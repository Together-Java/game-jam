package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.BackgroundComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

public class UpdateBackgroundPositionsSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {

        if (DukeGameClient.isPaused()) return;
        boolean alive = DukeGameClient.isAlive();

        manager.getEntitiesWith(BackgroundComponent.class, TransformationComponent.class).forEach(entity -> {
            var transformation = entity.getComponent(TransformationComponent.class);
            transformation.translate(deltaTime * DukeGameClient.MOVEMENT_SPEED * DukeGameClient.BACKGROUND_SPEED_MULTIPLIER * (alive ? 1 : 0.1f), 0, 0);
            if (transformation.getPosition().x < (-DukeGameClient.SCALE - 600))
                transformation.translate(DukeGameClient.SCALE * 8 * DukeGameClient.BACKGROUND_PARTS, 0, 0);
        });
    }
}
