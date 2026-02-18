package com.alathreon.guesstheduke;

import com.alathreon.guesstheduke.controller.GameController;
import com.alathreon.guesstheduke.controller.MainMenuController;
import com.alathreon.guesstheduke.controller.View;
import com.alathreon.guesstheduke.util.IOUtil;
import java.io.IOException;
import javafx.application.Application;
import javafx.stage.Stage;

public class GuessTheDukeApp extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    View<MainMenuController> mainMenu = IOUtil.loadView("/MainMenu.fxml", 1200, 800);
    View<GameController> gameView = IOUtil.loadView("/Game.fxml", 1200, 1000);

    mainMenu
        .controller()
        .setOnStartAction(
            () -> {
              stage.setScene(gameView.scene());
              gameView.controller().startNewGame();
            });

    stage.setTitle("GuessResult The Duke");
    stage.setScene(mainMenu.scene());
    stage.show();
  }
}
