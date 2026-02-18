package com.alathreon.guesstheduke.logic;

import com.alathreon.guesstheduke.model.AiQuestion;
import com.alathreon.guesstheduke.model.Duke;
import com.alathreon.guesstheduke.model.DukeAttribute;
import com.alathreon.guesstheduke.util.DukeFactory;
import java.util.*;
import java.util.stream.Collectors;

public class GameManager {

  private final Random random = new Random();

  private final DukeFactory dukeFactory = new DukeFactory(random);

  private List<Duke> allDukes;

  private Duke aiSecret;
  private Duke playerSecret;
  private Duke selectedSecret;

  private List<Duke> playerRemaining;
  private List<Duke> aiRemaining;

  public void newGame() {
    allDukes = dukeFactory.createAllDukes(100);
    aiSecret = allDukes.get(random.nextInt(allDukes.size()));
    playerSecret = null;
    selectedSecret = null;
    playerRemaining = new ArrayList<>(allDukes);
    aiRemaining = new ArrayList<>(allDukes);
  }

  public int getDukeRemainingCount() {
    return playerRemaining.size();
  }

  public DukeFactory getDukefactory() {
    return dukeFactory;
  }

  public List<Duke> getAllDukes() {
    return Collections.unmodifiableList(allDukes);
  }

  public Duke getSelectedSecret() {
    return selectedSecret;
  }

  public void setSelectedSecret(Duke selectedSecret) {
    this.selectedSecret = selectedSecret;
  }

  public Duke getPlayerSecret() {
    return playerSecret;
  }

  public void setPlayerSecret(Duke playerSecret) {
    this.playerSecret = playerSecret;
  }

  public Duke getAiSecret() {
    return aiSecret;
  }

  public List<Duke> getPlayerRemaining() {
    return playerRemaining;
  }

  public List<Duke> getAiRemaining() {
    return aiRemaining;
  }

  public GuessResult<DukeAttribute> askHat(String hatName) {
    if (!isReady()) return new GuessResult<>(GuessResultKind.NOT_READY, null);

    DukeAttribute hat = findHatByName(hatName);
    if (hat == null) {
      return new GuessResult<>(GuessResultKind.INVALID, null);
    }

    boolean match = aiSecret.hat().equals(hat);
    if (match) {
      playerRemaining = filterByHat(playerRemaining, hat, true);
      return new GuessResult<>(GuessResultKind.CORRECT, hat);
    } else {
      playerRemaining = filterByHat(playerRemaining, hat, false);
      return new GuessResult<>(GuessResultKind.INCORRECT, hat);
    }
  }

  public GuessResult<DukeAttribute> askArms(String armsName) {
    if (!isReady()) return new GuessResult<>(GuessResultKind.NOT_READY, null);

    DukeAttribute arms = findArmsByName(armsName);
    if (arms == null) {
      return new GuessResult<>(GuessResultKind.INVALID, null);
    }

    boolean match = aiSecret.arms().equals(arms);
    if (match) {
      playerRemaining = filterByArms(playerRemaining, arms, true);
      return new GuessResult<>(GuessResultKind.CORRECT, arms);
    } else {
      playerRemaining = filterByArms(playerRemaining, arms, false);
      return new GuessResult<>(GuessResultKind.INCORRECT, arms);
    }
  }

  public GuessResult<DukeAttribute> askBottom(String bottomName) {
    if (!isReady()) return new GuessResult<>(GuessResultKind.NOT_READY, null);

    DukeAttribute bottom = findBottomByName(bottomName);
    if (bottom == null) {
      return new GuessResult<>(GuessResultKind.INVALID, null);
    }

    boolean match = aiSecret.bottom().equals(bottom);
    if (match) {
      playerRemaining = filterByBottom(playerRemaining, bottom, true);
      return new GuessResult<>(GuessResultKind.CORRECT, bottom);
    } else {
      playerRemaining = filterByBottom(playerRemaining, bottom, false);
      return new GuessResult<>(GuessResultKind.INCORRECT, bottom);
    }
  }

