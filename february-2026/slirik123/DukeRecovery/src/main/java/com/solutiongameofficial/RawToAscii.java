package com.solutiongameofficial;

import com.solutiongameofficial.graphics.AsciiColorMode;
import com.solutiongameofficial.graphics.parser.Parser;
import com.solutiongameofficial.graphics.parser.ImageToAsciiParser;
import com.solutiongameofficial.io.ResourceLoader;

import java.awt.image.BufferedImage;
import java.util.List;

public class RawToAscii {

    public static void main(String[] args) {
        ImageToAsciiParser parser = new ImageToAsciiParser();

        List<BufferedImage> rawImages = ResourceLoader.loadAllImagesFromRaw().stream()
                .map(image -> parser.parse(image, 4, AsciiColorMode.MONOCHROME))
                .toList();

        ResourceLoader.saveAllImagesToAsciiFolder(rawImages);
    }
}
