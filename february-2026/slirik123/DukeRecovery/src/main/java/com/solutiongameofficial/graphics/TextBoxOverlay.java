package com.solutiongameofficial.graphics;

import com.solutiongameofficial.game.Dialogue;
import com.solutiongameofficial.io.ResourceLoader;

import java.awt.*;
import java.util.List;

public final class TextBoxOverlay {

    private static final int PADDING_X = 50;
    private static final int PADDING_Y = 70;
    private static final int LINE_HEIGHT = 30;
    private static final int OUTSIDE_LINES_OFFSET_Y = 200;

    private final Image textBoxAscii = ResourceLoader.loadImage("ascii/ui/Textbox.png");
    private final Font font = new Font("Monospaced", Font.PLAIN, 22);

    public void draw(Graphics2D graphics2D, int boxX, int boxY, Dialogue dialogue) {
        graphics2D.drawImage(textBoxAscii, boxX, boxY, (int) (textBoxAscii.getWidth(null) * 1.2), textBoxAscii.getHeight(null), null);

        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        graphics2D.setFont(font);
        graphics2D.setColor(Color.WHITE);

        drawTextBoxContent(graphics2D, boxX, boxY, dialogue);
        drawOutsideTextBoxContent(graphics2D, boxX, boxY, dialogue);
    }

    private void drawTextBoxContent(Graphics2D graphics2D, int boxX, int boxY, Dialogue dialogue){
        List<String> lines = dialogue.getVisibleMainLines(3);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            int x = boxX + PADDING_X;
            int y = boxY + PADDING_Y + (index * LINE_HEIGHT);
            graphics2D.drawString(line, x, y);
        }
    }

    private void drawOutsideTextBoxContent(Graphics2D graphics2D, int boxX, int boxY, Dialogue dialogue) {
        List<String> outside = dialogue.getVisibleOutsideLines(2);
        for (int index = 0; index < outside.size(); index++) {
            String line = outside.get(index);
            int x = boxX + PADDING_X;
            int y = boxY - OUTSIDE_LINES_OFFSET_Y - ((outside.size() - 1 - index) * LINE_HEIGHT);
            graphics2D.drawString(line, x, y);
        }
    }
}