  public GuessResult<DukeAttribute> askBottomPattern(String bottomPatternName) {
    if (!isReady()) return new GuessResult<>(GuessResultKind.NOT_READY, null);

    DukeAttribute bottomPattern = findBottomPatternByName(bottomPatternName);
    if (bottomPattern == null) {
      return new GuessResult<>(GuessResultKind.INVALID, null);
    }

    boolean match = aiSecret.bottomPattern().equals(bottomPattern);
    if (match) {
      playerRemaining = filterByBottomPattern(playerRemaining, bottomPattern, true);
      return new GuessResult<>(GuessResultKind.CORRECT, bottomPattern);
    } else {
      playerRemaining = filterByBottomPattern(playerRemaining, bottomPattern, false);
      return new GuessResult<>(GuessResultKind.INCORRECT, bottomPattern);
    }
  }

  public GuessResult<Void> askColor(boolean primary, String colorName) {
    if (!isReady()) return new GuessResult<>(GuessResultKind.NOT_READY, null);

    if (colorName == null || colorName.isBlank()) {
      return new GuessResult<>(GuessResultKind.INVALID, null);
    }

    boolean match =
        primary
            ? aiSecret.colorScheme().primaryColorName().equals(colorName)
            : aiSecret.colorScheme().secondaryColorName().equals(colorName);

    if (match) {
      playerRemaining = filterByColor(playerRemaining, colorName, primary, true);
      return new GuessResult<>(GuessResultKind.CORRECT, null);
    } else {
      playerRemaining = filterByColor(playerRemaining, colorName, primary, false);
      return new GuessResult<>(GuessResultKind.INCORRECT, null);
    }
  }

  public GuessResult<AiQuestion> handleAiTurn() {
    if (playerSecret == null || aiRemaining.isEmpty())
      return new GuessResult<>(GuessResultKind.NOT_READY, null);

    // AI asks about a random attribute type: 0=hat, 1=arms, 2=bottom, 3=bottom, 4=color
    int questionType = random.nextInt(5);

    switch (questionType) {
      case 0:
        DukeAttribute randomHat = aiRemaining.get(random.nextInt(aiRemaining.size())).hat();
        boolean matchHat = playerSecret.hat().equals(randomHat);
        if (matchHat) {
          aiRemaining = filterByHat(aiRemaining, randomHat, true);
        } else {
          aiRemaining = filterByHat(aiRemaining, randomHat, false);
        }
        return new GuessResult<>(
            matchHat ? GuessResultKind.CORRECT : GuessResultKind.INCORRECT,
            new AiQuestion("hat", randomHat.name(), matchHat, false));

      case 1:
        DukeAttribute randomArms = aiRemaining.get(random.nextInt(aiRemaining.size())).arms();
        boolean matchArms = playerSecret.arms().equals(randomArms);
        if (matchArms) {
          aiRemaining = filterByArms(aiRemaining, randomArms, true);
        } else {
          aiRemaining = filterByArms(aiRemaining, randomArms, false);
        }
        return new GuessResult<>(
            matchArms ? GuessResultKind.CORRECT : GuessResultKind.INCORRECT,
            new AiQuestion("arms", randomArms.name(), matchArms, false));

      case 2:
        DukeAttribute randomBottom = aiRemaining.get(random.nextInt(aiRemaining.size())).bottom();
        boolean matchBottom = playerSecret.bottom().equals(randomBottom);
        if (matchBottom) {
          aiRemaining = filterByBottom(aiRemaining, randomBottom, true);
        } else {
          aiRemaining = filterByBottom(aiRemaining, randomBottom, false);
        }
        return new GuessResult<>(
            matchBottom ? GuessResultKind.CORRECT : GuessResultKind.INCORRECT,
            new AiQuestion("bottom", randomBottom.name(), matchBottom, false));

      case 3:
        DukeAttribute randomBottomPattern =
            aiRemaining.get(random.nextInt(aiRemaining.size())).bottomPattern();
        boolean matchBottomPattern = playerSecret.bottom().equals(randomBottomPattern);
        if (matchBottomPattern) {
          aiRemaining = filterByBottomPattern(aiRemaining, randomBottomPattern, true);
        } else {
          aiRemaining = filterByBottomPattern(aiRemaining, randomBottomPattern, false);
        }
        return new GuessResult<>(
            matchBottomPattern ? GuessResultKind.CORRECT : GuessResultKind.INCORRECT,
            new AiQuestion("bottomPattern", randomBottomPattern.name(), matchBottomPattern, false));

      case 4:
      default:
        // Ask about primary or secondary color randomly
        boolean askPrimary = random.nextBoolean();
        String colorName =
            askPrimary
                ? aiRemaining
                    .get(random.nextInt(aiRemaining.size()))
                    .colorScheme()
                    .primaryColorName()
                : aiRemaining
                    .get(random.nextInt(aiRemaining.size()))
                    .colorScheme()
                    .secondaryColorName();

        boolean matchColor =
            askPrimary
                ? playerSecret.colorScheme().primaryColorName().equals(colorName)
                : playerSecret.colorScheme().secondaryColorName().equals(colorName);

        if (matchColor) {
          aiRemaining = filterByColor(aiRemaining, colorName, askPrimary, true);
        } else {
          aiRemaining = filterByColor(aiRemaining, colorName, askPrimary, false);
        }
        return new GuessResult<>(
            matchColor ? GuessResultKind.CORRECT : GuessResultKind.INCORRECT,
            new AiQuestion("color", colorName, matchColor, askPrimary));
    }
  }

