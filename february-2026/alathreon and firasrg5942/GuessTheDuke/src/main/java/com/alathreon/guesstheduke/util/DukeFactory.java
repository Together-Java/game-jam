package com.alathreon.guesstheduke.util;

import com.alathreon.guesstheduke.model.ColorScheme;
import com.alathreon.guesstheduke.model.Duke;
import com.alathreon.guesstheduke.model.DukeAttribute;
import java.util.*;
import java.util.random.RandomGenerator;
import javafx.scene.paint.Color;

public class DukeFactory {

  private final List<DukeAttribute> allHats =
      List.of(
          new DukeAttribute("Bandana", "/duke/head/Bandana.png"),
          new DukeAttribute("Bucket", "/duke/head/Bucket.png"),
          new DukeAttribute("Cap", "/duke/head/Cap.png"),
          new DukeAttribute("Cardboard Box", "/duke/head/CardboardBox.png"),
          new DukeAttribute("Crown", "/duke/head/Crown.png"),
          new DukeAttribute("Fascinator", "/duke/head/Fascinator.png"),
          new DukeAttribute("Glasses", "/duke/head/Glasses.png"),
          new DukeAttribute("Graduation Cap", "/duke/head/GraduationCap.png"),
          new DukeAttribute("Hair Clips", "/duke/head/HairClips.png"),
          new DukeAttribute("Hanging Earrings", "/duke/head/HangingEarrings.png"),
          new DukeAttribute("Laurel Wreath", "/duke/head/LaurelWreath.png"),
          new DukeAttribute("Party Hat", "/duke/head/PartyHat.png"),
          new DukeAttribute("Pirate Hat", "/duke/head/PirateHat.png"),
          new DukeAttribute("Santa Hat", "/duke/head/SantaHat.png"),
          new DukeAttribute("Sombrero", "/duke/head/Sombrero.png"),
          new DukeAttribute("Tiara", "/duke/head/Tiara.png"),
          new DukeAttribute("Top Hat", "/duke/head/TopHat.png"),
          new DukeAttribute("Turban", "/duke/head/Turban.png"),
          new DukeAttribute("Viking Helmet", "/duke/head/VikingHelmet.png"),
          new DukeAttribute("Wizard Hat", "/duke/head/WizardHat.png"));

  private final List<DukeAttribute> allArms =
      List.of(
          new DukeAttribute("Arms Out To Sides", "/duke/arms/ArmsOutToSides.png"),
          new DukeAttribute("Arms Straight Up", "/duke/arms/ArmsStraightUp.png"),
          new DukeAttribute("Biceps Flex", "/duke/arms/BicepsFlex.png"),
          new DukeAttribute("Crossed Arms", "/duke/arms/CrossedArms.png"),
          new DukeAttribute("Dab Pose", "/duke/arms/DabPose.png"),
          new DukeAttribute("Goalpost Down Pose", "/duke/arms/GoalpostDownPose.png"),
          new DukeAttribute("Goalpost Pose", "/duke/arms/GoalpostPose.png"),
          new DukeAttribute("Hand On Nose", "/duke/arms/HandOnNose.png"),
          new DukeAttribute("Hands At Sides", "/duke/arms/HandsAtSides.png"),
          new DukeAttribute("Hands In Front", "/duke/arms/HandsInFront.png"),
          new DukeAttribute("Hands On Hips", "/duke/arms/HandsOnHips.png"),
          new DukeAttribute("Hand Stop Pose", "/duke/arms/HandStopPose.png"),
          new DukeAttribute("Hand Waving", "/duke/arms/HandWaving.png"),
          new DukeAttribute("Jazz Hands", "/duke/arms/JazzHands.png"),
          new DukeAttribute("One Arm Up One Arm Down", "/duke/arms/OneArmUpOneArmDown.png"),
          new DukeAttribute("Sassy Pose", "/duke/arms/SassyPose.png"),
          new DukeAttribute("Stretching Arms", "/duke/arms/StretchingArms.png"),
          new DukeAttribute("Thumbs Down", "/duke/arms/ThumbsDown.png"),
          new DukeAttribute("Thumbs Up", "/duke/arms/ThumbsUp.png"),
          new DukeAttribute("V Pose", "/duke/arms/VPose.png"));

