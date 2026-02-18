package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.GroundComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

public class UpdateGroundPositionsSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {

        if (DukeGameClient.isPaused()) return;
        if (!(boolean) DukeGameClient.isAlive()) return;

        manager.getEntitiesWith(GroundComponent.class, TransformationComponent.class).forEach(entity -> {
            var transformation = entity.getComponent(TransformationComponent.class);
            transformation.translate(deltaTime * DukeGameClient.MOVEMENT_SPEED, 0, 0);
            if (transformation.getPosition().x < (-DukeGameClient.SCALE - 600))
                transformation.translate(DukeGameClient.SCALE * 4 * DukeGameClient.GROUND_PARTS, 0, 0);
        });
    }
}
