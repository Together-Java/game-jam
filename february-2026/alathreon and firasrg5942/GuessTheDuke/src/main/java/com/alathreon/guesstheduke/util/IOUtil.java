package com.alathreon.guesstheduke.util;

import com.alathreon.guesstheduke.controller.View;
import com.alathreon.guesstheduke.model.DukeAttribute;
import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

public class IOUtil {
  private IOUtil() {}

  public static <T> View<T> loadView(String path, int width, int height) throws IOException {
    FXMLLoader loader = new FXMLLoader();
    Parent root = loader.load(IOUtil.class.getResourceAsStream(path));
    Scene scene = new Scene(root, width, height);
    T controller = loader.getController();
    return new View<>(scene, controller);
  }

  public static Image loadImage(DukeAttribute attribute) {
    return loadImage(attribute.imagePath());
  }

  public static Image loadImage(String path) {
    try (InputStream image = IOUtil.class.getResourceAsStream(path)) {

      if (image == null) {
        throw new IllegalStateException("Invalid resource path: " + path);
      }
      return new Image(image);
    } catch (IOException ex) {
      throw new IllegalStateException("Invalid resource path: " + path, ex);
    }
  }
}
