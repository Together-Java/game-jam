package com.alathreon.guesstheduke.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MainMenuController {

  private Runnable onStartAction;

  @FXML
  private void onStart(ActionEvent event) {
    this.onStartAction.run();
  }

  public void setOnStartAction(Runnable onStartAction) {
    this.onStartAction = onStartAction;
  }
}
