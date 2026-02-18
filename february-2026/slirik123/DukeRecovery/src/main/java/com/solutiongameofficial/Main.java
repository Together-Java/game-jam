package com.solutiongameofficial;

import com.solutiongameofficial.game.GameLoopRunner;
import com.solutiongameofficial.io.InputFacade;
import com.solutiongameofficial.io.PngStreamRenderer;
import com.solutiongameofficial.io.StdioInputAdapter;
import com.solutiongameofficial.phase.Phase;
import com.solutiongameofficial.phase.checksum.ChecksumPhase;
import com.solutiongameofficial.phase.intro.IntroPhase;
import com.solutiongameofficial.phase.loading.LoadingPhase;
import com.solutiongameofficial.phase.maze.MazePhase;
import com.solutiongameofficial.phase.success.GameEndedPhase;
import com.solutiongameofficial.swing.Frame;
import com.solutiongameofficial.swing.SwingInputAdapter;
import com.solutiongameofficial.swing.SwingRenderer;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] arguments) {
        boolean headless = GraphicsEnvironment.isHeadless() || hasArguments(arguments,"--headless");

        InputFacade input = new InputFacade();
        Phase[] phases = {
                new LoadingPhase(),
                new IntroPhase(),
                new ChecksumPhase(),
                new MazePhase(),
                new GameEndedPhase()
        };

        boolean postProcessingEnabled = !hasArguments(arguments,"--post-processing-disabled");
        if (headless) {
            runHeadless(input, phases, postProcessingEnabled);
        } else {
            runSwing(input, phases, postProcessingEnabled);
        }
    }

    private static void runHeadless(InputFacade input, Phase[] phases, boolean postProcessingEnabled) {
        try (StdioInputAdapter stdio = new StdioInputAdapter(input, System.in, System.out)) {
            stdio.start();
            new GameLoopRunner(input, phases, new PngStreamRenderer(System.out), postProcessingEnabled).run();
        }
    }

    private static void runSwing(InputFacade input, Phase[] phases, boolean postProcessingEnabled) {
        SwingUtilities.invokeLater(() -> {
            Frame frame = new Frame((graphics2D, scaleX, scaleY) -> {});

            new SwingInputAdapter(input).bind(frame.getRootPane());

            new Thread(() -> new GameLoopRunner(input, phases, new SwingRenderer(frame), postProcessingEnabled).run(), "game-loop").start();
        });
    }

    private static boolean hasArguments(String[] arguments, String targetArgument) {
        for (String argument : arguments) {
            if (argument.equals(targetArgument)) {
                return true;
            }
        }
        return false;
    }
}