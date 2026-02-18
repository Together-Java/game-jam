package com.alathreon.guesstheduke.controller;

import com.alathreon.guesstheduke.logic.GameManager;
import com.alathreon.guesstheduke.logic.GuessResult;
import com.alathreon.guesstheduke.logic.GuessResultKind;
import com.alathreon.guesstheduke.model.AiQuestion;
import com.alathreon.guesstheduke.model.ColorScheme;
import com.alathreon.guesstheduke.model.Duke;
import com.alathreon.guesstheduke.model.DukeAttribute;
import com.alathreon.guesstheduke.util.DukeImagesManager;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;

public class GameController {

  private static final Color BORDER_COLOR = Color.GRAY;
  private static final Color SELECTED_BORDER_COLOR = Color.LIMEGREEN;
  private static final Color CROSS_COLOR = Color.ORANGERED;

  private final Map<Integer, StackPane> cardById = new HashMap<>();
  private final Map<Integer, Label> crossById = new HashMap<>();

  @FXML private TilePane grid;
  @FXML private TextArea log;
  @FXML private Label statusLabel;
  @FXML private Label remainingLabel;
  @FXML private ImageView playerSecretView;
  @FXML private ImageView aiSecretView;
  @FXML private ComboBox<String> hatCombo;
  @FXML private ComboBox<String> armsCombo;
  @FXML private ComboBox<String> bottomCombo;
  @FXML private ComboBox<String> bottomPatternCombo;
  @FXML private ComboBox<String> primaryColorCombo;
  @FXML private ComboBox<String> secondaryColorCombo;
  @FXML private Button askHatButton;
  @FXML private Button askArmsButton;
  @FXML private Button askBottomButton;
  @FXML private Button askBottomPatternButton;
  @FXML private Button askPrimaryButton;
  @FXML private Button askSecondaryButton;
  @FXML private Button setSecretButton;
  @FXML private Button guessButton;
  @FXML private Button toggleBoardButton;

  private final GameManager gameManager = new GameManager();
  private final DukeImagesManager dukeImagesManager = new DukeImagesManager();
  private boolean showingAiBoard = false;

  @FXML
  private void onAskHat(ActionEvent event) {
    askHat();
  }

  @FXML
  private void onAskArms(ActionEvent event) {
    askArms();
  }

  @FXML
  private void onAskBottom(ActionEvent event) {
    askBottom();
  }

  @FXML
  private void onAskBottomPattern(ActionEvent event) {
    askBottomPattern();
  }

  @FXML
  private void onPrimaryColor(ActionEvent event) {
    askColor(true);
  }

  @FXML
  private void onSecondaryColor(ActionEvent event) {
    askColor(false);
  }

  @FXML
  private void onGuess(ActionEvent event) {
    makeFinalGuess();
  }

  @FXML
  private void onSetSecret(ActionEvent event) {
    setPlayerSecret();
  }

  @FXML
  private void onNewGame(ActionEvent event) {
    startNewGame();
  }

  @FXML
  private void onToggleBoard(ActionEvent event) {
    toggleBoard();
  }

  public void startNewGame() {
    log.clear();
    gameManager.newGame();
    statusLabel.setText("Pick your secret Duke to start.");
    remainingLabel.setText("Remaining: " + gameManager.getDukeRemainingCount());
    aiSecretView.setImage(null);
    playerSecretView.setImage(null);
    showingAiBoard = false;
    toggleBoardButton.setText("View AI Board");
    buildSelectors();
    buildGrid();
    setControlsEnabled(false);
    guessButton.setDisable(true);
    setSecretButton.setDisable(false);
  }

  private void buildSelectors() {
    hatCombo.getItems().clear();
    gameManager.getDukefactory().getAllHats().stream()
        .map(DukeAttribute::name)
        .forEach(hatCombo.getItems()::add);

    armsCombo.getItems().clear();

    gameManager.getDukefactory().getAllArms().stream()
        .map(DukeAttribute::name)
        .forEach(armsCombo.getItems()::add);

    bottomCombo.getItems().clear();
    gameManager.getDukefactory().getAllBottoms().stream()
        .map(DukeAttribute::name)
        .forEach(bottomCombo.getItems()::add);

    bottomPatternCombo.getItems().clear();
    gameManager.getDukefactory().getAllBottomPatterns().stream()
        .map(DukeAttribute::name)
        .forEach(bottomPatternCombo.getItems()::add);

    primaryColorCombo.getItems().clear();
    secondaryColorCombo.getItems().clear();

    // Both dropdowns get the same unique list of colors
    Set<String> uniquePrimaryColors = new LinkedHashSet<>();
    for (ColorScheme scheme : gameManager.getDukefactory().getAllColors()) {
      uniquePrimaryColors.add(scheme.primaryColorName());
    }
    for (String colorName : uniquePrimaryColors) {
      primaryColorCombo.getItems().add(colorName);
      secondaryColorCombo.getItems().add(colorName);
    }

    if (!hatCombo.getItems().isEmpty()) {
      hatCombo.getSelectionModel().select(0);
    }

    if (!armsCombo.getItems().isEmpty()) {
      armsCombo.getSelectionModel().select(0);
    }

    if (!bottomCombo.getItems().isEmpty()) {
      bottomCombo.getSelectionModel().select(0);
    }

    if (!bottomPatternCombo.getItems().isEmpty()) {
      bottomPatternCombo.getSelectionModel().select(0);
    }

    if (!primaryColorCombo.getItems().isEmpty()) {
      primaryColorCombo.getSelectionModel().select(0);
    }

    if (!secondaryColorCombo.getItems().isEmpty()) {
      secondaryColorCombo.getSelectionModel().select(0);
    }
  }

