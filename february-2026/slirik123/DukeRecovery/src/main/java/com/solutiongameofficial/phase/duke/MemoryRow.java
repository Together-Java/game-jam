package com.solutiongameofficial.phase.duke;

import lombok.Getter;

import java.util.SplittableRandom;

public final class MemoryRow {

    private static final double REGROW_SECONDS = 6.2;
    private static final double CORRUPTION_PULSE_SECONDS = 0.65;

    @Getter
    private boolean corrupted = false;
    @Getter
    private boolean removed = false;

    private double regrowTimer = 0.0;
    private double pulseTimer = 0.0;

    private char symbol = '?';

    public void update(double deltaTime, SplittableRandom random) {
        if (removed) {
            regrowTimer -= deltaTime;
            if (regrowTimer <= 0.0) {
                removed = false;
                corrupted = false;
                pulseTimer = 0.0;
            }
            return;
        }

        if (corrupted) {
            pulseTimer += deltaTime;
            if (pulseTimer >= CORRUPTION_PULSE_SECONDS) {
                pulseTimer = 0.0;
                // Let the symbol mutate a bit to feel glitchy.
                if (random.nextDouble() < 0.35) {
                    symbol = randomGlitchSymbol(random);
                }
            }
        }
    }

    public void corrupt(SplittableRandom random) {
        if (removed) {
            return;
        }
        corrupted = true;
        pulseTimer = 0.0;
        symbol = randomGlitchSymbol(random);
    }

    public void eject() {
        removed = true;
        regrowTimer = REGROW_SECONDS;
    }

    public char symbol() {
        return symbol;
    }

    private static char randomGlitchSymbol(SplittableRandom random) {
        final char[] pool = new char[] {
                '#', '@', '%', '&', '*', '+', '=', '~',
                '!', '?', '/', '\\', '^', ':', ';',
                '░', '▒', '▓'
        };
        return pool[random.nextInt(pool.length)];
    }
}