  private final List<DukeAttribute> allBottoms =
      List.of(
          new DukeAttribute("High Rise Pants", "/duke/bottom/shape/HighRisePants.png"),
          new DukeAttribute("Low Rise Pants", "/duke/bottom/shape/LowRisePants.png"),
          new DukeAttribute("Skirt", "/duke/bottom/shape/Skirt.png"),
          new DukeAttribute("Swim Suit", "/duke/bottom/shape/SwimSuit.png"));

  private final List<DukeAttribute> allBottomPatterns =
      List.of(
          new DukeAttribute("Checker", "/duke/bottom/pattern/Checker.png"),
          new DukeAttribute("Clear Gingham", "/duke/bottom/pattern/ClearGingham.png"),
          new DukeAttribute("Colored Gingham", "/duke/bottom/pattern/ColoredGingham.png"),
          new DukeAttribute("Diamond", "/duke/bottom/pattern/Diamond.png"),
          new DukeAttribute("Flower", "/duke/bottom/pattern/Flower.png"),
          new DukeAttribute("Horizontal Stripe", "/duke/bottom/pattern/HorizontalStripe.png"),
          new DukeAttribute("Monochrome", "/duke/bottom/pattern/Monochrome.png"),
          new DukeAttribute("Polka Dot", "/duke/bottom/pattern/PolkaDot.png"),
          new DukeAttribute("Vertical Stripe", "/duke/bottom/pattern/VerticalStripe.png"));

  private final List<String> baseColorNames =
      List.of(
          "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Cyan", "Pink", "Brown", "Teal");

  private final List<Color> baseColors =
      List.of(
          Color.RED,
          Color.BLUE,
          Color.GREEN,
          Color.YELLOW,
          Color.ORANGE,
          Color.PURPLE,
          Color.CYAN,
          Color.HOTPINK,
          Color.SADDLEBROWN,
          Color.TEAL);

  private final List<ColorScheme> availableColors;

  private final RandomGenerator random;

  public DukeFactory(RandomGenerator random) {
    this.random = random;
    this.availableColors = generateColorCombinations();
  }

  private List<ColorScheme> generateColorCombinations() {
    List<ColorScheme> allCombinations = new ArrayList<>();

    for (int i = 0; i < baseColors.size(); i++) {
      for (int j = 0; j < baseColors.size(); j++) {

        if (i != j && !baseColors.get(i).equals(baseColors.get(j))) {
          allCombinations.add(
              new ColorScheme(
                  baseColors.get(i),
                  baseColors.get(j),
                  baseColorNames.get(i),
                  baseColorNames.get(j)));
        }
      }
    }

    return allCombinations;
  }

  public List<Duke> createAllDukes(int count) {
    Set<DukeKey> keys = new HashSet<>();
    List<Duke> allDukes = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      while (true) {
        DukeAttribute hat = takeRandom(allHats);
        DukeAttribute arm = takeRandom(allArms);
        DukeAttribute bottom = takeRandom(allBottoms);
        DukeAttribute bottomPattern = takeRandom(allBottomPatterns);
        ColorScheme colorScheme = takeRandom(availableColors);
        Duke duke = new Duke(i, hat, arm, bottom, bottomPattern, colorScheme);
        DukeKey dukeKey = DukeKey.create(duke);
        if (keys.add(dukeKey)) {
          allDukes.add(duke);
          break;
        }
      }
    }
    return allDukes;
  }

  private <T> T takeRandom(List<T> attributes) {
    return attributes.get(random.nextInt(attributes.size()));
  }

  public List<DukeAttribute> getAllHats() {
    return allHats;
  }

  public List<DukeAttribute> getAllArms() {
    return allArms;
  }

  public List<DukeAttribute> getAllBottoms() {
    return allBottoms;
  }

  public List<DukeAttribute> getAllBottomPatterns() {
    return allBottomPatterns;
  }

  public List<ColorScheme> getAllColors() {
    return availableColors;
  }
}
