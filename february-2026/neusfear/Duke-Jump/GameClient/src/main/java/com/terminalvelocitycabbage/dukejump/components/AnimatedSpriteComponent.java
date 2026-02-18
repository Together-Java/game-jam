package com.terminalvelocitycabbage.dukejump.components;

import com.terminalvelocitycabbage.engine.ecs.Component;
import com.terminalvelocitycabbage.engine.registry.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AnimatedSpriteComponent implements Component {

    int runtime;
    Map<String, AnimationState> animationStateModels = new HashMap<>();

    @Override
    public void setDefaults() {

    }

    public AnimatedSpriteComponent addStateAndStages(String state, float speed, Identifier... models) {
        animationStateModels.put(state, new AnimationState(speed, models));
        return this;
    }

    public Identifier updateAnimation(String state, float deltaTime) {
        runtime += (int) deltaTime;
        var modelsForState = animationStateModels.get(state);
        int modelIndex = (int) ((runtime * modelsForState.speed())) % modelsForState.numStages();
        return modelsForState.models[modelIndex];
    }

    record AnimationState(float speed, int numStages, Identifier... models) {
        AnimationState(float speed, Identifier... models) {
            this(speed, models.length, models);
        }
    }
}
