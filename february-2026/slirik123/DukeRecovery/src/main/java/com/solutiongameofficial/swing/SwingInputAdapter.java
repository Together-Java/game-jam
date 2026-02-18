package com.solutiongameofficial.swing;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.io.InputFacade;

import javax.swing.*;
import java.awt.event.ActionEvent;

public record SwingInputAdapter(InputFacade input) {

    public void bind(JComponent component) {
        int scope = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = component.getInputMap(scope);
        ActionMap actionMap = component.getActionMap();

        bind(inputMap, actionMap, "pressed W", "upPress", GameAction.MOVE_UP);
        bind(inputMap, actionMap, "pressed UP", "upPress", GameAction.MOVE_UP);

        bind(inputMap, actionMap, "pressed A", "leftPress", GameAction.MOVE_LEFT);
        bind(inputMap, actionMap, "pressed LEFT", "leftPress", GameAction.MOVE_LEFT);

        bind(inputMap, actionMap, "pressed S", "downPress", GameAction.MOVE_DOWN);
        bind(inputMap, actionMap, "pressed DOWN", "downPress", GameAction.MOVE_DOWN);

        bind(inputMap, actionMap, "pressed D", "rightPress", GameAction.MOVE_RIGHT);
        bind(inputMap, actionMap, "pressed RIGHT", "rightPress", GameAction.MOVE_RIGHT);

        bind(inputMap, actionMap, "pressed Q", "quit", GameAction.QUIT);
    }

    private void bind(InputMap inputMap, ActionMap actionMap, String keyStroke, String id, GameAction action) {
        inputMap.put(KeyStroke.getKeyStroke(keyStroke), id);
        actionMap.put(id, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                input.publish(action);
            }
        });
    }
}
