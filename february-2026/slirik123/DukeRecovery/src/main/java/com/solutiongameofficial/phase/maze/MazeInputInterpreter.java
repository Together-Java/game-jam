package com.solutiongameofficial.phase.maze;

import com.solutiongameofficial.game.GameAction;

public final class MazeInputInterpreter {

    private final double doubleTapSeconds;

    private GameAction lastMoveAction = null;
    private double lastMoveActionSeconds = 0.0;

    public MazeInputInterpreter(double doubleTapSeconds) {
        this.doubleTapSeconds = doubleTapSeconds;
    }

    public MazeCommand update(GameAction action, double deltaTime) {
        lastMoveActionSeconds += deltaTime;

        if (action == null) {
            return MazeCommand.none();
        }

        boolean isDirectional =
                action == GameAction.MOVE_LEFT ||
                        action == GameAction.MOVE_RIGHT ||
                        action == GameAction.MOVE_UP ||
                        action == GameAction.MOVE_DOWN;

        if (!isDirectional) {
            return MazeCommand.none();
        }

        boolean isDoubleTap =
                action == lastMoveAction &&
                        lastMoveActionSeconds <= doubleTapSeconds;

        if (isDoubleTap) {
            lastMoveAction = null;
            lastMoveActionSeconds = 0.0;

            boolean counterClockwise = action == GameAction.MOVE_LEFT || action == GameAction.MOVE_UP;
            return counterClockwise ? MazeCommand.rotateCounterClockwise() : MazeCommand.rotateClockwise();
        }

        lastMoveAction = action;
        lastMoveActionSeconds = 0.0;

        return switch (action) {
            case MOVE_LEFT -> MazeCommand.move(-1, 0);
            case MOVE_RIGHT -> MazeCommand.move(1, 0);
            case MOVE_UP -> MazeCommand.move(0, -1);
            case MOVE_DOWN -> MazeCommand.move(0, 1);
            default -> MazeCommand.none();
        };
    }

    public sealed interface MazeCommand permits MoveCommand, RotateCommand, NoneCommand {
        static MazeCommand move(int dx, int dy) {
            return new MoveCommand(dx, dy);
        }

        static MazeCommand rotateClockwise() {
            return new RotateCommand(true);
        }

        static MazeCommand rotateCounterClockwise() {
            return new RotateCommand(false);
        }

        static MazeCommand none() {
            return new NoneCommand();
        }
    }

    public record MoveCommand(int dx, int dy) implements MazeCommand { }

    public record RotateCommand(boolean clockwise) implements MazeCommand { }

    public record NoneCommand() implements MazeCommand { }
}
