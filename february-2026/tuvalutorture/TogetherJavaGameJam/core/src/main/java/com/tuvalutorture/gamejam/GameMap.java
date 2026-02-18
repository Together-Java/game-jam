package com.tuvalutorture.gamejam;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class GameMap implements Disposable {
    public static class GameMapTile implements Disposable {
        private Anim animation;
        private String[] attributes;
        private final Texture texture;

        public Coordinate coordinate;

        public GameMapTile(Texture texture) {
            this.texture = texture;
        }

        public GameMapTile clone() {
            GameMapTile map = new GameMapTile(this.texture);
            return map;
        }

        public boolean hasAttribute(String attribute) {
            return attributes != null && Arrays.asList(attributes).contains(attribute);
        }

        public void updateAnimation() {
            if (animation == null) return;
            animation.update(Gdx.graphics.getDeltaTime());
        }

        public Texture getTexture() {
            if (animation == null) return this.texture;
            else return animation.returnCurrentFrame();
        }

        public void dispose() {
            texture.dispose();
            animation.dispose();
        }

        public GameMapTile(Texture texture, Anim animation, String[] attributes, Coordinate coord) {
            this.texture = texture;
            this.animation = animation;
            this.attributes = attributes;
            this.coordinate = coord;
        }
    }

    public static class AnimationOverride {
        private final Anim oldAnim;
        private final GameMapTile overridden;

        public AnimationOverride(GameMapTile overridden, Anim newAnim) {
            this.overridden = overridden;
            this.oldAnim = overridden.animation;
            this.overridden.animation = newAnim;
        }

        public void setOldAnim() {
            this.overridden.animation = oldAnim;
        }
    }

    public static final class Coordinate {
        private final int x;
        private final int y;
        public Coordinate(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public boolean equals(Coordinate coord) { return x == coord.x && y == coord.y; }
    }

    private static HashMap<String, Texture> tilesLoaded = new HashMap<>(); // this is static to use across all maps
    private static HashMap<String, Texture[]> animationsLoaded = new HashMap<>();
    public ArrayList<AnimationOverride> overrides;
    private static final byte UNIT_SIZE = 16;
    private static final byte MAX_WIDTH = 40;
    private static final byte MAX_HEIGHT = 30;
    public int mapWidth, mapHeight;
    private int tileWidth, tileHeight;
    private int tileCount;
    private int propLayer;
    private int layerCount;
    private GameMapTile[][] layers;

    public static GameMap loadFromFile(String file) {
        GameMap returnedGameMap = new GameMap();
        byte[] data;
        InputStream stream = GameState.loadAsset(file).read();
        try {
            data = stream.readAllBytes();
            stream.close();
        } catch (IOException e) {
            return null;
        }

        String str = new String(data);
        if (!str.startsWith("tmap")) return null;

        returnedGameMap.overrides = new ArrayList<AnimationOverride>();

        int markerIndex = str.indexOf("markers"); // attributes
        int layerIndex = str.indexOf("tuvalutorture"); // layers
        int animatedIndex = str.indexOf("animated"); // animation paths & info

        int index = 6; // 6 to account for the string header
        returnedGameMap.tileCount = BytePacker.bytesToInt(data, index); index += 4;
        returnedGameMap.layerCount = BytePacker.bytesToInt(data, index); index += 4;
        returnedGameMap.tileWidth = BytePacker.bytesToInt(data, index); index += 4;
        returnedGameMap.tileHeight = BytePacker.bytesToInt(data, index); index += 4;
        returnedGameMap.mapWidth = BytePacker.bytesToInt(data, index); index += 4;
        returnedGameMap.mapHeight = BytePacker.bytesToInt(data, index); index += 4;
        returnedGameMap.propLayer = BytePacker.bytesToInt(data, index); index += 5; // 5 because newline :)

        int dest = 0;
        if (markerIndex != -1) {
            dest = markerIndex - 1;
        } else if (animatedIndex != -1) {
            dest = animatedIndex - 1;
        } else {
            dest = layerIndex - 1;
        }

        String[] paths = new String(Arrays.copyOfRange(data, index, dest)).split("\n");
        for (String path : paths) {
            System.out.println(path);
        }
        GameMapTile[] tiles = new GameMapTile[paths.length];

        for (int i = 0; i < paths.length; i++) {
            Texture tex = null;
            if (GameMap.tilesLoaded.containsKey(paths[i])) tex = GameMap.tilesLoaded.get(paths[i]);
            else { tex = new Texture(GameState.loadAsset(paths[i])); GameMap.tilesLoaded.put(paths[i], tex); }

            tiles[i] = new GameMapTile(tex);
        }

        if (animatedIndex != -1) {
            dest = animatedIndex - 1;
        } else {
            dest = layerIndex - 1;
        }

        if (markerIndex != -1) {
            index = markerIndex + "markers\n".length();
            String[] attributeStrings = new String(Arrays.copyOfRange(data, index, dest)).split("\n");
            for (int i = 0; i < attributeStrings.length; i++) {
                String[] attributes = attributeStrings[i].split(",");
                int attributedTile = Integer.parseInt(attributes[0]);
                tiles[attributedTile].attributes = Arrays.copyOfRange(attributes, 1, attributes.length);
            }
        }

        if (animatedIndex != -1) {
            index = animatedIndex + "animated\n".length();
            String[] animStrings = new String(Arrays.copyOfRange(data, index, layerIndex)).split("\n");
            for (int i = 0; i < animStrings.length; i++) {
                String[] animAttributes = animStrings[i].split(",");
                int animatedTile = Integer.parseInt(animAttributes[0]);
                Texture[] animation;
                if (animationsLoaded.containsKey(animAttributes[1])) {
                    animation = animationsLoaded.get(animAttributes[1]);
                } else {
                    animation = Anim.loadAnimFromFolder(animAttributes[1], returnedGameMap.tileWidth, returnedGameMap.tileHeight, Integer.parseInt(animAttributes[3]), ((float)Integer.parseInt(animAttributes[2]) / 1000.0f)).frames;
                    animationsLoaded.put(animAttributes[1], animation);
                }
                tiles[animatedTile].animation = new Anim(animation, returnedGameMap.tileWidth, returnedGameMap.tileHeight, ((float)Integer.parseInt(animAttributes[2]) / 1000.0f));
            }
        }

        index = layerIndex + "tuvalutorture\n".length();
        GameMapTile[][] layerData = new GameMapTile[returnedGameMap.layerCount][returnedGameMap.mapWidth * returnedGameMap.mapHeight];
        for (int i = 0; i < layerData.length; i++) {
            for (int j = 0; j < layerData[i].length; j++) {
                int tileIndex = BytePacker.bytesToInt(data, index);
                if (tileIndex < 0 || tileIndex >= layerData[i].length) layerData[i][j] = null;
                else {
                    GameMapTile tile = tiles[BytePacker.bytesToInt(data, index)];
                    Coordinate location = new Coordinate(j % returnedGameMap.mapWidth, j / returnedGameMap.mapWidth);
                    layerData[i][j] = new GameMapTile(tile.texture, (tile.animation != null ? new Anim(tile.animation.frames, returnedGameMap.tileWidth, returnedGameMap.tileHeight, tile.animation.frameTime) : null), tile.attributes, location);
                }
                index += 4;
            }
            index += 1; // 1 for new line ofc
        }

        returnedGameMap.layers = layerData;
        return returnedGameMap;
    }

    public void updateAnimations() {
        for (GameMapTile[] layer : layers) {
            for (GameMapTile tile : layer) {
                if (tile != null) tile.updateAnimation();
            }
        }
    }

    public GameMapTile findTileAt(int layer, int x, int y) {
        int index = x + (y * mapWidth);
        if (index >= layers[layer].length - 1) return null;
        return layers[layer][x + (y * mapWidth)];
    }

    public GameMapTile findTileAt(int layer, Coordinate coordinate) {
        return findTileAt(layer, coordinate.x, coordinate.y);
    }

    public static short resolvePixelToCoordinate(int pixel) {
        if (pixel == 0) return 0;
        return (short)(pixel / 16);
    }

    public static Coordinate resolvePixelsToCoordinates(int x, int y) {
        return new Coordinate(resolvePixelToCoordinate(x), resolvePixelToCoordinate(y));
    }

    public static short resolveCoordinateToPixel(int coordinate) {
        return (short) (coordinate * 16);
    }

    public static Coordinate resolveCoordinatesToPixels(int x, int y) {
        return new Coordinate(resolveCoordinateToPixel(x), resolveCoordinateToPixel(y));
    }

    public int getLayerCount() {
        return layerCount;
    }

    public GameMapTile[] findAllTilesWithAttribute(String attribute) {
        ArrayList<GameMapTile> tiles = new ArrayList<>();
        for (GameMapTile[] layer : layers) {
            for (GameMapTile tile : layer) {
                if (tile == null) continue;
                if (tile.hasAttribute(attribute)) tiles.add(tile);
            }
        }
        return tiles.toArray(new GameMapTile[0]);
    }

    public void renderLayer(SpriteBatch batch, int layer) {
        for (int index = 0; index < layers[layer].length; index++) {
            GameMapTile tile = layers[layer][index];

            if (tile == null) continue;

            int tileX = index % mapWidth;
            int tileY = index / mapWidth;

            batch.draw(tile.getTexture(), (tileX * tileWidth), (tileY * tileHeight));
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < layers.length; i++) {
            renderLayer(batch, i);
        }
    }

    public void dispose() {
        for (GameMapTile[] layer : layers) {
            for  (GameMapTile tile : layer) {
                tile.dispose();
            }
        }
    }
}
