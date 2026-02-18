package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class GameState {
    public static Item[] checklist;
    public static Item[] acquired;
    public static int currentFloor = 0;
    public static Room currentRoom;
    public static Player player = new Player();
    public static Creature[] enemies;

    private static String stripRelativity(String path) {
        if (!path.startsWith("./") && !path.startsWith("/") && !path.startsWith(".")) return path;
        else if (path.startsWith("./")) return path.substring(2);
        else if (path.startsWith("/") || path.startsWith(".")) return path.substring(1);
        else return path;
    }

    public static FileHandle loadAsset(String path) {
        if (path == null) return null;
        return Gdx.files.internal(stripRelativity(path));
    }

    public static void dispose() {
        player.dispose();
    }
}
