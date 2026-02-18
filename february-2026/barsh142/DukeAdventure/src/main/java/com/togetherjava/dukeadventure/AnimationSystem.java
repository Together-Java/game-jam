package com.togetherjava.dukeadventure;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class AnimationSystem {
    private Image spriteSheet;
    private Image[] frames;
    private int currentFrameIndex;
    private double frameTime; // Time per frame in seconds
    private double elapsedTime;
    private boolean loop;

    public AnimationSystem(Image spriteSheet, int frameCount, int frameWidth, int frameHeight, double frameTime) {
        this.spriteSheet = spriteSheet;
        this.frameTime = frameTime;
        this.loop = true;
        this.currentFrameIndex = 0;
        this.elapsedTime = 0;
        extractFrames(frameCount, frameWidth, frameHeight);
    }

    private void extractFrames(int frameCount, int frameWidth, int frameHeight) {
        frames = new Image[frameCount];
        
        if (spriteSheet == null) {
            // No sprite sheet - create empty frames
            return;
        }
        
        for (int i = 0; i < frameCount; i++) {
            int x = i * frameWidth;
            try {
                frames[i] = new WritableImage(
                    spriteSheet.getPixelReader(),
                    x, 0,
                    frameWidth, frameHeight
                );
            } catch (Exception e) {
                frames[i] = null;
            }
        }
    }

    public void update(double deltaTime) {
        elapsedTime += deltaTime;
        
        if (elapsedTime >= frameTime) {
            elapsedTime = 0;
            currentFrameIndex++;
            
            if (currentFrameIndex >= frames.length) {
                if (loop) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frames.length - 1;
                }
            }
        }
    }

    public Image getCurrentFrame() {
        if (frames == null || frames.length == 0 || frames[currentFrameIndex] == null) {
            return null;
        }
        return frames[currentFrameIndex];
    }

    public void reset() {
        currentFrameIndex = 0;
        elapsedTime = 0;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    

    public boolean isFinished() {
        return !loop && currentFrameIndex == frames.length - 1;
    }

    public void setFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < frames.length) {
            currentFrameIndex = frameIndex;
            elapsedTime = 0;
        }
    }

    public static AnimationSystem fromSingleImage(Image image) {
        AnimationSystem anim = new AnimationSystem(image, 1, (int)image.getWidth(), (int)image.getHeight(), 1.0);
        return anim;
    }
}
