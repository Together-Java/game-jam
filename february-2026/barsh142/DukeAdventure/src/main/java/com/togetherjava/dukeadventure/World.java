package com.togetherjava.dukeadventure;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class World {
    public enum TileType {
        GRASS,      // Walkable
        WALL,       // Blocked
        TALL_GRASS, // Walkable + wild encounters
        WATER,      // Decorative, blocked
        PATH        // Walkable, no encounters
    }
    
    private int width, height;
    private TileType[][] tiles;
    private List<NPC> npcs;
    
    // Tile images
    private Image grassTile;
    private Image wallTile;
    private Image tallGrassTile;
    private Image waterTile;
    private Image pathTile;
    
    // Boss location
    private int bossX = 45;
    private int bossY = 5;
    private boolean bossDefeated = false;
    
    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TileType[width][height];
        this.npcs = new ArrayList<>();
        
        loadTileImages();
        generateMap();
        createNPCs();
    }

    private void loadTileImages() {
        grassTile = ImageLoader.loadTile("grass.png");
        wallTile = ImageLoader.loadTile("wall.png");
        tallGrassTile = ImageLoader.loadTile("tall_grass.png");
        waterTile = ImageLoader.loadTile("water.png");
        pathTile = ImageLoader.loadTile("path.png");
    }

    private void generateMap() {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = TileType.GRASS;
            }
        }

        for (int x = 0; x < width; x++) {
            tiles[x][0] = TileType.WALL;
            tiles[x][height - 1] = TileType.WALL;
        }
        for (int y = 0; y < height; y++) {
            tiles[0][y] = TileType.WALL;
            tiles[width - 1][y] = TileType.WALL;
        }

        for (int i = 0; i < 30; i++) {
            int x = 3 + (int)(Math.random() * (width - 6));
            int y = 3 + (int)(Math.random() * (height - 6));
            createGrassPatch(x, y);
        }

        for (int i = 0; i < 25; i++) {
            int x = 2 + (int)(Math.random() * (width - 4));
            int y = 2 + (int)(Math.random() * (height - 4));
            if (tiles[x][y] == TileType.GRASS) {
                tiles[x][y] = TileType.WALL;
            }
        }
        

        for (int i = 0; i < 15; i++) {
            int x = 2 + (int)(Math.random() * (width - 4));
            int y = 2 + (int)(Math.random() * (height - 4));
            if (tiles[x][y] == TileType.GRASS) {
                tiles[x][y] = TileType.WATER;
            }
        }

        for (int x = 43; x < 48; x++) {
            for (int y = 2; y < 8; y++) {
                tiles[x][y] = TileType.PATH;
            }
        }

        for (int x = 23; x < 28; x++) {
            for (int y = 23; y < 28; y++) {
                tiles[x][y] = TileType.GRASS;
            }
        }
    }
    

    private void createGrassPatch(int centerX, int centerY) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
                    if (tiles[x][y] == TileType.GRASS) {
                        tiles[x][y] = TileType.TALL_GRASS;
                    }
                }
            }
        }
    }

    private void createNPCs() {
        npcs.add(new NPC("Zabuzard", "Top Helper", 25, 20));          // Near center
        npcs.add(new NPC("Wazei", "Admin", 30, 15));
        npcs.add(new NPC("Marko", "Admin", 20, 30));
        npcs.add(new NPC("Adi", "Moderator", 35, 20));
        npcs.add(new NPC("SoLuckySeven", "Smart Member", 15, 25));
        npcs.add(new NPC("FirasG", "Legend Contributor", 25, 35));
        npcs.add(new NPC("Alathreon", "Moderator", 40, 25));
        npcs.add(new NPC("SquidxTV", "Moderator", 20, 40));
        npcs.add(new NPC("Christolis", "Git Expert", 35, 30));
        npcs.add(new NPC("barshERROR(404)", "Developer", 15, 20));     // Player's doppelganger!
        npcs.add(new NPC("Number1Engineer", "Great Engineer", 30, 25));
        npcs.add(new NPC("TJ-Bot", "Server Bot", 25, 25));            // Center shopkeeper
        npcs.add(new NPC("AlphaBee", "Community Ambassador", 35, 35));

        npcs.add(NPC.createGlitchedDuke(3, 3));
    }
    

    public void render(GraphicsContext gc, int tileSize, Camera camera) {
        // Only render visible tiles (optimization!)
        int startX = Math.max(0, (int)(camera.getX() / tileSize) - 1);
        int endX = Math.min(width, (int)((camera.getX() + 800) / tileSize) + 1);
        int startY = Math.max(0, (int)(camera.getY() / tileSize) - 1);
        int endY = Math.min(height, (int)((camera.getY() + 576) / tileSize) + 1);
        
        // Draw visible tiles
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int worldX = x * tileSize;
                int worldY = y * tileSize;
                
                int screenX = camera.worldToScreenX(worldX);
                int screenY = camera.worldToScreenY(worldY);
                
                TileType tile = tiles[x][y];
                Image tileImage = getTileImage(tile);
                
                if (tileImage != null) {
                    gc.drawImage(tileImage, screenX, screenY, tileSize, tileSize);
                } else {
                    // Fallback: colored squares
                    Color color = getTileFallbackColor(tile);
                    gc.setFill(color);
                    gc.fillRect(screenX, screenY, tileSize, tileSize);
                }
            }
        }
        
        // Draw boss location marker (if not defeated)
        if (!bossDefeated) {
            int worldX = bossX * tileSize;
            int worldY = bossY * tileSize;
            int screenX = camera.worldToScreenX(worldX);
            int screenY = camera.worldToScreenY(worldY);
            
            gc.setFill(Color.RED);
            gc.fillText("⚠ BOSS", screenX, screenY - 5);
        }
        
        // Draw NPCs (they handle their own visibility check)
        for (NPC npc : npcs) {
            npc.render(gc, tileSize, camera);
        }
    }

    private Image getTileImage(TileType type) {
        switch (type) {
            case GRASS: return grassTile;
            case WALL: return wallTile;
            case TALL_GRASS: return tallGrassTile;
            case WATER: return waterTile;
            case PATH: return pathTile;
            default: return grassTile;
        }
    }

    private Color getTileFallbackColor(TileType type) {
        switch (type) {
            case GRASS: return Color.LIGHTGREEN;
            case WALL: return Color.DARKGRAY;
            case TALL_GRASS: return Color.DARKGREEN;
            case WATER: return Color.LIGHTBLUE;
            case PATH: return Color.SANDYBROWN;
            default: return Color.LIGHTGREEN;
        }
    }

    public boolean canMoveTo(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        
        TileType tile = tiles[x][y];
        if (tile == TileType.WALL || tile == TileType.WATER) return false;
        
        // Check if NPC is blocking
        for (NPC npc : npcs) {
            if (npc.getGridX() == x && npc.getGridY() == y) {
                return false;
            }
        }
        
        return true;
    }

    public boolean checkEncounter(int x, int y) {
        if (tiles[x][y] == TileType.TALL_GRASS) {
            return Math.random() < 0.01; // 1% chance (reduced from 3%)
        }
        return false;
    }

    public boolean isBossLocation(int x, int y) {
        return x == bossX && y == bossY && !bossDefeated;
    }
    

    public void defeatBoss() {
        bossDefeated = true;
    }

    public NPC getNPCAt(int x, int y) {
        for (NPC npc : npcs) {
            if (npc.getGridX() == x && npc.getGridY() == y) {
                return npc;
            }
        }
        return null;
    }

    public NPC getAdjacentNPC(int playerX, int playerY) {
        for (NPC npc : npcs) {
            if (npc.isAdjacentTo(playerX, playerY)) {
                return npc;
            }
        }
        return null;
    }
    
    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<NPC> getNPCs() { return npcs; }
}
