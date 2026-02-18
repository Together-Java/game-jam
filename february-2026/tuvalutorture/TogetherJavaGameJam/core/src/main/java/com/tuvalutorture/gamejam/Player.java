package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

public class Player implements Disposable {
    public Creature playerEntity;
    private int maxHealth;
    private int health;
    private int cooldownTime;
    private int cooldown;

    public Player() {
        maxHealth = 3;
        health = 1;
        cooldownTime = 5;
        cooldown = 3;
        playerEntity = new Creature("duke", "default.png");

        playerEntity.setUpDirectionalAnim("right", 4, Creature.EntityDirection.RIGHT, 16, 16, 0.1f);
        playerEntity.setUpDirectionalAnim("left", 4, Creature.EntityDirection.LEFT, 16, 16, 0.1f);
        playerEntity.setUpDirectionalAnim("up", 4, Creature.EntityDirection.UP, 16, 16, 0.1f);
        playerEntity.setUpDirectionalAnim("down", 4, Creature.EntityDirection.DOWN, 16, 16, 0.1f);

        playerEntity.setSpriteHeight(16);
        playerEntity.setSpriteWidth(16);

        playerEntity.loadDirectionTextures();
        playerEntity.setSpeed(60f);

        playerEntity.room = GameState.currentRoom;
    }

    public void sacrifice() {
        this.maxHealth -= 1;
        if (this.health >= this.maxHealth) this.health = this.maxHealth;
        this.cooldownTime -= 1;
        this.cooldown = 0;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public int getCooldownTime() {
        return cooldownTime;
    }

    public int getCooldown() {
        return (int)Math.floor(cooldown);
    }

    public void heal() {
        if (health < maxHealth) health += 1;
    }

    public void centreOnScreen() {
        playerEntity.mapX = GameMap.resolvePixelToCoordinate(Gdx.graphics.getWidth() / 2);
        playerEntity.mapY = GameMap.resolvePixelToCoordinate(Gdx.graphics.getHeight() / 2);

        playerEntity.pixelX = GameMap.resolveCoordinateToPixel(playerEntity.mapX);
        playerEntity.pixelY = GameMap.resolveCoordinateToPixel(playerEntity.mapY);
    }

    public void dispose() {
        GameState.currentRoom.roomMap.dispose();
        GameState.dispose();
    }
}
