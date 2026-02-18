package com.togetherjava.dukeadventure;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class BattleSystem {
    private Duke playerDuke;
    private Duke wildDuke;
    private Player player;
    private boolean playerTurn;
    private boolean battleActive;
    private String message;
    private double messageTimer;
    
    private int selectedOption = 0; // 0=Fight, 1=Item, 2=Switch, 3=Run
    private boolean inItemMenu = false;
    private boolean inSwitchMenu = false;
    private int selectedItem = 0;
    private int selectedDuke = 0;
    
    public BattleSystem(Player player, Duke wildDuke) {
        this.player = player;
        this.playerDuke = player.getStrongestDuke();
        this.wildDuke = wildDuke;
        this.playerTurn = true;
        this.battleActive = true;
        this.message = "Wild " + wildDuke.getName() + " appeared!";
        this.messageTimer = 3.0; // LONGER! 3 seconds instead of 2
        
        // GLITCHED DUKE DIALOG!
        if (wildDuke.getType().equals("glitched")) {
            this.message = Duke.getGlitchedDukeDialog();
            this.battleActive = false; // Can't fight glitched duke!
        }
    }
    
    public void update(double deltaTime) {
        if (messageTimer > 0) {
            messageTimer -= deltaTime;
        }
        
        // Auto enemy turn after delay
        if (!playerTurn && battleActive && messageTimer <= 0) {
            enemyAttack();
        }
    }
    
    public void handleInput(String key) {
        if (!battleActive || messageTimer > 0) return;
        
        if (inItemMenu) {
            handleItemMenu(key);
        } else if (inSwitchMenu) {
            handleSwitchMenu(key);
        } else {
            handleMainMenu(key);
        }
    }
    
    private void handleMainMenu(String key) {
        switch (key) {
            case "W", "UP":
                selectedOption = (selectedOption - 2 + 4) % 4;
                break;
            case "S", "DOWN":
                selectedOption = (selectedOption + 2) % 4;
                break;
            case "A", "LEFT":
                selectedOption = (selectedOption % 2 == 0) ? selectedOption : selectedOption - 1;
                break;
            case "D", "RIGHT":
                selectedOption = (selectedOption % 2 == 0) ? selectedOption + 1 : selectedOption;
                break;
            case "SPACE", "ENTER":
                executeOption();
                break;
            case "P": // Quick potion hotkey!
                usePotion();
                break;
        }
    }
    
    private void handleItemMenu(String key) {
        switch (key) {
            case "W", "UP":
                selectedItem = Math.max(0, selectedItem - 1);
                break;
            case "S", "DOWN":
                selectedItem = Math.min(1, selectedItem + 1);
                break;
            case "SPACE", "ENTER":
                useSelectedItem();
                break;
            case "ESC":
                inItemMenu = false;
                break;
        }
    }
    
    private void handleSwitchMenu(String key) {
        switch (key) {
            case "W", "UP":
                selectedDuke = Math.max(0, selectedDuke - 1);
                break;
            case "S", "DOWN":
                selectedDuke = Math.min(player.getDukes().size() - 1, selectedDuke + 1);
                break;
            case "SPACE", "ENTER":
                switchDuke();
                break;
            case "ESC":
                inSwitchMenu = false;
                break;
        }
    }
    
    private void executeOption() {
        switch (selectedOption) {
            case 0: // Fight
                playerAttack();
                break;
            case 1: // Item
                inItemMenu = true;
                break;
            case 2: // Switch
                if (player.getDukes().size() > 1) {
                    inSwitchMenu = true;
                } else {
                    message = "No other Dukes to switch to!";
                    messageTimer = 1.0;
                }
                break;
            case 3: // Run
                battleActive = false;
                message = "Got away safely!";
                break;
        }
    }
    
    private void useSelectedItem() {
        if (selectedItem == 0) {
            usePotion();
        } else if (selectedItem == 1) {
            useMaxPotion();
        }
        inItemMenu = false;
    }
    
    public void usePotion() {
        if (player.hasItem("Health Potion")) {
            player.useItem("Health Potion");
            playerDuke.heal(50);
            message = playerDuke.getName() + " restored 50 HP!";
            messageTimer = 2.5; // LONGER - 2.5 seconds
            playerTurn = false;
        } else {
            message = "No Health Potions!";
            messageTimer = 4.0; // LONGER - 2 seconds
        }
    }
    
    private void useMaxPotion() {
        if (player.hasItem("Max Potion")) {
            player.useItem("Max Potion");
            playerDuke.heal(playerDuke.getMaxHealth());
            message = playerDuke.getName() + " fully healed!";
            messageTimer = 4.0;
            playerTurn = false;
        } else {
            message = "No Max Potions!";
            messageTimer = 1.0;
        }
    }
    
    private void switchDuke() {
        Duke newDuke = player.getDukes().get(selectedDuke);
        if (newDuke == playerDuke) {
            message = "Already using " + newDuke.getName() + "!";
            messageTimer = 1.0;
            return;
        }
        
        playerDuke = newDuke;
        message = "Go, " + playerDuke.getName() + "!";
        messageTimer = 4.0;
        playerTurn = false;
        inSwitchMenu = false;
    }
    
    public void playerAttack() {
        if (!playerTurn || !battleActive) return;
        
        int damage = playerDuke.getPower() + (int)(Math.random() * 10);
        wildDuke.takeDamage(damage);
        
        message = playerDuke.getName() + " dealt " + damage + " damage!";
        messageTimer = 2.5; // LONGER - 2.5 seconds instead of 1.5
        
        if (!wildDuke.isAlive()) {
            battleActive = false;
            message = "Wild " + wildDuke.getName() + " fainted! You caught it!";
        } else {
            playerTurn = false;
        }
    }
    
    public void enemyAttack() {
        int damage = wildDuke.getPower() + (int)(Math.random() * 10);
        playerDuke.takeDamage(damage);
        
        message = wildDuke.getName() + " dealt " + damage + " damage!";
        messageTimer = 2.5; // LONGER - 2.5 seconds
        
        if (!playerDuke.isAlive()) {
            battleActive = false;
            message = playerDuke.getName() + " fainted! You lost!";
        } else {
            playerTurn = true;
        }
    }
    
    public void render(GraphicsContext gc, int width, int height) {
        // Pokemon-style battle background
        gc.setFill(Color.rgb(144, 238, 144)); // Light green grass
        gc.fillRect(0, 0, width, height);
        
        // Wild Duke (top)
        int wildX = width - 200;
        int wildY = 100;
        gc.setFill(wildDuke.getColor());
        gc.fillOval(wildX, wildY, 80, 80);
        
        // Wild Duke HP bar
        gc.setFill(Color.BLACK);
        gc.fillText(wildDuke.getName(), wildX - 50, wildY - 10);
        drawHPBar(gc, wildX - 50, wildY, wildDuke.getHealth(), wildDuke.getMaxHealth());
        
        // Player Duke (bottom)
        int playerX = 100;
        int playerY = height - 200;
        gc.setFill(playerDuke.getColor());
        gc.fillOval(playerX, playerY, 80, 80);
        
        // Player Duke HP bar
        gc.setFill(Color.BLACK);
        gc.fillText(playerDuke.getName(), playerX + 100, playerY + 20);
        drawHPBar(gc, playerX + 100, playerY + 30, playerDuke.getHealth(), playerDuke.getMaxHealth());
        
        // Dialog/Menu box
        gc.setFill(Color.WHITE);
        gc.fillRect(20, height - 120, width - 40, 100);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeRect(20, height - 120, width - 40, 100);
        
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        
        if (messageTimer > 0) {
            gc.fillText(message, 40, height - 80);
        } else if (inItemMenu) {
            renderItemMenu(gc, width, height);
        } else if (inSwitchMenu) {
            renderSwitchMenu(gc, width, height);
        } else {
            renderMainMenu(gc, width, height);
        }
    }
    
    private void renderMainMenu(GraphicsContext gc, int width, int height) {
        String[] options = {"FIGHT", "ITEM", "SWITCH", "RUN"};
        int x = 40;
        int y = height - 90;
        
        for (int i = 0; i < 4; i++) {
            if (i == selectedOption) {
                gc.setFill(Color.RED);
                gc.fillText("> " + options[i], x + (i % 2) * 200, y + (i / 2) * 30);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillText(options[i], x + 10 + (i % 2) * 200, y + (i / 2) * 30);
            }
        }
        
        gc.setFont(Font.font("Arial", 10));
        gc.setFill(Color.GRAY);
        gc.fillText("Press P for quick potion!", width - 180, height - 30);
    }
    
    private void renderItemMenu(GraphicsContext gc, int width, int height) {
        gc.fillText("ITEMS:", 40, height - 90);
        
        String[] items = {
            "Health Potion (50 HP)",
            "Max Potion (Full HP)"
        };
        
        for (int i = 0; i < items.length; i++) {
            if (i == selectedItem) {
                gc.setFill(Color.RED);
                gc.fillText("> " + items[i], 40, height - 70 + i * 20);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillText(items[i], 50, height - 70 + i * 20);
            }
        }
    }
    
    private void renderSwitchMenu(GraphicsContext gc, int width, int height) {
        gc.fillText("SWITCH DUKE:", 40, height - 90);
        
        for (int i = 0; i < player.getDukes().size(); i++) {
            Duke d = player.getDukes().get(i);
            String text = d.getName() + " (HP: " + d.getHealth() + "/" + d.getMaxHealth() + ")";
            
            if (i == selectedDuke) {
                gc.setFill(Color.RED);
                gc.fillText("> " + text, 40, height - 70 + i * 20);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillText(text, 50, height - 70 + i * 20);
            }
        }
    }
    
    private void drawHPBar(GraphicsContext gc, int x, int y, int hp, int maxHP) {
        int barWidth = 150;
        int barHeight = 10;
        
        // Background
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x, y, barWidth, barHeight);
        
        // HP bar color based on percentage
        double hpPercent = (double) hp / maxHP;
        Color hpColor;
        if (hpPercent > 0.5) {
            hpColor = Color.GREEN;
        } else if (hpPercent > 0.25) {
            hpColor = Color.YELLOW;
        } else {
            hpColor = Color.RED;
        }
        
        gc.setFill(hpColor);
        gc.fillRect(x, y, barWidth * hpPercent, barHeight);
        
        // Border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, barWidth, barHeight);
        
        // HP text
        gc.setFont(Font.font("Arial", 10));
        gc.setFill(Color.BLACK);
        gc.fillText(hp + "/" + maxHP, x + barWidth + 5, y + 9);
    }
    
    public boolean isActive() { return battleActive; }
    public boolean isPlayerTurn() { return playerTurn; }
    public Duke getWildDuke() { return wildDuke; }
    public boolean playerWon() { return !wildDuke.isAlive(); }
}
