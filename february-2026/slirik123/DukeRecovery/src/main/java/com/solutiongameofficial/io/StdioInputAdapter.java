package com.solutiongameofficial.io;

import com.solutiongameofficial.game.GameAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class StdioInputAdapter implements AutoCloseable {

    private final InputFacade input;
    private final BufferedReader reader;
    private final PrintStream output;
    private final Thread thread;
    private volatile boolean running = true;

    public StdioInputAdapter(InputFacade input, InputStream inputStream, PrintStream outputStream) {
        this.input = input;
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        this.output = outputStream;

        this.thread = new Thread(this::loop, "stdio-input");
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
        output.println("STDIN controls: UP, LEFT, DOWN, RIGHT");
        output.flush();
    }

    private void loop() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                GameAction action = parse(line);
                if (action != null) {
                    input.publish(action);
                } else {
                    output.println("Unknown input: " + line);
                    output.flush();
                }
            }
        } catch (IOException exception) {
            if (running) {
                output.println("STDIN error: " + exception.getMessage());
                output.flush();
            }
        }
    }

    private GameAction parse(String raw) {
        String upperCase = raw.toUpperCase(Locale.ROOT);

        return switch (upperCase) {
            case "UP" -> GameAction.MOVE_UP;
            case "LEFT" -> GameAction.MOVE_LEFT;
            case "DOWN" -> GameAction.MOVE_DOWN;
            case "RIGHT" -> GameAction.MOVE_RIGHT;
            default -> null;
        };
    }

    @Override
    public void close() {
        running = false;
        thread.interrupt();
    }
}
