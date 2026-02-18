package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Disposable;

public class Anim implements Disposable {
    public float frameTime;
    public Texture[] frames;
    public int frameWidth, frameHeight;
    private int frameCount;
    private float timeElapsed;
    private int currentFrame;
    private boolean paused = false, hasFinished = true;
    private PlayMode playbackMode;

    public Anim(Texture[] frames, int frameWidth, int frameHeight, float frameTime) {
        this.frames = frames;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.timeElapsed = 0f;
        this.frameCount = frames.length;
        this.currentFrame = 0;
        this.frameTime = frameTime;
        this.paused = false;
        this.playbackMode = PlayMode.LOOP;
    }

    public Texture returnCurrentFrame() {
        return frames[currentFrame];
    }

    public void update(float delta) {
        if (paused) return;
        timeElapsed += delta;
        while (timeElapsed > frameTime) {
            timeElapsed -= frameTime;
            currentFrame += ((this.playbackMode == PlayMode.LOOP || this.playbackMode == PlayMode.NORMAL) ? 1 : -1);
        }
        if (currentFrame > frameCount - 1 || currentFrame < 0) {
            if (playbackMode == PlayMode.LOOP) currentFrame = 0;
            else if (playbackMode == PlayMode.LOOP_REVERSED) currentFrame = frameCount - 1;
            else this.setPaused(true);
        }
    }

    public void setPlaybackMode(PlayMode playbackMode) {
        this.playbackMode = playbackMode;
    }

    public void resetFrame() {
        currentFrame = ((this.playbackMode == PlayMode.LOOP || this.playbackMode == PlayMode.NORMAL) ? 0 : frameCount - 1);
        timeElapsed = 0;
    }

    public PlayMode getPlaybackMode() {
        return playbackMode;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public static Anim loadAnimFromFolder(String path, int frameWidth, int frameHeight, int frameCount, float frameTime) { // FUCK YOU, I MAKE MY OWN PROPRIETARY SHIT THAT WILL BREAK AND CONFUSE ME AND YOU
        Texture[] frames = new Texture[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new Texture(GameState.loadAsset(path + "/" + String.valueOf(i + 1) + ".png"));
        }
        return new Anim(frames, frameWidth, frameHeight, frameTime);
    }

    public void dispose() {
        for (Texture frame : frames) {
            frame.dispose();
        }
        frames = null;
    }
}
