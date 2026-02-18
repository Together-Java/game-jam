package com.alathreon.guesstheduke.model;

import javafx.scene.paint.Color;

public record ColorScheme(
    Color primaryColor, Color secondaryColor, String primaryColorName, String secondaryColorName) {}
