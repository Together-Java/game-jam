package com.togetherjava.dukeadventure;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Shop system for buying items.
 * TJ-Bot is the shopkeeper!
 */
public class ShopSystem {
    
    public static class ShopItem {
        public String name;
        public String description;
        public int price;
        public String key; // Keyboard key to buy
        
        public ShopItem(String name, String description, int price, String key) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.key = key;
        }
    }
    
    private Player player;
    private ShopItem[] items;
    private String message;
    
    public ShopSystem(Player player) {
        this.player = player;
        this.message = "Welcome to TJ-Bot's shop! \nWhat would you like to buy?";
        
        // Define shop items
        items = new ShopItem[] {
            new ShopItem("Health Potion", "Restores 30 HP to your Duke", 50, "1"),
            new ShopItem("Max Potion", "Fully restores Duke HP", 100, "2"),
            new ShopItem("Duke Ball", "Catch Dukes easier (not implemented)", 150, "3"),
            new ShopItem("Rare Candy", "Level up a Duke (not implemented)", 200, "4")
        };
    }
    
    /**
     * Buy an item
     */
    public boolean buyItem(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= items.length) return false;
        
        ShopItem item = items[itemIndex];
        
        if (player.spendCoins(item.price)) {
            message = "Purchased " + item.name + "! \nIt's in your inventory.";
            return true;
        } else {
            message = "Not enough coins! \nYou need " + item.price + " coins.";
            return false;
        }
    }
    
    /**
     * Render the shop
     */
    public void render(GraphicsContext gc, int width, int height) {
        // Background
        gc.setFill(Color.WHEAT);
        gc.fillRect(0, 0, width, height);
        
        // Title
        gc.setFill(Color.BLACK);
        gc.fillText("TJ-Bot's Shop", width / 2 - 50, 50);
        gc.fillText("Your coins: " + player.getCoins(), width / 2 - 50, 70);
        
        // Shop items
        int y = 120;
        for (int i = 0; i < items.length; i++) {
            ShopItem item = items[i];
            gc.fillText("[" + item.key + "] " + item.name + " - " + item.price + " coins", 50, y);
            gc.fillText("    " + item.description, 50, y + 15);
            y += 50;
        }
        
        // Message
        gc.setFill(Color.WHITE);
        gc.fillRect(20, height - 80, width - 40, 60);
        gc.setFill(Color.BLACK);
        gc.strokeRect(20, height - 80, width - 40, 60);
        gc.fillText(message, 30, height - 55);
        
        // Exit instruction
        gc.fillText("[ESC] Exit shop", 30, height - 10);
    }
    
    // Getters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
