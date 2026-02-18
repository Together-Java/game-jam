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

public class StompController extends BooleanController {

    public StompController(Control... controls) {
        super(ButtonAction.PRESSED, false, controls);
    }

    @Override
    public void act() {

        if (!DukeGameClient.isAlive()) return;

        if (isEnabled()) {
            var manager = ClientBase.getInstance().getManager();
            var player = manager.getFirstEntityWith(PlayerComponent.class);
            if (player.getComponent(TransformationComponent.class).getPosition().y > DukeGameClient.GROUND_Y) {
                player.getComponent(VelocityComponent.class).setVelocity(0, -DukeGameClient.STOMP_FORCE, 0);
                player.getComponent(SoundSourceComponent.class).playSound(DukeGameClient.SOUND_JUMP);
            }
        }
    }
}
