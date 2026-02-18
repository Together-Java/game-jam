package com.alathreon.guesstheduke.util;

import com.alathreon.guesstheduke.model.Duke;

public record DukeKey(
    String hat,
    String arms,
    String bottom,
    String bottomPattern,
    String primaryColor,
    String secondaryColor) {
  public static DukeKey create(Duke duke) {
    return new DukeKey(
        duke.hat().imagePath(),
        duke.arms().imagePath(),
        duke.bottom().imagePath(),
        duke.bottomPattern().imagePath(),
        duke.colorScheme().primaryColorName(),
        duke.colorScheme().secondaryColorName());
  }
}
