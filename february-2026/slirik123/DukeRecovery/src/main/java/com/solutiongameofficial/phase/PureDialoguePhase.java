package com.solutiongameofficial.phase;

import com.solutiongameofficial.game.Dialogue;
import com.solutiongameofficial.game.GameAction;
import com.solutiongameofficial.graphics.TextBoxOverlay;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class PureDialoguePhase implements Phase {

    private final double SECONDS_PER_FRAME;
    private final int FRAME_X;
    private final int FRAME_Y;
    private final int TEXTBOX_X;
    private final int TEXTBOX_Y;

    private final BufferedImage canvas = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
    private final TextBoxOverlay overlay = new TextBoxOverlay();
    private final Dialogue dialogue = dialogue();
    private final BufferedImage[] frames;

    private double elapsedSeconds;

    public PureDialoguePhase(BufferedImage[] frames) {
        this(frames,2, 600, 400, 1120, 750);
    }

    public PureDialoguePhase(
            BufferedImage[] frames,
            double imageSecondsPerFrame,
            int frameX,
            int frameY,
            int textX,
            int textY
    ) {
        this.frames = frames;
        this.SECONDS_PER_FRAME = imageSecondsPerFrame;
        this.FRAME_X = frameX;
        this.FRAME_Y = frameY;
        this.TEXTBOX_X = textX;
        this.TEXTBOX_Y = textY;
    }

    @Override
    public boolean update(GameAction action, double deltaTime) {
        elapsedSeconds += deltaTime;
        dialogue.update(deltaTime);

        boolean dukeAnimFinished = isDukeAnimFinished();
        return dukeAnimFinished && dialogue.isFinished();
    }

    @Override
    public BufferedImage content() {
        Graphics2D graphics2D = canvas.createGraphics();
        try {
            graphics2D.setComposite(AlphaComposite.Src);
            graphics2D.setColor(Color.BLACK);
            graphics2D.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            graphics2D.setComposite(AlphaComposite.SrcOver);
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            drawFrame(graphics2D);
            overlay.draw(graphics2D, TEXTBOX_X, TEXTBOX_Y, dialogue);
        } finally {
            graphics2D.dispose();
        }

        return canvas;
    }

    private void drawFrame(Graphics2D graphics2D) {
        int frameIndex = (int) Math.floor(elapsedSeconds / SECONDS_PER_FRAME);
        frameIndex = Math.min(frameIndex, frames.length - 1);

        BufferedImage frame = frames[frameIndex];
        graphics2D.drawImage(frame, FRAME_X, FRAME_Y, 500, 500, null);
    }

    private boolean isDukeAnimFinished() {
        double totalAnimationSeconds = frames.length * SECONDS_PER_FRAME;

        return elapsedSeconds >= totalAnimationSeconds;
    }

    protected abstract Dialogue dialogue();
}
