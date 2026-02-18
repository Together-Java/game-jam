package com.alathreon.guesstheduke.logic;

public record GuessResult<T>(GuessResultKind kind, T value) {}
