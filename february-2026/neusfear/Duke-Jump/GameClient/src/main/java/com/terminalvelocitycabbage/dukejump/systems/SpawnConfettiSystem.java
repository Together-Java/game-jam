package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.ConfettiComponent;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import com.terminalvelocitycabbage.templates.ecs.components.VelocityComponent;

public class SpawnConfettiSystem extends System {

    int remainingConfetti = 0;
    long spawnStartTime = 0;
    boolean highScoreReached = false;
    
    @Override
    public void update(Manager manager, float deltaTime) {
        int currentScore = (int) DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue();

        if (currentScore == 0) {
            remainingConfetti = 0;
            spawnStartTime = 0;
            highScoreReached = false;
            return;
        }

        if (!highScoreReached && !DukeGameClient.HIGH_SCORES.isEmpty() && currentScore > DukeGameClient.HIGH_SCORES.getFirst().score()) {
            remainingConfetti = DukeGameClient.CONFETTI_COUNT;
            spawnStartTime = DukeGameClient.getInstance().getRuntime();
            highScoreReached = true;
        }

        if (remainingConfetti > 0) {
            long currentTime = DukeGameClient.getInstance().getRuntime();
            long elapsedTime = currentTime - spawnStartTime;
            float percentDone = Math.min(1.0f, (float) elapsedTime / DukeGameClient.CONFETTI_SPAWN_DURATION);
            int targetRemaining = (int) (DukeGameClient.CONFETTI_COUNT * (1.0f - percentDone));

            while (remainingConfetti > targetRemaining) {
                var entity = manager.createEntityFromTemplate(DukeGameClient.CONFETTI_ENTITY);
                var confettiComponent = entity.getComponent(ConfettiComponent.class);
                var velocityComponent = entity.getComponent(VelocityComponent.class);
                entity.getComponent(TransformationComponent.class).setPosition(DukeGameClient.CONFETTI_SPAWN_LOCATION);
                velocityComponent.setVelocity(confettiComponent.getHorizontalVelocity(), confettiComponent.getVerticalVelocity(), 0);
                remainingConfetti--;
            }
        }
    }
}
