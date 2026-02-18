package com.alathreon.guesstheduke.util;

import com.alathreon.guesstheduke.model.Duke;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class DukeImagesManager {

  private Image baseDukeImage;
  private final Map<DukeKey, Image> imageCache = new HashMap<>();

  public Image find(Duke duke) {
    DukeKey key = DukeKey.create(duke);
    if (imageCache.containsKey(key)) {
      return imageCache.get(key);
    }

    if (baseDukeImage == null) {
      baseDukeImage = IOUtil.loadImage("/duke/Duke.png");
    }

    ImageColoringService imageColoringService = new ImageColoringService();

    Image bottomImage = IOUtil.loadImage(duke.bottom());
    Image bottomPatternImage = IOUtil.loadImage(duke.bottomPattern());

    Image colorizedBottomPattern =
        imageColoringService.colorizeImage(
            bottomPatternImage,
            duke.colorScheme().primaryColor(),
            duke.colorScheme().secondaryColor());

    Image colorizedBottom =
        imageColoringService.colorizeImage(
            bottomImage, ImageColoringService.PRIMARY_COLOR, ImageColoringService.SECONDARY_COLOR);

    Image paternizedBottom =
        imageColoringService.fillPattern(colorizedBottom, colorizedBottomPattern);

    Image armsImage = IOUtil.loadImage(duke.arms());

    Image colorizedArms =
        imageColoringService.colorizeImage(
            armsImage, duke.colorScheme().primaryColor(), duke.colorScheme().secondaryColor());

    Image hatImage = IOUtil.loadImage(duke.hat());

    Image colorizedHat =
        imageColoringService.colorizeImage(
            hatImage, duke.colorScheme().primaryColor(), duke.colorScheme().secondaryColor());

    Image combined = overlay(baseDukeImage, paternizedBottom);
    combined = overlay(combined, colorizedArms);
    combined = overlay(combined, colorizedHat);

    imageCache.put(key, combined);

    return combined;
  }

  private Image overlay(Image base, Image top) {
    int width = (int) base.getWidth();
    int height = (int) base.getHeight();
    WritableImage combined = new WritableImage(width, height);
    PixelWriter writer = combined.getPixelWriter();
    PixelReader baseReader = base.getPixelReader();
    PixelReader topReader = top.getPixelReader();

    // copy the base image ...
    IntStream.range(0, height)
        .forEach(
            y ->
                IntStream.range(0, width)
                    .forEach(x -> writer.setColor(x, y, baseReader.getColor(x, y))));

    // Overlay top image
    int topWidth = (int) top.getWidth();
    int topHeight = (int) top.getHeight();

    IntStream.range(0, Math.min(topHeight, height))
        .forEach(
            y ->
                IntStream.range(0, Math.min(topWidth, width))
                    .forEach(
                        x -> {
                          Color pixel = topReader.getColor(x, y);
                          if (pixel.getOpacity() > 0.05) {
                            writer.setColor(x, y, pixel);
                          }
                        }));

    return combined;
  }
}
