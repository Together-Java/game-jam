package com.togetherjava.dukeadventure;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Pokemon-style dialog box - SIMPLE and WORKS!
 */
public class DialogBox {
    private boolean isOpen;
    private String npcName;
    private String[] dialogLines;
    private int currentLine;
    private String questId;
    
    public DialogBox() {
        this.isOpen = false;
        this.currentLine = 0;
    }
    
    public void show(String npcName, String[] dialogLines, String questId) {
        this.isOpen = true;
        this.npcName = npcName;
        this.dialogLines = dialogLines;
        this.currentLine = 0;
        this.questId = questId;
    }
    
    public void advance() {
        currentLine++;
    }
    
    public boolean hasMoreText() {
        return currentLine < dialogLines.length - 1;
    }
    
    public String getQuestId() {
        return questId;
    }
    
    public void close() {
        isOpen = false;
        npcName = null;
        dialogLines = null;
        currentLine = 0;
        questId = null;
    }
    
    public void render(GraphicsContext gc, int windowWidth, int windowHeight) {
        if (!isOpen || dialogLines == null) return;
        
        // Dialog box at bottom (Pokemon style!)
        int boxX = 20;
        int boxY = windowHeight - 150;
        int boxWidth = windowWidth - 40;
        int boxHeight = 130;
        
        // Box background (white with black border)
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        // NPC name
        gc.setFill(Color.DARKBLUE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.fillText(npcName + ":", boxX + 15, boxY + 30);
        
        // Dialog text
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        
        String text = dialogLines[currentLine];
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int y = boxY + 60;
        int maxWidth = boxWidth - 30;
        
        for (String word : words) {
            String testLine = line + word + " ";
            if (gc.getFont().getSize() * testLine.length() / 2 > maxWidth && line.length() > 0) {
                gc.fillText(line.toString(), boxX + 15, y);
                line = new StringBuilder(word + " ");
                y += 20;
            } else {
                line.append(word).append(" ");
            }
        }
        if (line.length() > 0) {
            gc.fillText(line.toString(), boxX + 15, y);
        }
        
        // Indicator
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        if (hasMoreText()) {
            gc.fillText("Press E to continue...", boxX + 15, boxY + boxHeight - 10);
        } else {
            gc.fillText("Press E to close", boxX + 15, boxY + boxHeight - 10);
        }
    }
}
