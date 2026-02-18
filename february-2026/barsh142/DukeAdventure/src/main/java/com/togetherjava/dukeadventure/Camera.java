package com.togetherjava.dukeadventure;

/**
 * Camera system for scrolling world view.
 * Player stays centered, world scrolls around them
 */
public class Camera {
    private double x, y;           // Camera position in world coordinates
    private int viewportWidth;     // Viewport width in pixels
    private int viewportHeight;    // Viewport height in pixels
    private int tileSize;          // Size of each tile
    
    private int worldWidth;        // World width in tiles
    private int worldHeight;       // World height in tiles
    
    public Camera(int viewportWidth, int viewportHeight, int tileSize) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.tileSize = tileSize;
        this.x = 0;
        this.y = 0;
    }
    
    /**
     * Set world bounds
     */
    public void setWorldBounds(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
    
    /**
     * Center camera on player
     */
    public void centerOn(int playerGridX, int playerGridY) {
        // Calculate desired camera position (center player on screen)
        double targetX = (playerGridX * tileSize) - (viewportWidth / 2.0) + (tileSize / 2.0);
        double targetY = (playerGridY * tileSize) - (viewportHeight / 2.0) + (tileSize / 2.0);
        
        // Clamp camera to world bounds
        double minX = 0;
        double maxX = (worldWidth * tileSize) - viewportWidth;
        double minY = 0;
        double maxY = (worldHeight * tileSize) - viewportHeight;
        
        x = Math.max(minX, Math.min(targetX, maxX));
        y = Math.max(minY, Math.min(targetY, maxY));
    }
    
    /**
     * Get camera X offset
     */
    public double getX() {
        return x;
    }
    
    /**
     * Get camera Y offset
     */
    public double getY() {
        return y;
    }
    
    /**
     * Convert world coordinates to screen coordinates
     */
    public int worldToScreenX(int worldX) {
        return (int)(worldX - x);
    }
    
    /**
     * Convert world coordinates to screen coordinates
     */
    public int worldToScreenY(int worldY) {
        return (int)(worldY - y);
    }
    
    /**
     * Check if a tile is visible on screen
     */
    public boolean isTileVisible(int tileX, int tileY) {
        int worldX = tileX * tileSize;
        int worldY = tileY * tileSize;
        
        int screenX = worldToScreenX(worldX);
        int screenY = worldToScreenY(worldY);
        
        return screenX + tileSize >= 0 && screenX < viewportWidth &&
               screenY + tileSize >= 0 && screenY < viewportHeight;
    }
}