  private void buildGrid() {
    grid.getChildren().clear();
    cardById.clear();
    crossById.clear();
    for (Duke duke : gameManager.getAllDukes()) {
      StackPane card = createCard(duke);
      grid.getChildren().add(card);
      cardById.put(duke.id(), card);
    }
    updateEliminations();
  }

  private StackPane createCard(Duke duke) {
    Image image = dukeImagesManager.find(duke);
    ImageView imageView = new ImageView(image);

    imageView.setFitWidth(90);
    imageView.setFitHeight(90);
    imageView.setPreserveRatio(true);

    Label cross = new Label("X");
    cross.setStyle(
        "-fx-font-size: 48; -fx-text-fill: "
            + toCssColor(CROSS_COLOR.deriveColor(0, 1, 1, 0.8))
            + ";");
    cross.setVisible(false);

    StackPane card = new StackPane(imageView, cross);
    card.setAlignment(Pos.CENTER);
    card.setStyle(
        "-fx-border-color: " + toCssColor(BORDER_COLOR) + "; -fx-border-width: 1; -fx-padding: 4;");
    card.setOnMouseClicked(
        e -> {
          if (gameManager.getPlayerSecret() == null) {
            gameManager.setSelectedSecret(duke);
            highlightSelection();
          }
        });

    crossById.put(duke.id(), cross);

    return card;
  }

  private void highlightSelection() {
    for (Duke duke : gameManager.getAllDukes()) {
      StackPane card = cardById.get(duke.id());
      if (card == null) continue;
      if (duke.equals(gameManager.getSelectedSecret())) {
        card.setStyle(
            "-fx-border-color: "
                + toCssColor(SELECTED_BORDER_COLOR)
                + "; -fx-border-width: 1; -fx-padding: 3;"
                + "-fx-background-color: "
                + toCssColor(SELECTED_BORDER_COLOR));
      } else {
        card.setStyle(
            "-fx-border-color: "
                + toCssColor(BORDER_COLOR)
                + "; -fx-border-width: 1; -fx-padding: 4;");
      }
    }
  }

  private void setPlayerSecret() {
    if (gameManager.getSelectedSecret() == null) {
      addLog("Select a Duke from the grid first.");
      return;
    }

    gameManager.setPlayerSecret(gameManager.getSelectedSecret());
    playerSecretView.setImage(dukeImagesManager.find(gameManager.getPlayerSecret()));
    statusLabel.setText("Game started. Ask about hats or colors.");
    setControlsEnabled(true);
    addLog("Your secret is set.");

    setSecretButton.setDisable(true);
  }

  private void askHat() {
    GuessResult<DukeAttribute> guessResult = gameManager.askHat(hatCombo.getValue());
    addLog(
        switch (guessResult.kind()) {
          case NOT_READY -> "Pick and set your secret Duke first.";
          case INVALID -> "Pick a hat first.";
          case CORRECT -> "AI: Yes, it has a " + guessResult.value().name() + ".";
          case INCORRECT -> "AI: No, it does NOT have a " + guessResult.value().name() + ".";
        });

    if (guessResult.kind() == GuessResultKind.NOT_READY) return;

    afterPlayerQuestion();
    handleAiTurn();
  }

  private void askArms() {
    GuessResult<DukeAttribute> guessResult = gameManager.askArms(armsCombo.getValue());
    addLog(
        switch (guessResult.kind()) {
          case NOT_READY -> "Pick and set your secret Duke first.";
          case INVALID -> "Pick arms first.";
          case CORRECT -> "AI: Yes, it has " + guessResult.value().name() + ".";
          case INCORRECT -> "AI: No, it does NOT have " + guessResult.value().name() + ".";
        });

    if (guessResult.kind() == GuessResultKind.NOT_READY) return;

    afterPlayerQuestion();
    handleAiTurn();
  }

  private void askBottom() {
    GuessResult<DukeAttribute> guessResult = gameManager.askBottom(bottomCombo.getValue());
    addLog(
        switch (guessResult.kind()) {
          case NOT_READY -> "Pick and set your secret Duke first.";
          case INVALID -> "Pick a bottom first.";
          case CORRECT -> "AI: Yes, it has " + guessResult.value().name() + ".";
          case INCORRECT -> "AI: No, it does NOT have " + guessResult.value().name() + ".";
        });

    if (guessResult.kind() == GuessResultKind.NOT_READY) return;

    afterPlayerQuestion();
    handleAiTurn();
  }

