package com.alathreon.guesstheduke.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageColoringService {

  public static final Color PRIMARY_COLOR = Color.rgb(237, 28, 36);
  public static final Color SECONDARY_COLOR = Color.rgb(255, 242, 0);

  public Image fillPattern(Image originalImage, Image pattern) {

    int width = (int) originalImage.getWidth();
    int height = (int) originalImage.getHeight();

    int patternWidth = (int) pattern.getWidth();
    int patternHeight = (int) pattern.getHeight();

    WritableImage coloredImage = new WritableImage(width, height);
    PixelReader reader = originalImage.getPixelReader();
    PixelWriter writer = coloredImage.getPixelWriter();
    PixelReader patternReader = pattern.getPixelReader();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Color pixel = reader.getColor(x, y);
        if (PRIMARY_COLOR.equals(pixel)) {
          int subX = x % patternWidth;
          int subY = y % patternHeight;
          pixel = patternReader.getColor(subX, subY);
        }
        writer.setColor(x, y, pixel);
      }
    }

    return coloredImage;
  }

  public Image colorizeImage(Image originalImage, Color primaryColor, Color secondaryColor) {

    int width = (int) originalImage.getWidth();
    int height = (int) originalImage.getHeight();

    WritableImage coloredImage = new WritableImage(width, height);
    PixelReader reader = originalImage.getPixelReader();
    PixelWriter writer = coloredImage.getPixelWriter();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Color pixel = reader.getColor(x, y);
        Color newColor = replaceColor(pixel, primaryColor, secondaryColor);
        writer.setColor(x, y, newColor);
      }
    }

    return coloredImage;
  }

  private Color replaceColor(Color pixel, Color primaryColor, Color secondaryColor) {
    if (pixel.equals(Color.WHITE)) {
      return Color.TRANSPARENT;
    } else if (PRIMARY_COLOR.equals(pixel)) {
      return primaryColor;
    } else if (SECONDARY_COLOR.equals(pixel)) {
      return secondaryColor;
    } else {
      return pixel;
    }
  }
}
