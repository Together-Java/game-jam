package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.AnimatedSpriteComponent;
import com.terminalvelocitycabbage.dukejump.components.BugComponent;
import com.terminalvelocitycabbage.dukejump.components.FlyComponent;
import com.terminalvelocitycabbage.dukejump.components.PlayerComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.ModelComponent;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;

public class AnimateSpritesSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {
        manager.getEntitiesWith(AnimatedSpriteComponent.class, ModelComponent.class).forEach(entity -> {

            if (DukeGameClient.isPaused()) return;

            String state = "walk";

            var transformation = entity.getComponent(TransformationComponent.class);
            var gameState = DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.GAME_STATE);

            if (entity.hasComponent(PlayerComponent.class)) {
                if (transformation.getPosition().y > DukeGameClient.GROUND_Y) {
                    state = "jump";
                } else {
                    state = "walk";
                }
                if (gameState.getValue().equals(DukeGameClient.GameState.MAIN_MENU)) {
                    state = "idle";
                }
                if (gameState.getValue().equals(DukeGameClient.GameState.DEAD)) {
                    state = "dead";
                }
            }

            if (entity.hasComponent(BugComponent.class) || entity.hasComponent(FlyComponent.class)) {
                state = "any";
            }

            var modelComponent = entity.getComponent(ModelComponent.class);
            var animatedSpriteComponent = entity.getComponent(AnimatedSpriteComponent.class);
            modelComponent.setModel(animatedSpriteComponent.updateAnimation(state, deltaTime));
        });
    }
}
