package com.solutiongameofficial.phase.maze;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.graphics.parser.ImageToAsciiParser;
import com.solutiongameofficial.phase.Phase;

import java.awt.image.BufferedImage;
import java.util.Random;

public class MazePhase implements Phase {

    private static final int GRID_WIDTH = 9;
    private static final int GRID_HEIGHT = 9;
    private static final int SOLVES_REQUIRED = 3;

    private static final double DOUBLE_TAP_SECONDS = 0.25;

    private static final double SNAKE_SPAWN_SECONDS = 1.4;
    private static final double SNAKE_STEP_SECONDS = 0.06;

    private final Random random = new Random(1337);

    private final MazeState mazeState = new MazeState(GRID_WIDTH, GRID_HEIGHT, random);
    private final MazeInputInterpreter inputInterpreter = new MazeInputInterpreter(DOUBLE_TAP_SECONDS);
    private final CorruptedSnake corruptedSnake = new CorruptedSnake(SNAKE_SPAWN_SECONDS, SNAKE_STEP_SECONDS);
    private final MazeRenderer renderer = new MazeRenderer(new ImageToAsciiParser());

    private boolean solvedFlashActive = false;
    private double solvedFlashSeconds = 0.0;

    public MazePhase() {
        renderer.markCorruptedLayerDirty();
    }

    @Override
    public boolean update(GameAction action, double deltaTime) {
        updateSolvedFlash(deltaTime);

        MazeInputInterpreter.MazeCommand command = inputInterpreter.update(action, deltaTime);
        boolean stateChanged = applyCommand(command);

        boolean snakeMutated = corruptedSnake.update(deltaTime, random, mazeState);
        if (snakeMutated) {
            stateChanged = true;
        }

        if (!solvedFlashActive && mazeState.pathExists()) {
            boolean completed = mazeState.advanceObjectiveIfSolved(SOLVES_REQUIRED, random);
            solvedFlashActive = true;
            solvedFlashSeconds = 0.0;
            stateChanged = true;

            if (completed) {
                return true;
            }
        }

        if (stateChanged) {
            renderer.markCorruptedLayerDirty();
        }

        return false;
    }

    @Override
    public BufferedImage content() {
        CorruptedSnake.Overlay overlay = corruptedSnake.getOverlay(GRID_WIDTH, GRID_HEIGHT);

        return renderer.render(mazeState, solvedFlashActive, solvedFlashSeconds, SOLVES_REQUIRED, overlay);
    }

    private boolean applyCommand(MazeInputInterpreter.MazeCommand command) {
        if (command instanceof MazeInputInterpreter.MoveCommand(int dx, int dy)) {
            mazeState.moveCursor(dx, dy);
            return true;
        }

        if (command instanceof MazeInputInterpreter.RotateCommand(boolean clockwise)) {
            if (clockwise) {
                mazeState.rotateSelectedClockwise();
            } else {
                mazeState.rotateSelectedCounterClockwise();
            }
            return true;
        }

        return false;
    }

    private void updateSolvedFlash(double deltaTime) {
        if (!solvedFlashActive) {
            return;
        }

        solvedFlashSeconds += deltaTime;

        if (solvedFlashSeconds >= 0.6) {
            solvedFlashActive = false;
            solvedFlashSeconds = 0.0;
        }
    }
}