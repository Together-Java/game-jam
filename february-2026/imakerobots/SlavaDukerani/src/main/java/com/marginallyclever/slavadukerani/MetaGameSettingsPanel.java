package com.marginallyclever.slavadukerani;

import javax.swing.*;
import java.awt.*;

/// allows user to set the game width, height, number of mines, and seed.
public class MetaGameSettingsPanel extends JPanel {
    private final JFormattedTextField widthField;
    private final JFormattedTextField heightField;
    private final JFormattedTextField minesField;
    private final JFormattedTextField seedField;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MetaGameSettingsPanel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MetaGameSettingsPanel(20,10,30,1234));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public MetaGameSettingsPanel(int width,int height,int mines,int seed) {
        super(new GridLayout(0, 2,5,5));

        minesField = PanelHelper.addNumberFieldInt("Number of mines", mines);
        widthField = PanelHelper.addNumberFieldInt("Width", width);
        seedField = PanelHelper.addNumberFieldInt("Map Seed", seed);
        heightField = PanelHelper.addNumberFieldInt("Height", height);

        add(new JLabel("Width"));
        add(widthField);
        add(new JLabel("Height"));
        add(heightField);
        add(new JLabel("Mines"));
        add(minesField);
        add(new JLabel("Seed"));
        add(seedField);
    }

    // NOTE: do NOT override Component.getWidth()/getHeight().
    // Use explicit names for the board dimensions so the panel behaves like a normal Swing component.
    public int getBoardWidth() {
        return ((Number)widthField.getValue()).intValue();
    }

    public int getBoardHeight() {
        return ((Number)heightField.getValue()).intValue();
    }

    public int getMines() {
        return ((Number)minesField.getValue()).intValue();
    }

    public int getSeed() {
        return ((Number)seedField.getValue()).intValue();
    }

    public int newSeed() {
        int s = (int)(Math.random()*1000000);
        seedField.setValue(s);
        return s;
    }
}
