package com.terminalvelocitycabbage.dukejump.inputcontrollers;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.dukejump.components.PlayerComponent;
import com.terminalvelocitycabbage.engine.client.ClientBase;
import com.terminalvelocitycabbage.engine.client.input.control.Control;
import com.terminalvelocitycabbage.engine.client.input.controller.BooleanController;
import com.terminalvelocitycabbage.engine.client.input.types.ButtonAction;
import com.terminalvelocitycabbage.templates.ecs.components.SoundSourceComponent;
import com.terminalvelocitycabbage.templates.ecs.components.TransformationComponent;
import com.terminalvelocitycabbage.templates.ecs.components.VelocityComponent;

public class JumpController extends BooleanController {

    private float jumpStamina;

    public JumpController(Control... controls) {
        super(ButtonAction.PRESSED, false, controls);
    }

    @Override
    public void act() {

        if (!DukeGameClient.isAlive()) return;

        var manager = ClientBase.getInstance().getManager();
        var player = manager.getFirstEntityWith(PlayerComponent.class);
        var transformation = player.getComponent(TransformationComponent.class);
        var velocity = player.getComponent(VelocityComponent.class);

        if (DukeGameClient.USE_ADAPTIVE_JUMP) {
            if (transformation.getPosition().y <= DukeGameClient.GROUND_Y){
                jumpStamina = DukeGameClient.JUMP_FORCE;
            }

            if (isEnabled()) {
                if (transformation.getPosition().y <= DukeGameClient.GROUND_Y) {
                    player.getComponent(SoundSourceComponent.class).playSound(DukeGameClient.SOUND_JUMP);
                    velocity.setVelocity(0, DukeGameClient.JUMP_FORCE, 0);
                } else {
                    if (velocity.getVelocity().y > 0) {
                        velocity.addVelocity(0, jumpStamina, 0);
                        jumpStamina *= DukeGameClient.JUMP_HOLD_FRICTION;
                    }
                }
            }
        } else {
            if (isEnabled()) {
                if (transformation.getPosition().y <= DukeGameClient.GROUND_Y) {
                    player.getComponent(SoundSourceComponent.class).playSound(DukeGameClient.SOUND_JUMP);
                    velocity.setVelocity(0, DukeGameClient.JUMP_FORCE, 0);
                }
            }
        }
    }
}