  private void askBottomPattern() {
    GuessResult<DukeAttribute> guessResult =
        gameManager.askBottomPattern(bottomPatternCombo.getValue());
    addLog(
        switch (guessResult.kind()) {
          case NOT_READY -> "Pick and set your secret Duke first.";
          case INVALID -> "Pick a bottom pattern first.";
          case CORRECT -> "AI: Yes, it has " + guessResult.value().name() + ".";
          case INCORRECT -> "AI: No, it does NOT have " + guessResult.value().name() + " pattern.";
        });

    if (guessResult.kind() == GuessResultKind.NOT_READY) return;

    afterPlayerQuestion();
    handleAiTurn();
  }

  private void askColor(boolean primary) {

    String colorName = primary ? primaryColorCombo.getValue() : secondaryColorCombo.getValue();
    String label = primary ? "primary" : "secondary";

    GuessResult<Void> guessResult = gameManager.askColor(primary, colorName);
    addLog(
        switch (guessResult.kind()) {
          case NOT_READY -> "Pick and set your secret Duke first.";
          case INVALID -> "Pick a color first.";
          case CORRECT -> "AI: Yes, it has the " + label + " color " + colorName + ".";
          case INCORRECT -> "AI: No, it does NOT have the " + label + " color " + colorName + ".";
        });

    if (guessResult.kind() == GuessResultKind.NOT_READY) return;

    afterPlayerQuestion();
    handleAiTurn();
  }

  private void afterPlayerQuestion() {
    updateEliminations();

    int remainingCount = gameManager.getDukeRemainingCount();
    remainingLabel.setText("Remaining: " + remainingCount);

    if (remainingCount == 1) {
      addLog("Only one candidate left. You can guess now.");
      guessButton.setDisable(false);
    }

    if (remainingCount == 0) {
      endGame(false, "No candidates left.");
    }
  }

  private void handleAiTurn() {
    GuessResult<AiQuestion> guessResult = gameManager.handleAiTurn();
    if (guessResult.kind() == GuessResultKind.NOT_READY) return;

    AiQuestion question = guessResult.value();
    addLog(question.toLogMessage());

    List<Duke> aiRemaining = gameManager.getAiRemaining();
    if (aiRemaining.size() == 1) {
      Duke aiGuess = aiRemaining.getFirst();

      if (aiGuess.equals(gameManager.getPlayerSecret())) {
        endGame(false, "AI guessed your Duke first.");
      }
    }
  }

  private void makeFinalGuess() {
    List<Duke> playerRemaining = gameManager.getPlayerRemaining();
    if (playerRemaining.size() != 1) {
      addLog("You need exactly one candidate left.");
      return;
    }

    Duke guess = playerRemaining.getFirst();

    if (guess.equals(gameManager.getAiSecret())) {
      endGame(true, "You guessed the AI Duke.");
    } else {
      endGame(false, "Wrong guess. AI Duke was different.");
    }
  }

  private void endGame(boolean playerWon, String reason) {
    setControlsEnabled(false);
    guessButton.setDisable(true);
    aiSecretView.setImage(dukeImagesManager.find(gameManager.getAiSecret()));
    statusLabel.setText(playerWon ? "You win!" : "AI wins!");
    addLog("Game over: " + reason);
  }

  private void setControlsEnabled(boolean enabled) {
    askHatButton.setDisable(!enabled);
    askArmsButton.setDisable(!enabled);
    askBottomButton.setDisable(!enabled);
    askBottomPatternButton.setDisable(!enabled);
    askPrimaryButton.setDisable(!enabled);
    askSecondaryButton.setDisable(!enabled);
    hatCombo.setDisable(!enabled);
    armsCombo.setDisable(!enabled);
    bottomCombo.setDisable(!enabled);
    primaryColorCombo.setDisable(!enabled);
    secondaryColorCombo.setDisable(!enabled);
  }

  private void toggleBoard() {
    showingAiBoard = !showingAiBoard;
    toggleBoardButton.setText(showingAiBoard ? "View Player Board" : "View AI Board");
    updateEliminations();
  }

  private void updateEliminations() {
    Set<Integer> remainingIds =
        showingAiBoard ? gameManager.getAiRemainingIds() : gameManager.getRemainingIds();
    for (Duke duke : gameManager.getAllDukes()) {
      Label cross = crossById.get(duke.id());
      if (cross != null) {
        cross.setVisible(!remainingIds.contains(duke.id()));
      }
    }
  }

  private void addLog(String message) {
    log.appendText(message + "\n");
  }

  private String toCssColor(Color color) {
    int r = (int) Math.round(color.getRed() * 255);
    int g = (int) Math.round(color.getGreen() * 255);
    int b = (int) Math.round(color.getBlue() * 255);
    double a = Math.round(color.getOpacity() * 100.0) / 100.0;
    return "rgba(" + r + "," + g + "," + b + "," + a + ")";
  }
}
