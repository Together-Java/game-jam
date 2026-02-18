package com.togetherjava.dukeadventure;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class NPC {
    private String name;
    private String role;
    private int gridX, gridY;
    private Image sprite;
    private boolean isGlitched;
    
    public NPC(String name, String role, int x, int y) {
        this.name = name;
        this.role = role;
        this.gridX = x;
        this.gridY = y;
        this.isGlitched = false;
        loadSprite();
    }

    public static NPC createGlitchedDuke(int x, int y) {
        NPC glitched = new NPC("??? Glitched Duke ???", "ERROR_404", x, y);
        glitched.isGlitched = true;
        glitched.sprite = ImageLoader.loadDuke("glitched_duke.png");
        return glitched;
    }
    
    /**
     * Load NPC sprite
     */
    private void loadSprite() {
        String filename = name.toLowerCase().replace(" ", "").replace("-", "").replace("(", "").replace(")", "") + ".png";
        this.sprite = ImageLoader.loadNPC(filename);
    }
    
    /**
     * Render the NPC with camera offset
     */
    public void render(GraphicsContext gc, int tileSize, Camera camera) {
        int worldX = gridX * tileSize;
        int worldY = gridY * tileSize;
        
        int screenX = camera.worldToScreenX(worldX);
        int screenY = camera.worldToScreenY(worldY);
        
        // Only render if visible
        if (!camera.isTileVisible(gridX, gridY)) {
            return;
        }
        
        // Draw sprite or placeholder
        if (sprite != null) {
            gc.drawImage(sprite, screenX, screenY, tileSize, tileSize);
        } else {
            // Placeholder color based on NPC type
            if (isGlitched) {
                // Glitched Duke - flashing red/white
                gc.setFill(Math.random() > 0.5 ? Color.RED : Color.WHITE);
            } else if (name.equals("TJ-Bot")) {
                gc.setFill(Color.GOLD);
            } else {
                gc.setFill(Color.GREEN);
            }
            gc.fillRect(screenX, screenY, tileSize, tileSize);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(screenX, screenY, tileSize, tileSize);
        }
        
        // Draw name above NPC (optional)
        if (isGlitched) {
            gc.setFill(Color.RED);
            gc.fillText("???", screenX, screenY - 5);
        }
    }

    public DialogSystem.DialogNode getDialogue(QuestSystem questSystem) {
        // Special dialogue for Glitched Duke
        if (isGlitched) {
            return DialogSystem.createGlitchedDukeDialogue();
        }
        
        // Quest-giver NPCs with special dialogue
        switch (name) {
            case "Zabuzard":
                return DialogSystem.createZabuzardDialogue(questSystem);
            case "Wazei":
                return DialogSystem.createWazeiDialogue(questSystem);
            case "Marko":
                return DialogSystem.createMarkoDialogue();
            case "Adi":
                return DialogSystem.createAdiDialogue();
            case "SoLuckySeven":
                return DialogSystem.createSoLuckySevenDialogue();
            case "FirasG":
                return DialogSystem.createFirasGDialogue(questSystem);
            case "Alathreon":
                return DialogSystem.createAlathreonDialogue();
            case "SquidxTV":
                return DialogSystem.createSquidxTVDialogue();
            case "Christolis":
                return DialogSystem.createchristolisDialogue(questSystem);
            case "barshERROR(404)":
                return DialogSystem.createBarshErrorDialogue();
            case "Number1Engineer":
                return DialogSystem.createNumber1EngineerDialogue(questSystem);
            case "AlphaBee":
                return DialogSystem.createAlphaBeeDialogue(questSystem);
            default:
                // Fallback (shouldn't happen now!)
                DialogSystem.DialogNode fallback = new DialogSystem.DialogNode(
                    "Hello! I'm " + name + "!"
                );
                fallback.setEnding();
                return fallback;
        }
    }
    
    /**
     * Check if player is adjacent to this NPC
     */
    public boolean isAdjacentTo(int playerX, int playerY) {
        int dx = Math.abs(playerX - gridX);
        int dy = Math.abs(playerY - gridY);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
    
    // Getters
    public String getName() { return name; }
    public String getRole() { return role; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isGlitched() { return isGlitched; }
}
