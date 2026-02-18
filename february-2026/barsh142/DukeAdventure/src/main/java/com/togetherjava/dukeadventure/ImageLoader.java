package com.togetherjava.dukeadventure;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {
    private static final Map<String, Image> cache = new HashMap<>();
    private static final int TILE_SIZE = 32;
    private static final int DUKE_SIZE = 64;
    private static final int NPC_SIZE = 32;

    public static Image loadTile(String filename) {
        return loadImage("/images/tiles/" + filename, TILE_SIZE, TILE_SIZE);
    }

    public static Image loadDuke(String filename) {
        return loadImage("/images/dukes/" + filename, DUKE_SIZE, DUKE_SIZE);
    }

    public static Image loadNPC(String filename) {
        return loadImage("/images/npcs/" + filename, NPC_SIZE, NPC_SIZE);
    }

    public static Image loadPlayer(String filename) {
        return loadImage("/images/player/" + filename, NPC_SIZE, NPC_SIZE);
    }

    public static Image loadBoss(String filename) {
        return loadImage("/images/boss/" + filename, 128, 128);
    }

    public static Image loadUI(String filename, int width, int height) {
        return loadImage("/images/ui/" + filename, width, height);
    }

    public static Image loadImage(String path, int width, int height) {
        // Check cache first
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        
        try {
            InputStream stream = ImageLoader.class.getResourceAsStream(path);
            if (stream != null) {
                Image image = new Image(stream, width, height, true, false);
                cache.put(path, image);
                System.out.println("✓ Loaded image: " + path);
                return image;
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to load image: " + path);
        }
        
        // Return null if image not found - we'll handle this in render methods
        System.out.println("⚠ Image not found (using placeholder): " + path);
        return null;
    }
    
    /**
     * Clear the image cache
     */
    public static void clearCache() {
        cache.clear();
    }
    
    /**
     * Check if an image is loaded
     */
    public static boolean hasImage(String path) {
        return cache.containsKey(path);
    }
}
