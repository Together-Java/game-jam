package com.solutiongameofficial.game;

import com.solutiongameofficial.game.hud.Hud;
import com.solutiongameofficial.game.hud.HudSnapshot;
import com.solutiongameofficial.graphics.Renderer;
import com.solutiongameofficial.graphics.ScreenCompositor;
import com.solutiongameofficial.io.InputFacade;
import com.solutiongameofficial.phase.Phase;
import com.solutiongameofficial.phase.intro.IntroPhase;
import com.solutiongameofficial.phase.loading.LoadingPhase;
import lombok.NonNull;

import java.awt.image.BufferedImage;

public final class GameLoopRunner {

    private static final double FIXED_DELTA_TIME = 1d / 60d;
    private static final long FRAME_NANOSECONDS = 1_000_000_000L / 60L;
    private static final double MAXIMUM_DELTA_TIME = 0.25;

    private final InputFacade input;
    private final Renderer renderer;
    private final ScreenCompositor compositor;
    private final Hud hud;

    private final PhaseFlow phaseFlow;
    private final HudRenderWorker hudWorker;
    private final FramePacer pacer;
    private final PostProcessor postProcessor;

    private boolean running = true;
    private double accumulatorSeconds = 0d;

    public GameLoopRunner(@NonNull InputFacade input,
                          @NonNull Phase[] phases,
                          @NonNull Renderer renderer,
                          boolean postProcessingEnabled)
    {
        this.input = input;
        this.renderer = renderer;

        this.compositor = new ScreenCompositor(1920, 1080, ScreenCompositor.FitMode.FIT_LETTERBOX);
        this.hud = new Hud();

        this.phaseFlow = new PhaseFlow(phases, new DukePhaseController());
        this.hudWorker = new HudRenderWorker();
        this.pacer = new FramePacer(FRAME_NANOSECONDS, MAXIMUM_DELTA_TIME);
        this.postProcessor = new PostProcessor(postProcessingEnabled);
    }

    public void run() {
        hudWorker.start();
        submitHudSnapshot();

        while (running) {
            accumulatorSeconds += pacer.consumeDeltaSeconds();

            handleInputActions();
            stepSimulation();
            renderIfDue();
            pacer.sleepUntilNextFrame();
        }

        hudWorker.shutdown();
    }

    private void handleInputActions() {
        GameAction action;
        while ((action = input.poll().orElse(null)) != null) {
            if (action == GameAction.QUIT) {
                running = false;
                return;
            }

            if (phaseFlow.currentPhase().update(action, 0d)) {
                if (phaseFlow.advance()) {
                    running = false;
                    return;
                }
                updateHudObjectiveText();
            }
        }
    }

    private void stepSimulation() {
        boolean hudDirty = false;

        while (accumulatorSeconds >= FIXED_DELTA_TIME && running) {
            hud.update(FIXED_DELTA_TIME);

            phaseFlow.tick(FIXED_DELTA_TIME);

            hudDirty = true;

            if (phaseFlow.currentPhase().update(null, FIXED_DELTA_TIME)) {
                if (phaseFlow.advance()) {
                    running = false;
                    return;
                }
                updateHudObjectiveText();
            }

            accumulatorSeconds -= FIXED_DELTA_TIME;
        }

        if (hudDirty) {
            submitHudSnapshot();
        }
    }

    private void updateHudObjectiveText() {
        if (phaseFlow.isInDukePhase()) {
            hud.setObjectiveText("Internal System Error 500");
            return;
        }

        Phase current = phaseFlow.currentPhase();
        String name = current.getClass().getSimpleName().replace("Phase", "");
        hud.setObjectiveText("Solve the " + name);
    }

    private void renderIfDue() {
        if (!running || !pacer.isFrameDue()) {
            return;
        }

        BufferedImage phaseImage = phaseFlow.currentPhase().content();

        BufferedImage hudImage = hudWorker.latestImageOrFallback();
        BufferedImage composed = compositor.compose(phaseImage, hudImage);

        renderer.present(postProcessor.apply(composed));

        pacer.markFramePresented();
    }

    private void submitHudSnapshot() {
        HudSnapshot snapshot = hud.snapshot();
        hudWorker.submit(snapshot);
    }

    // Used by PhaseFlow to avoid Duke phase in Intro
    static boolean isIntroOrLoadingPhase(Phase phase) {
        return phase instanceof IntroPhase || phase instanceof LoadingPhase;
    }
}