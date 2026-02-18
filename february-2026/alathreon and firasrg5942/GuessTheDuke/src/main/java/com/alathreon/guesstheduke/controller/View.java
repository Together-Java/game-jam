package com.alathreon.guesstheduke.controller;

import javafx.scene.Scene;

public record View<T>(Scene scene, T controller) {}
