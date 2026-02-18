package com.solutiongameofficial.phase.checksum;

import com.solutiongameofficial.game.GameAction;
import lombok.Getter;

public final class ChecksumState {

    private final char[][] symbolSequence = {
            {'J', 'k', 'B', 'z', 'Q', 'V', 'u', 'f', 'F'},
            {'<', '}', '#', '-', '!', '@', '[', '?', '/'},
            {'1', '0', '3', '4', '5', '6', '5', '8', '9'},
            {'9', '&', '5', '2', '+', '_', '~', '6', 'q'},
            {'|', 'A', '%', '0', '>', '5', 'F', 'C', 't'},
            {'9', ')', 'B', 'h', '*', 'U', '+', 'x', '='}
    };

    @Getter
    private final String expectedSequence = "V-4559";

    private final int[] sequenceVelocity = {3, -3, -3, 3, -3, 3};
    private final double[] sequenceState = {0, 0, 0, 0, 0, 0};

    @Getter
    private int selectedIndex = 0;

    public boolean applyInput(GameAction action) {
        if (action == null) {
            return false;
        }

        return switch (action) {
            case MOVE_LEFT -> {
                moveIndex(-1);
                yield true;
            }
            case MOVE_RIGHT -> {
                moveIndex(1);
                yield true;
            }
            case MOVE_UP -> {
                adaptVelocity(1);
                yield true;
            }
            case MOVE_DOWN -> {
                adaptVelocity(-1);
                yield true;
            }
            default -> false;
        };
    }

    public void update(double deltaTime) {
        for (int index = 0; index < sequenceState.length; index++) {
            sequenceState[index] += sequenceVelocity[index] * deltaTime;
        }
    }

    public boolean isSolved() {
        for (int columnIndex = 0; columnIndex < symbolSequence.length; columnIndex++) {
            char[] columnSymbols = symbolSequence[columnIndex];
            int symbolCount = columnSymbols.length;

            double position = sequenceState[columnIndex];
            double floored = Math.floor(position);

            int baseIndex = floorMod((int) floored, symbolCount);
            double fraction = position - floored;
            int nextIndex = (baseIndex + 1) % symbolCount;

            int chosenIndex = (fraction < 0.5) ? baseIndex : nextIndex;
            if (columnSymbols[chosenIndex] != expectedSequence.charAt(columnIndex)) {
                return false;
            }
        }

        return true;
    }

    private static int floorMod(int value, int mod) {
        int result = value % mod;
        return result < 0 ? result + mod : result;
    }

    private void moveIndex(int sign) {
        selectedIndex = floorMod(selectedIndex + sign, symbolSequence.length);
    }

    private void adaptVelocity(int sign) {
        int newVelocity = sequenceVelocity[selectedIndex] + sign;
        if (newVelocity > 10) newVelocity = 10;
        if (newVelocity < -10) newVelocity = -10;
        sequenceVelocity[selectedIndex] = newVelocity;
    }

    public int getColumnCount() {
        return symbolSequence.length;
    }

    public double getPosition(int columnIndex) {
        return sequenceState[columnIndex];
    }

    public char[] getSymbols(int columnIndex) {
        return symbolSequence[columnIndex];
    }
}