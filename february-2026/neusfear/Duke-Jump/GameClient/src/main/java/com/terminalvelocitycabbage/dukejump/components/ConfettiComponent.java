package com.terminalvelocitycabbage.dukejump.components;

import com.terminalvelocitycabbage.dukejump.DukeGameClient;
import com.terminalvelocitycabbage.engine.ecs.Component;
import com.terminalvelocitycabbage.engine.util.Color;
import org.joml.Vector3f;

import java.util.Random;

public class ConfettiComponent implements Component {

    Color color;
    Vector3f initialVelocity;

    @Override
    public void setDefaults() {
        var random = new Random();
        initialVelocity = new Vector3f(
                random.nextFloat(DukeGameClient.CONFETTI_MAX_HORIZONTAL_VELOCITY) * (random.nextBoolean() ? 1f : -1f),
                random.nextFloat(DukeGameClient.CONFETTI_MAX_VERTICAL_VELOCITY),
                random.nextFloat(DukeGameClient.CONFETTI_MAX_ROTATIONAL_VELOCITY) * (random.nextBoolean() ? 1f : -1f)
        );
        color = DukeGameClient.confettiColors[random.nextInt(DukeGameClient.confettiColors.length)];
    }

    public Color getColor() {
        return color;
    }

    public float getVerticalVelocity() {
        return initialVelocity.y;
    }

    public float getHorizontalVelocity() {
        return initialVelocity.x;
    }

    public float getRotationalVelocity() {
        return initialVelocity.z;
    }
}
