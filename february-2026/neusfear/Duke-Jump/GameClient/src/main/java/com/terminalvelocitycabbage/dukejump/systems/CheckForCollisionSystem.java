package com.terminalvelocitycabbage.dukejump.systems;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.*;
import com.terminalvelocitycabbage.engine.client.ClientBase;
import com.terminalvelocitycabbage.engine.debug.Log;
import com.terminalvelocitycabbage.engine.ecs.Entity;
import com.terminalvelocitycabbage.engine.ecs.Manager;
import com.terminalvelocitycabbage.engine.ecs.System;
import com.terminalvelocitycabbage.engine.util.touples.Pair;
import com.terminalvelocitycabbage.templates.ecs.components.SoundSourceComponent;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import com.terminalvelocitycabbage.templates.ecs.components.VelocityComponent;
import org.joml.Vector2f;

public class CheckForCollisionSystem extends System {

    @Override
    public void update(Manager manager, float deltaTime) {

        if (!(boolean) DukeGameClient.isAlive()) return;

        var player = manager.getFirstEntityWith(PlayerComponent.class);
        var transformation = player.getComponent(TransformationComponent.class);
        var playerX = transformation.getPosition().x;
        var playerY = transformation.getPosition().y;

        for (Entity enemy : manager.getEntitiesWith(EnemyComponent.class)) {
            if (enemy.hasComponent(SquashedComponent.class)) continue;
            var entityTransformation = enemy.getComponent(TransformationComponent.class);
            var bugX = entityTransformation.getPosition().x;
            var bugY = entityTransformation.getPosition().y;
            if (intersects(playerX, playerY, bugX, bugY)) {
                if (!enemy.hasComponent(SquashedComponent.class) && playerY - bugY > DukeGameClient.SQUASH_OFFSET) {
                    enemy.addComponent(SquashedComponent.class);
                    enemy.addComponent(VelocityComponent.class);
                    var points = (enemy.hasComponent(FlyComponent.class)) ? 50 : 10;
                    ClientBase.getInstance().getStateHandler()
                            .updateState(DukeGameClient.CURRENT_SCORE,
                                    ((int) ClientBase.getInstance().getStateHandler().getState(DukeGameClient.CURRENT_SCORE).getValue()) + points);
                    enemy.getComponent(SoundSourceComponent.class).playSound(DukeGameClient.SOUND_SQUASH);
                    player.getComponent(VelocityComponent.class).addVelocity(0, DukeGameClient.SQUASH_UPFORCE, 0);
                } else {
                    DukeGameClient.getInstance().getStateHandler().getState(DukeGameClient.GAME_STATE).setValue(DukeGameClient.GameState.DEAD);
                    player.getComponent(SoundSourceComponent.class).playSound(DukeGameClient.SOUND_DEATH);
                }
            }
        }
    }

    private boolean intersects(float playerX, float playerY, float bugX, float bugY) {
        return Vector2f.distanceSquared(playerX, playerY, bugX, bugY) <= (DukeGameClient.INTERSECTION_RADIUS * DukeGameClient.INTERSECTION_RADIUS);
    }
}
