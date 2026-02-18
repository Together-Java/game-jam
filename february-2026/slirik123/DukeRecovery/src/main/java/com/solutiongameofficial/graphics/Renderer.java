package com.solutiongameofficial.graphics;

import java.awt.image.BufferedImage;

public interface Renderer {
    void present(BufferedImage image);
    default void close() {}
}
