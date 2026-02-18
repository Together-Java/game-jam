package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public final class UserInterface {
    private final static int tilePixelSize = 16;
    private final static int healthSegmentWidth = 6;
    private final static int healthSegmentHeight = 16;
    private final static int laserSegmentWidth = 6;
    private final static int laserSegmentHeight = 10;
    private final static int ui_scale = 3;
    private final static Texture fullHealthBarSegment = new Texture(Gdx.files.internal("healthBarSegment.png"));
    private final static Texture fullLaserBarSegment = new Texture(Gdx.files.internal("laserBarSegment.png"));
    private final static Texture emptyHealthBarSegment = new Texture(Gdx.files.internal("emptyHealthBarSegment.png"));
    private final static Texture emptyLaserBarSegment  = new Texture(Gdx.files.internal("emptyLaserBarSegment.png"));
    private final static FontRenderer fontRenderer = new FontRenderer("white", 8, 8, 2);
    private final static FontRenderer notices = new FontRenderer("black", 8, 8, 2);
    // private final Texture dialogueBox;

    private static void drawHealthBar(int currentHealth, int maxHealth, SpriteBatch ui_batch) {
        for (int i = 0; i < currentHealth; i++) {
            ui_batch.draw(fullHealthBarSegment, 0, (479 - ui_scale) - (((healthSegmentHeight * ui_scale) - ui_scale) * (i + 1)), healthSegmentWidth * ui_scale, healthSegmentHeight * ui_scale);
        }

        for (int i = currentHealth; i < maxHealth; i++) {
            ui_batch.draw(emptyHealthBarSegment, 0, (479 - ui_scale) - (((healthSegmentHeight * ui_scale) - ui_scale) * (i + 1)), healthSegmentWidth * ui_scale, healthSegmentHeight * ui_scale);
        }
    }

    private static void drawLaserBar(int currentLaser, int maxLaser, SpriteBatch ui_batch) {
        for (int i = 0; i < maxLaser - currentLaser; i++) {
            ui_batch.draw(fullLaserBarSegment, healthSegmentWidth * ui_scale, (479 - ui_scale) - (((laserSegmentHeight * ui_scale) - ui_scale) * (i + 1)), laserSegmentWidth * ui_scale, laserSegmentHeight * ui_scale);
        }

        for (int i = maxLaser - currentLaser; i < maxLaser; i++) {
            ui_batch.draw(emptyLaserBarSegment, healthSegmentWidth * ui_scale, (479 - ui_scale) - (((laserSegmentHeight * ui_scale) - ui_scale) * (i + 1)), laserSegmentWidth * ui_scale, laserSegmentHeight * ui_scale);
        }
    }

    private static void drawCurrentItems(Item[] drawn) {

    }

    public static void draw(SpriteBatch batch) {
        drawHealthBar(GameState.player.getHealth(), GameState.player.getMaxHealth(), batch);
        drawLaserBar(GameState.player.getCooldown(), GameState.player.getCooldownTime(), batch);
    }

    public static void dispose() {
        fullHealthBarSegment.dispose();
        fullLaserBarSegment.dispose();
        emptyHealthBarSegment.dispose();
        emptyLaserBarSegment.dispose();
        fontRenderer.dispose();
        notices.dispose();
    }
}
