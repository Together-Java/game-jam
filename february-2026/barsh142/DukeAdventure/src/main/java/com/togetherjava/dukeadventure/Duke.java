package com.togetherjava.dukeadventure;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Random;

public class Duke {
    private String type;
    private int power;
    private int health;
    private int maxHealth;
    private String name;
    private Color color;
    private Image sprite;
    
    private static Random rand = new Random();
    private static boolean glitchedDukeFound = false; // Track if glitched duke was found
    
    public Duke(String type, int level) {
        this.type = type;
        this.power = calculatePower(type, level);
        this.maxHealth = 50 + (level * 10);
        this.health = maxHealth;
        this.name = getTypeName(type);
        this.color = getTypeColor(type);
        
        loadSprite();
    }
    
    private void loadSprite() {
        this.sprite = ImageLoader.loadImage("dukes/" + type + "_duke.png", 64, 64);
    }
    
    private int calculatePower(String type, int level) {
        int basePower = switch (type) {
            case "common" -> 10;
            case "rare" -> 20;
            case "dev" -> 25;
            case "coffee" -> 25;
            case "gold" -> 30;
            case "rainbow" -> 40;
            case "glitched" -> 50;
            default -> 10;
        };
        return basePower + (level * 2);
    }
    
    private String getTypeName(String type) {
        return switch (type) {
            case "common" -> "Common Duke";
            case "rare" -> "Rare Duke";
            case "dev" -> "Dev Duke";
            case "coffee" -> "Coffee Duke";
            case "gold" -> "Gold Duke";
            case "rainbow" -> "Rainbow Duke";
            case "glitched" -> "G̴L̸I̷T̶C̵H̶E̵D̸ Duke";
            default -> "Duke";
        };
    }
    
    private Color getTypeColor(String type) {
        return switch (type) {
            case "common" -> Color.GRAY;
            case "rare" -> Color.BLUE;
            case "dev" -> Color.DARKGREEN;
            case "coffee" -> Color.SADDLEBROWN;
            case "gold" -> Color.GOLD;
            case "rainbow" -> Color.MAGENTA;
            case "glitched" -> Color.rgb(255, 0, 255, 0.5); // Glitchy transparent magenta
            default -> Color.GRAY;
        };
    }

    public static Duke createWildDuke() {
        int roll = rand.nextInt(100);
        
        // GLITCHED DUKE EASTER EGG! (3% chance, one-time only)
        if (!glitchedDukeFound && roll < 3) {
            glitchedDukeFound = true;
            System.out.println("🎉 GLITCHED DUKE APPEARED! (Easter egg found!)");
            return new Duke("glitched", 15);
        }
        
        String type;
        if (roll < 40) {
            type = "common";      // 40%
        } else if (roll < 65) {
            type = "rare";        // 25%
        } else if (roll < 77) {
            type = "dev";         // 12%
        } else if (roll < 89) {
            type = "coffee";      // 12%
        } else if (roll < 97) {
            type = "gold";        // 8%
        } else {
            type = "rainbow";     // 3%
        }
        
        int level = 5 + rand.nextInt(10); // Level 5-14
        return new Duke(type, level);
    }
    
    /**
     * Get special dialog for Glitched Duke encounter
     */
    public static String getGlitchedDukeDialog() {
        return "fight- wait... who are YOU?!";
    }
    
    public static boolean isGlitchedDukeFound() {
        return glitchedDukeFound;
    }
    
    // Getters
    public String getType() { return type; }
    public int getPower() { return power; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public String getName() { return name; }
    public Color getColor() { return color; }
    public Image getSprite() { return sprite; }
    public String getDisplayName() { return name; }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }
}
