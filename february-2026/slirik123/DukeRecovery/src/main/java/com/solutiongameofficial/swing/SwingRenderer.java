package com.solutiongameofficial.swing;

import com.solutiongameofficial.graphics.Renderer;

import java.awt.image.BufferedImage;

public final class SwingRenderer implements Renderer {

    private final Frame frame;

    public SwingRenderer(Frame frame) {
        this.frame = frame;
    }

    @Override
    public void present(BufferedImage image) {
        frame.setGraphicScaleFunction((graphics2D, scaleX, scaleY) ->
                graphics2D.drawImage(image,
                        0,
                        0,
                        (int) (Frame.WIDTH * scaleX),
                        (int) (Frame.HEIGHT * scaleY),
                        null
                )
        );
        frame.repaint();
    }
}
