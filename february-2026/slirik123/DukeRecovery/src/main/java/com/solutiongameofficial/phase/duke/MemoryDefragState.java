package com.solutiongameofficial.phase.duke;

import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.graphics.MeshBounds;
import lombok.Getter;

import java.util.SplittableRandom;

public final class MemoryDefragState {

    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;
    public static final int ROW_COUNT = 18;

    private final SplittableRandom random = new SplittableRandom();
    private final MemoryRow[] rows = new MemoryRow[ROW_COUNT];

    private int selectedIndex = 0;
    private double timeAliveSeconds = 0.0;

    /**
     * 0...1 where 1 means Duke fully corrupted (all yellow).
     */
    private double modelCorruption = 0.0;

    /**
     * Survival win condition. You can change this to "remove X rows" if you prefer.
     */
    private static final double SURVIVE_SECONDS_TO_WIN = 20.0;

    /**
     * If modelCorruption reaches this, player loses
     */
    private static final double FAIL_CORRUPTION = 1.0;

    @Getter
    private boolean failed = false;

    /**
     * Used by renderer: normalized feet and head levels from the simplified mesh.
     */
    private double meshMinY = -1.0;
    private double meshMaxY = 1.0;

    public void initialize(MeshBounds bounds) {
        this.meshMinY = bounds.minY();
        this.meshMaxY = bounds.maxY();

        for (int index = 0; index < rows.length; index++) {
            rows[index] = new MemoryRow();
        }

        // Seed a few initial corruptions so it is not sleepy at start.
        rows[random.nextInt(rows.length)].corrupt(random);
        rows[random.nextInt(rows.length)].corrupt(random);
    }

    private boolean rightLatch = false;

    public boolean applyInput(GameAction action) {
        boolean changed = false;

        if (action != GameAction.MOVE_RIGHT) {
            rightLatch = false;
        }

        if (action == null) {
            return false;
        }

        switch (action) {
            case MOVE_UP -> {
                int prev = selectedIndex;
                selectedIndex = (selectedIndex - 1 + rows.length) % rows.length;
                changed = prev != selectedIndex;
            }
            case MOVE_DOWN -> {
                int prev = selectedIndex;
                selectedIndex = (selectedIndex + 1) % rows.length;
                changed = prev != selectedIndex;
            }
            case MOVE_RIGHT -> {
                if (rightLatch) {
                    return false;
                }
                rightLatch = true;

                MemoryRow row = rows[selectedIndex];
                if (row.isCorrupted() && !row.isRemoved()) {
                    row.eject();

                    modelCorruption -= 0.08;
                    if (modelCorruption < 0.0) {
                        modelCorruption = 0.0;
                    }

                    changed = true;
                }
            }
            default -> {
            }
        }

        return changed;
    }

    public void update(double deltaTime) {
        if (failed) {
            // Optional: still animate, but keep state stable if you prefer.
            timeAliveSeconds += deltaTime;
            return;
        }

        timeAliveSeconds += deltaTime;

        for (MemoryRow row : rows) {
            row.update(deltaTime, random);
        }

        // Rows can corrupt anytime. Increase chance with time pressure.
        // This is tuned for "always something to do" without being instantly impossible.
        double corruptionPressure = 0.25 + modelCorruption * 0.9;
        double perRowChance = (0.12 + 0.25 * corruptionPressure) * deltaTime;

        for (MemoryRow row : rows) {
            if (!row.isRemoved() && !row.isCorrupted()) {
                if (random.nextDouble() < perRowChance) {
                    row.corrupt(random);
                }
            }
        }

        // Model corruption increases based on how many corrupted rows exist.
        int corruptedCount = 0;
        for (MemoryRow row : rows) {
            if (row.isCorrupted() && !row.isRemoved()) {
                corruptedCount++;
            }
        }

        double fracCorrupted = corruptedCount / (double) rows.length;

        // Baseline rising plus "panic multiplier".
        // You can make this harsher or softer by tweaking these constants.
        double increasePerSecond = 0.06 + 0.28 * fracCorrupted + 0.25 * modelCorruption * fracCorrupted;

        modelCorruption += increasePerSecond * deltaTime;

        if (modelCorruption >= FAIL_CORRUPTION) {
            modelCorruption = FAIL_CORRUPTION;
            failed = true;
        }
    }

    public boolean isSolved() {
        // Win if player survives long enough without failing.
        return !failed && timeAliveSeconds >= SURVIVE_SECONDS_TO_WIN;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public MemoryRow[] rows() {
        return rows;
    }

    public double modelCorruption() {
        return modelCorruption;
    }

    public double timeAliveSeconds() {
        return timeAliveSeconds;
    }

    public double meshMinY() {
        return meshMinY;
    }

    public double meshMaxY() {
        return meshMaxY;
    }
}