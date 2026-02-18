package com.solutiongameofficial.swing;

import java.awt.*;

/**
 * Used to provide scalable graphics as lambda functions
 */
@FunctionalInterface
public interface GraphicScaleFunction {

    /**
     * To scale your graphics, use 'scaleX' and 'scaleY' for your graphics to make them resizable.
     * For example:
     * <pre>
     *     JPanel panel = new JPanel().applyGraphics((graphics2D, scaleX, scaleY) -> {
     *             graphics2D.setColor(Color.CYAN);
     *             graphics2D.fillRect(10, 10, (int) (100 * scaleX), (int) (100 * scaleY));
     *         });
     * </pre>
     */
    void apply(Graphics2D graphics2D, double scaleX, double scaleY);
}