package com.togetherjava.dukeadventure;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Player {
    private int gridX, gridY;
    private int health;
    private int maxHealth;
    private int coins;
    private List<Duke> dukes;
    private Set<String> talkedToNPCs;
    private int battlesWon;
    
    // INVENTORY SYSTEM!
    private Map<String, Integer> inventory;
    
    // Animation
    private Image[] downFrames = new Image[4];
    private Image[] upFrames = new Image[4];
    private Image[] leftFrames = new Image[4];
    private Image[] rightFrames = new Image[4];
    
    private int currentFrame = 0;
    private double animationTimer = 0;
    private static final double FRAME_DURATION = 0.2; // SLOWER!
    
    private int direction = 0;
    private boolean isMoving = false;
    
    // MOVEMENT DELAY (Pokemon speed!)
    private double moveDelay = 0;
    private static final double MOVE_DELAY_TIME = 0.2; // Slower movement!
    
    public Player(int startX, int startY) {
        this.gridX = startX;
        this.gridY = startY;
        this.maxHealth = 100;
        this.health = maxHealth;
        this.coins = 100;
        this.dukes = new ArrayList<>();
        this.talkedToNPCs = new HashSet<>();
        this.battlesWon = 0;
        this.inventory = new HashMap<>();
        
        // Start with items
        inventory.put("Health Potion", 3);
        inventory.put("Max Potion", 1);
        
        dukes.add(new Duke("common", 10));
        
        loadSprites();
    }
    
    private void loadSprites() {
        for (int i = 0; i < 4; i++) {
            downFrames[i] = ImageLoader.loadPlayer("down/" + (i + 1) + ".png");
            upFrames[i] = ImageLoader.loadPlayer("up/" + (i + 1) + ".png");
            leftFrames[i] = ImageLoader.loadPlayer("left/" + (i + 1) + ".png");
            rightFrames[i] = ImageLoader.loadPlayer("right/" + (i + 1) + ".png");
        }
        
        if (downFrames[0] == null) {
            Image defaultSprite = ImageLoader.loadPlayer("default.png");
            for (int i = 0; i < 4; i++) {
                downFrames[i] = defaultSprite;
                upFrames[i] = defaultSprite;
                leftFrames[i] = defaultSprite;
                rightFrames[i] = defaultSprite;
            }
        }
        
        System.out.println("✅ Player sprites loaded!");
    }
    
    public boolean canMove() {
        return moveDelay <= 0;
    }
    
    public boolean move(int dx, int dy, World world) {
        if (!canMove()) return false;
        
        int newX = gridX + dx;
        int newY = gridY + dy;
        
        if (world.canMoveTo(newX, newY)) {
            gridX = newX;
            gridY = newY;
            isMoving = true;
            moveDelay = MOVE_DELAY_TIME; // Reset delay
            
            if (dy < 0) direction = 1;
            else if (dy > 0) direction = 0;
            else if (dx < 0) direction = 2;
            else if (dx > 0) direction = 3;
            
            return true;
        }
        return false;
    }
    
    public void update(double deltaTime) {
        if (moveDelay > 0) {
            moveDelay -= deltaTime;
        }
        
        if (isMoving) {
            animationTimer += deltaTime;
            
            if (animationTimer >= FRAME_DURATION) {
                animationTimer = 0;
                currentFrame = (currentFrame + 1) % 4;
            }
        } else {
            currentFrame = 0;
            animationTimer = 0;
        }
        
        isMoving = false;
    }
    
    public void render(GraphicsContext gc, int tileSize, Camera camera) {
        int screenX = camera.worldToScreenX(gridX * tileSize);
        int screenY = camera.worldToScreenY(gridY * tileSize);
        
        Image currentSprite = getCurrentSprite();
        
        if (currentSprite != null) {
            gc.drawImage(currentSprite, screenX, screenY, tileSize, tileSize);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(screenX, screenY, tileSize, tileSize);
        }
    }
    
    private Image getCurrentSprite() {
        Image[] frames = switch (direction) {
            case 0 -> downFrames;
            case 1 -> upFrames;
            case 2 -> leftFrames;
            case 3 -> rightFrames;
            default -> downFrames;
        };
        
        return frames[currentFrame];
    }
    
    // INVENTORY METHODS
    public boolean hasItem(String item) {
        return inventory.getOrDefault(item, 0) > 0;
    }
    
    public void useItem(String item) {
        if (hasItem(item)) {
            inventory.put(item, inventory.get(item) - 1);
        }
    }
    
    public void addItem(String item, int amount) {
        inventory.put(item, inventory.getOrDefault(item, 0) + amount);
    }
    
    public int getItemCount(String item) {
        return inventory.getOrDefault(item, 0);
    }
    
    // Getters
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getCoins() { return coins; }
    public int getDukeCount() { return dukes.size(); }
    
    public void addDuke(Duke duke) {
        dukes.add(duke);
        System.out.println("Caught " + duke.getName() + "! Total: " + dukes.size());
    }
    
    public Duke getStrongestDuke() {
        if (dukes.isEmpty()) {
            return new Duke("common", 5);
        }
        
        // Get first ALIVE duke
        for (Duke duke : dukes) {
            if (duke.isAlive()) {
                return duke;
            }
        }
        
        // All fainted - heal first one
        dukes.get(0).heal(dukes.get(0).getMaxHealth());
        return dukes.get(0);
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }
    
    public void healAllDukes() {
        for (Duke duke : dukes) {
            duke.heal(duke.getMaxHealth());
        }
        System.out.println("All Dukes healed!");
    }
    
    public void addCoins(int amount) {
        coins += amount;
    }
    
    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }
    
    public void talkToNPC(String npcName) {
        talkedToNPCs.add(npcName);
    }
    
    public boolean hasTalkedTo(String npcName) {
        return talkedToNPCs.contains(npcName);
    }
    
    public void winBattle() {
        battlesWon++;
    }
    
    public int getBattlesWon() {
        return battlesWon;
    }
    
    public List<Duke> getDukes() {
        return dukes;
    }
}
