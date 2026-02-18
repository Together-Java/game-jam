package com.solutiongameofficial.game;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Dialogue {

    private final List<Entry> entries;
    private final double secondsPerStep;

    private int cursor;
    private double accumulatorSeconds;

    public void update(double deltaTime) {
        if (isFinished()) {
            return;
        }

        accumulatorSeconds += Math.max(0.0, deltaTime);
        if (accumulatorSeconds >= secondsPerStep) {
            accumulatorSeconds = 0.0;
            cursor = Math.min(entries.size(), cursor + 1);
        }
    }

    public List<String> getVisibleMainLines(int maxLines) {
        int clamped = Math.max(1, maxLines);
        List<String> result = new ArrayList<>(clamped);

        for (int index = Math.min(cursor, entries.size()) - 1; index >= 0; index--) {
            Entry entry = entries.get(index);
            if (entry.outsideField) {
                continue;
            }

            result.addFirst(entry.text);
            if (result.size() == clamped) {
                break;
            }
        }

        return result;
    }

    public List<String> getVisibleOutsideLines(int maxLines) {
        int clamped = Math.max(1, maxLines);
        List<String> result = new ArrayList<>(clamped);

        for (int index = Math.min(cursor, entries.size()) - 1; index >= 0; index--) {
            Entry entry = entries.get(index);
            if (!entry.outsideField) {
                continue;
            }

            result.addFirst(entry.text);
            if (result.size() == clamped) {
                break;
            }
        }

        return result;
    }

    public boolean isFinished() {
        return cursor >= entries.size();
    }

    public record Entry(String text, boolean outsideField) {
    }
}