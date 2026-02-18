package com.alathreon.guesstheduke.model;

public record Duke(
    int id,
    DukeAttribute hat,
    DukeAttribute arms,
    DukeAttribute bottom,
    DukeAttribute bottomPattern,
    ColorScheme colorScheme) {}
