package com.togetherjava.dukeadventure;

/**
 * Represents the current state/screen of the game.
 */
public enum GameState {
    TITLE,          // Title screen
    OVERWORLD,      // Exploring the world
    BATTLE,         // Wild Duke battle
    BOSS_BATTLE,    // Boss fight
    DIALOG,         // Talking to NPC
    SHOP,           // Shopping at TJ-Bot's shop
    QUEST_LOG,      // View active quests
    COLLECTION,     // View Duke collection
    GAME_OVER,      // Player lost
    VICTORY         // Defeated the boss!
}
