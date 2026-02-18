package com.tuvalutorture.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.HashMap;

public class FontRenderer implements Disposable {
    private static class GlyphResolver {
        private enum GlyphType {
            LOWERCASE("lower"),
            UPPERCASE("upper"),
            DIGIT("digit"),
            SYMBOL("symbol");

            private final String type;
            private SymbolNames[] symbols;

            GlyphType(String type) {
                this.type = type;
            }

            public static GlyphType resolve(char c) {
                if (c > 128) return null;
                else if (c >= 65 && c <= 90) return GlyphType.UPPERCASE;
                else if (c >= 97 && c <= 122) return GlyphType.LOWERCASE;
                else if (c >= 48 && c <= 57) return GlyphType.DIGIT;
                else return GlyphType.SYMBOL;
            }
        }

        private static final String fileType = ".png";
        private static Texture loadGlyph(String folder, char c) {
            StringBuilder builder = new StringBuilder();
            GlyphType type = GlyphType.resolve(c);
            if (type == null) return null;
            builder.append(folder);
            builder.append('/');
            builder.append(type.type);
            builder.append('_');
            String symbolName = SymbolNames.getSymbolName(c);
            if (symbolName == null && type == GlyphType.SYMBOL) return null;
            builder.append(type == GlyphType.SYMBOL ? symbolName : Character.toString(c));
            builder.append(fileType);
            return new Texture(GameState.loadAsset(builder.toString()));
        }
    }

    public static class Message extends Thread {
        private int renderX, renderY, scale;
        private SpriteBatch batch;
        private String msg;
        private FontRenderer fontRenderer;

        public Message(String msg, SpriteBatch batch, int renderX, int renderY, int scale) {
            this.msg = msg;
            this.batch = batch;
            this.renderX = renderX;
            this.renderY = renderY;
            this.scale = scale;
        }
    }

    private int glyphWidth, glyphHeight, glyphGap;

    private ArrayList<Message> queuedMessages = new ArrayList<>();
    private int currentMsg = 0;

    public HashMap<Character, Texture> glyphs = new HashMap<>();

    public FontRenderer(String folder, int height, int width, int gap) {
        for (char i = 0; i < 128; i++) { // DATA, BABY
            glyphs.put(Character.valueOf(i), GlyphResolver.loadGlyph(folder, i));
        }
        glyphWidth = width;
        glyphHeight = height;
        glyphGap = gap;
    }

    public void printGlyph(char c, SpriteBatch batch, int x, int y, int scale) {
        Texture texture = glyphs.get(c);
        if (texture == null) return;
        batch.draw(texture, x, y, glyphWidth * scale, glyphHeight * scale);
    }

    public void render(String str, SpriteBatch batch, int x, int y, int scale) {
        int currentX = x, currentY = y;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            currentX += ((glyphWidth - glyphGap) * scale);
            if (c == '\n' || currentX > Gdx.graphics.getWidth()) {
                currentX = x;
                currentY -= (glyphHeight * scale);
            }
            printGlyph(c, batch, currentX, currentY, scale);
        }
    }

    public void render(String str, SpriteBatch batch, int x, int y) {
        this.render(str, batch, x, y, 1);
    }

    public void render(Message msg) {
        this.render(msg.msg, msg.batch, msg.renderX, msg.renderY, msg.scale);
    }

    public void addMessage(Message msg) {
        queuedMessages.add(msg);
    }

    public void removeMessage(int index) {
        queuedMessages.remove(index);
    }

    public void advanceMessage() {
        currentMsg += 1;
    }

    public int calculateX(String str, int scale) {
        String[] strings = str.split("\n");
        int longestSize = 0;
        for (String s : strings) {
            if (s.length() > longestSize) {
                longestSize = s.length();
            }
        }
        return (longestSize * glyphWidth * scale) - (glyphGap * scale);
    }

    public int calculateY(String str, int scale) {
        String[] strings = str.split("\n");
        int longestSize = 0;
        for (String s : strings) {
            if (s.length() > longestSize) {
                longestSize = s.length();
            }
        }
        return (longestSize * glyphHeight * scale);
    }

    public void renderCurrent() {
        render(queuedMessages.get(currentMsg));
    }

    public void dispose() {
        for (Texture texture : glyphs.values()) {
            if (texture != null) texture.dispose();
        }
    }
}
