package com.alathreon.guesstheduke.model;

public record AiQuestion(String type, String value, boolean isCorrect, boolean isPrimary) {
  public String toLogMessage() {
    String answer = isCorrect ? "Yes." : "No.";
    return switch (type) {
      case "hat" -> "AI asks: Is your Duke wearing a " + value + "? " + answer;
      case "arms" -> "AI asks: Does your Duke have " + value + "? " + answer;
      case "bottom" -> "AI asks: Is your Duke wearing " + value + "? " + answer;
      case "bottomPattern" ->
          "AI asks: Is your Duke wearing a bottom with " + value + "? " + answer;
      case "color" -> {
        String colorLabel = isPrimary ? "primary" : "secondary";
        yield "AI asks: Does your Duke have " + value + " as " + colorLabel + " color? " + answer;
      }
      default -> "AI asks a question.";
    };
  }
}
