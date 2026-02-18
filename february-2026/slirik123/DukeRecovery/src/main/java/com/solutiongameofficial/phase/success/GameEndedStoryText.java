package com.solutiongameofficial.phase.success;

public final class GameEndedStoryText {

    private static final double CHARACTER_PER_SECOND = 28.0;
    private static final double LINE_PAUSE_SECONDS = 0.5;

    private final String[] lines = new String[] {
            "RECOVERY COMPLETE.",
            "Corruption contained. Core signal stabilized.",
            "",
            "Vey exhales. Not because she needs air.",
            "Because the system finally lets her.",
            "",
            "DUKE: \"Successfully recovered all fragments.\"",
            "DUKE: \"Disconnecting host from DUKE.\"",
            "",
            "A clean thread. A quiet heartbeat.",
            "Somewhere deep inside the machine, the lights stay on."
    };

    private int currentLineIndex;
    private int revealedCharactersInLine;

    private double typeAccumulator;
    private double pauseSecondsRemaining;

    private boolean fullyRevealed;
    private double secondsSinceFullyRevealed;

    public void update(double deltaTimeSeconds) {
        double deltaTime = Math.max(0.0, deltaTimeSeconds);

        if (fullyRevealed) {
            secondsSinceFullyRevealed += deltaTime;
            return;
        }

        if (pauseSecondsRemaining > 0.0) {
            pauseSecondsRemaining -= deltaTime;
            if (pauseSecondsRemaining < 0.0) {
                pauseSecondsRemaining = 0.0;
            }
            return;
        }

        typeAccumulator += deltaTime * CHARACTER_PER_SECOND;

        while (typeAccumulator >= 1.0 && !fullyRevealed) {
            typeAccumulator -= 1.0;
            revealNextCharacter();
        }
    }

    public boolean isFullyRevealed() {
        return !fullyRevealed;
    }

    public double secondsSinceFullyRevealed() {
        return secondsSinceFullyRevealed;
    }

    public int lineCount() {
        return lines.length;
    }

    public String visibleLine(int index) {
        if (index < 0 || index >= lines.length) {
            return "";
        }

        if (fullyRevealed) {
            return lines[index];
        }

        if (index < currentLineIndex) {
            return lines[index];
        }

        if (index > currentLineIndex) {
            return "";
        }

        String line = lines[index];
        int count = Math.min(revealedCharactersInLine, line.length());
        return line.substring(0, count);
    }

    private void revealNextCharacter() {
        if (currentLineIndex >= lines.length) {
            fullyRevealed = true;
            secondsSinceFullyRevealed = 0.0;
            return;
        }

        String line = lines[currentLineIndex];

        if (revealedCharactersInLine < line.length()) {
            revealedCharactersInLine++;
            return;
        }

        currentLineIndex++;
        revealedCharactersInLine = 0;
        pauseSecondsRemaining = LINE_PAUSE_SECONDS;

        if (currentLineIndex >= lines.length) {
            fullyRevealed = true;
            secondsSinceFullyRevealed = 0.0;
        }
    }
}