  private boolean isReady() {
    return playerSecret != null;
  }

  public Set<Integer> getRemainingIds() {
    return playerRemaining.stream().map(Duke::id).collect(Collectors.toSet());
  }

  public Set<Integer> getAiRemainingIds() {
    return aiRemaining.stream().map(Duke::id).collect(Collectors.toSet());
  }

  private DukeAttribute findHatByName(String name) {
    if (name == null) return null;
    return dukeFactory.getAllHats().stream()
        .filter(hat -> hat.name().equals(name))
        .findFirst()
        .orElse(null);
  }

  private DukeAttribute findArmsByName(String name) {
    if (name == null) return null;
    return dukeFactory.getAllArms().stream()
        .filter(arms -> arms.name().equals(name))
        .findFirst()
        .orElse(null);
  }

  private DukeAttribute findBottomByName(String name) {
    if (name == null) return null;
    return dukeFactory.getAllBottoms().stream()
        .filter(bottom -> bottom.name().equals(name))
        .findFirst()
        .orElse(null);
  }

  private DukeAttribute findBottomPatternByName(String name) {
    if (name == null) return null;
    return dukeFactory.getAllBottomPatterns().stream()
        .filter(bottom -> bottom.name().equals(name))
        .findFirst()
        .orElse(null);
  }

  private List<Duke> filterByHat(List<Duke> source, DukeAttribute hat, boolean keepMatches) {
    return source.stream().filter(duke -> (duke.hat().equals(hat)) == keepMatches).toList();
  }

  private List<Duke> filterByArms(List<Duke> source, DukeAttribute arms, boolean keepMatches) {
    return source.stream().filter(duke -> (duke.arms().equals(arms)) == keepMatches).toList();
  }

  private List<Duke> filterByBottom(List<Duke> source, DukeAttribute bottom, boolean keepMatches) {
    return source.stream().filter(duke -> (duke.bottom().equals(bottom)) == keepMatches).toList();
  }

  private List<Duke> filterByBottomPattern(
      List<Duke> source, DukeAttribute bottomPattern, boolean keepMatches) {
    return source.stream()
        .filter(duke -> (duke.bottomPattern().equals(bottomPattern)) == keepMatches)
        .toList();
  }

  private List<Duke> filterByColor(
      List<Duke> source, String colorName, boolean primary, boolean keepMatches) {
    return source.stream()
        .filter(
            duke -> {
              String candidateColor =
                  primary
                      ? duke.colorScheme().primaryColorName()
                      : duke.colorScheme().secondaryColorName();
              return candidateColor.equals(colorName) == keepMatches;
            })
        .toList();
  }
}
