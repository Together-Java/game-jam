package com.solutiongameofficial.game;

import com.solutiongameofficial.graphics.parser.ChromaticAberrationParser;
import com.solutiongameofficial.graphics.parser.Parser;

import java.awt.image.BufferedImage;

public record PostProcessor(boolean enabled) {

    public BufferedImage apply(BufferedImage image) {
        if (!enabled) {
            return image;
        }

        Parser parser = new ChromaticAberrationParser(1, 2);
        return parser.parse(image);
    }
}