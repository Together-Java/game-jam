package com.terminalvelocitycabbage.dukejump.components;

import com.terminalvelocitycabbage.engine.ecs.Component;

public class EnemyComponent implements Component {

    boolean passed = false;

    @Override
    public void setDefaults() {
        passed = false;
    }

    public boolean isPassed() {
        return passed;
    }

    public void pass() {
        passed = true;
    }
}
