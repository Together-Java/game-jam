package com.togetherjava.dukeadventure;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DukeAdventure extends Application {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 576;
    private static final int TILE_SIZE = 32;
    
    // Game objects
    private Canvas canvas;
    private GraphicsContext gc;
    private GameState currentState;
    private Camera camera;  // NEW: Camera system!
    
    private World world;
    private Player player;
    private QuestSystem questSystem;
    
    // Battle state
    private BattleSystem currentBattle;
    private BossSystem.BossBattle bossBattle;
    
    // Dialog state
    private DialogSystem.Conversation currentConversation;
    private NPC talkingToNPC;
    
    // Shop state
    private ShopSystem shop;
    
    // Battle cooldown (FIX for infinite battle loop!)
    private double battleCooldown = 0;
    private static final double BATTLE_COOLDOWN_TIME = 3.0; // 3 seconds
    
    // Time tracking
    private long lastFrameTime = 0;
    
    @Override
    public void start(Stage stage) {
        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);
        
        initializeGame();
        
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                if (lastFrameTime == 0) deltaTime = 0.016; // First frame
                lastFrameTime = now;
                
                update(deltaTime);
                render();
            }
        }.start();
        
        stage.setTitle("Duke Adventure");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        System.out.println("=== Duke Adventure ===");
        System.out.println("Hello there");
        System.out.println("Made by barshERROR");
        System.out.println("Save the together java village!");
        System.out.println("Good luck");
        System.out.println("=====================================");
    }
    
    private void initializeGame() {
        currentState = GameState.TITLE;
        
        // Create bigger world (50x50!)
        world = new World(50, 50);
        
        // Create player in center of world
        player = new Player(25, 25);
        
        // Create camera
        camera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT, TILE_SIZE);
        camera.setWorldBounds(50, 50);
        
        // Create quest system
        questSystem = new QuestSystem();
    }
    
    private void update(double deltaTime) {
        // Update battle cooldown
        if (battleCooldown > 0) {
            battleCooldown -= deltaTime;
        }
        
        switch (currentState) {
            case OVERWORLD:
                player.update(deltaTime); // UPDATE PLAYER!
                updateOverworld();
                // Update camera to follow player
                camera.centerOn(player.getGridX(), player.getGridY());
                break;
            case BATTLE:
                updateBattle();
                break;
            case BOSS_BATTLE:
                updateBossBattle(deltaTime);
                break;
        }
    }
    
    private void updateOverworld() {
        // IMPORTANT: Only check for encounters if cooldown is 0!
        if (battleCooldown <= 0 && world.checkEncounter(player.getGridX(), player.getGridY())) {
            startWildBattle();
        }
        
        if (world.isBossLocation(player.getGridX(), player.getGridY())) {
            if (BossSystem.canChallengeBoss(player)) {
                startBossBattle();
            }
        }
    }
    
    private void updateBattle() {
        if (!currentBattle.isPlayerTurn() && currentBattle.isActive()) {
            currentBattle.enemyAttack();
        }
    }
    
    private void updateBossBattle(double deltaTime) {
        if (bossBattle != null) {
            bossBattle.update(deltaTime);
            
            if (!bossBattle.isPlayerTurn() && bossBattle.isBattleActive()) {
                bossBattle.bossAttack();
            }
        }
    }
    
    private void render() {
        switch (currentState) {
            case TITLE:
                renderTitle();
                break;
            case OVERWORLD:
                renderOverworld();
                break;
            case BATTLE:
                renderBattle();
                break;
            case BOSS_BATTLE:
                renderBossBattle();
                break;
            case DIALOG:
                renderDialog();
                break;
            case SHOP:
                renderShop();
                break;
            case QUEST_LOG:
                renderQuestLog();
                break;
            case COLLECTION:
                renderCollection();
                break;
            case VICTORY:
                renderVictory();
                break;
        }
    }
    
    private void renderTitle() {
        gc.setFill(Color.DARKBLUE);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        gc.setFill(Color.WHITE);
        gc.fillText("DUKE ADVENTURE", WINDOW_WIDTH / 2 - 80, 150);
        gc.fillText("Save the together java Kingdom", WINDOW_WIDTH / 2 - 80, 180);
        gc.fillText("[PRESS SPACE TO START]", WINDOW_WIDTH / 2 - 90, 400);
        gc.fillText("Java Forever! C++ Never!", WINDOW_WIDTH / 2 - 90, 480);
    }
    
    private void renderOverworld() {
        // Draw world WITH CAMERA!
        world.render(gc, TILE_SIZE, camera);
        
        // Draw player WITH CAMERA!
        player.render(gc, TILE_SIZE, camera);
        
        // Draw UI overlay (always on top, not affected by camera)
        renderUI();
        
        // Show battle cooldown (debug)
        if (battleCooldown > 0) {
            gc.setFill(Color.YELLOW);
            gc.fillText("Battle cooldown: " + String.format("%.1f", battleCooldown), 10, 50);
        }
    }
    
    private void renderUI() {
        // UI background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WINDOW_WIDTH, 30);
        
        // Health
        gc.setFill(Color.RED);
        gc.fillText("❤ " + player.getHealth() + "/" + player.getMaxHealth(), 10, 20);
        
        // Coins
        gc.setFill(Color.GOLD);
        gc.fillText("💰 " + player.getCoins(), 150, 20);
        
        // Duke count
        gc.setFill(Color.LIGHTBLUE);
        gc.fillText("🦆 " + player.getDukeCount() + " Dukes", 300, 20);
        
        // Controls hint
        gc.setFill(Color.WHITE);
        gc.fillText("[SPACE] Talk  [Q] Quests  [C] Collection", 450, 20);
    }
    
    private void renderBattle() {
        if (currentBattle != null) {
            currentBattle.render(gc, WINDOW_WIDTH, WINDOW_HEIGHT);
        }
    }
    
    private void renderBossBattle() {
        if (bossBattle == null) return;
        
        gc.setFill(Color.DARKRED);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        BossSystem.Boss boss = bossBattle.getBoss();
        int bossX = WINDOW_WIDTH / 2 - 64;
        int bossY = 50;
        
        AnimationSystem bossAnim = boss.getAnimation();
        if (bossAnim != null) {
            javafx.scene.image.Image frame = bossAnim.getCurrentFrame();
            if (frame != null) {
                gc.drawImage(frame, bossX, bossY, 128, 128);
            }
        } else {
            gc.setFill(Color.DARKVIOLET);
            gc.fillRect(bossX, bossY, 128, 128);
        }
        
        gc.setFill(Color.WHITE);
        gc.fillText(boss.getName(), bossX, bossY - 10);
        drawBossHealthBar(gc, bossX, bossY - 25, 128, boss.getHealth(), boss.getMaxHealth());
        
        Duke playerDuke = bossBattle.getPlayerDuke();
        int playerX = WINDOW_WIDTH / 2 - 32;
        int playerY = WINDOW_HEIGHT - 150;
        
        javafx.scene.image.Image playerSprite = playerDuke.getSprite();
        if (playerSprite != null) {
            gc.drawImage(playerSprite, playerX, playerY, 64, 64);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillRect(playerX, playerY, 64, 64);
        }
        
        gc.setFill(Color.WHITE);
        gc.fillText("Your " + playerDuke.getDisplayName(), playerX, playerY - 10);
        drawBossHealthBar(gc, playerX, playerY - 25, 64, playerDuke.getHealth(), playerDuke.getMaxHealth());
        
        gc.setFill(Color.BLACK);
        gc.fillRect(10, WINDOW_HEIGHT - 100, WINDOW_WIDTH - 20, 90);
        gc.setFill(Color.WHITE);
        gc.strokeRect(10, WINDOW_HEIGHT - 100, WINDOW_WIDTH - 20, 90);
        gc.fillText(bossBattle.getBattleLog(), 20, WINDOW_HEIGHT - 70);
        
        if (bossBattle.isPlayerTurn() && bossBattle.isBattleActive()) {
            gc.fillText("[A] Attack  [P] Potion", 20, WINDOW_HEIGHT - 20);
        } else if (bossBattle.isBattleActive()) {
            gc.fillText("[Press SPACE to continue]", 20, WINDOW_HEIGHT - 20);
        } else {
            gc.fillText("[Press SPACE to continue]", 20, WINDOW_HEIGHT - 20);
        }
    }
    
    private void drawBossHealthBar(GraphicsContext gc, int x, int y, int width, int health, int maxHealth) {
        gc.setFill(Color.GRAY);
        gc.fillRect(x, y, width, 10);
        
        double healthPercent = health / (double) maxHealth;
        Color barColor = healthPercent > 0.5 ? Color.GREEN : healthPercent > 0.25 ? Color.YELLOW : Color.RED;
        gc.setFill(barColor);
        gc.fillRect(x, y, width * healthPercent, 10);
        
        gc.setStroke(Color.WHITE);
        gc.strokeRect(x, y, width, 10);
    }
    
    private void renderDialog() {
        renderOverworld();
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        int boxX = 50;
        int boxY = WINDOW_HEIGHT - 200;
        int boxWidth = WINDOW_WIDTH - 100;
        int boxHeight = 150;
        
        gc.setFill(Color.WHITE);
        gc.fillRect(boxX, boxY, boxWidth, boxHeight);
        gc.setFill(Color.BLACK);
        gc.strokeRect(boxX, boxY, boxWidth, boxHeight);
        
        if (currentConversation != null && talkingToNPC != null) {
            gc.fillText(talkingToNPC.getName(), boxX + 10, boxY + 20);
            
            DialogSystem.DialogNode node = currentConversation.currentNode;
            gc.fillText(node.text, boxX + 10, boxY + 50);
            
            if (node.choices != null && !node.choices.isEmpty()) {
                int choiceY = boxY + 90;
                for (int i = 0; i < node.choices.size(); i++) {
                    String choiceKey = i == 0 ? "Q" : i == 1 ? "E" : "C";
                    gc.fillText("[" + choiceKey + "] " + node.choices.get(i).text, 
                              boxX + 20, choiceY + (i * 20));
                }
            } else {
                gc.fillText("[SPACE] Continue", boxX + 10, boxY + 130);
            }
        }
    }
    
    private void renderShop() {
        if (shop != null) {
            shop.render(gc, WINDOW_WIDTH, WINDOW_HEIGHT);
        }
    }
    
    private void renderQuestLog() {
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        gc.setFill(Color.BLACK);
        gc.fillText("=== QUEST LOG ===", 50, 50);
        
        var activeQuests = questSystem.getActiveQuests();
        if (activeQuests.isEmpty()) {
            gc.fillText("No active quests. Talk to NPCs to get quests!", 50, 100);
        } else {
            int y = 100;
            for (Quest quest : activeQuests) {
                gc.fillText("• " + quest.getName(), 50, y);
                gc.fillText("  " + quest.getDescription(), 70, y + 15);
                gc.fillText("  Progress: " + quest.getProgressString(), 70, y + 30);
                y += 60;
            }
        }
        
        gc.fillText("[ESC] Close", 50, WINDOW_HEIGHT - 30);
    }
    
    private void renderCollection() {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        gc.setFill(Color.BLACK);
        gc.fillText("=== YOUR DUKE COLLECTION ===", 50, 50);
        gc.fillText("Total: " + player.getDukeCount() + " Dukes", 50, 80);
        
        var dukes = player.getDukes();
        int y = 120;
        int x = 50;
        for (int i = 0; i < dukes.size(); i++) {
            Duke duke = dukes.get(i);
            
            javafx.scene.image.Image sprite = duke.getSprite();
            if (sprite != null) {
                gc.drawImage(sprite, x, y, 48, 48);
            } else {
                gc.setFill(Color.CYAN);
                gc.fillRect(x, y, 48, 48);
            }
            
            gc.setFill(Color.BLACK);
            gc.fillText(duke.getDisplayName(), x + 60, y + 15);
            gc.fillText("Power: " + duke.getPower(), x + 60, y + 30);
            gc.fillText("HP: " + duke.getHealth() + "/" + duke.getMaxHealth(), x + 60, y + 45);
            
            y += 70;
            if (y > WINDOW_HEIGHT - 100) {
                y = 120;
                x += 250;
            }
        }
        
        gc.setFill(Color.BLACK);
        gc.fillText("[ESC] Close", 50, WINDOW_HEIGHT - 30);
    }
    
    private void renderVictory() {
        gc.setFill(Color.GOLD);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        gc.setFill(Color.BLACK);
        gc.fillText("🎉 VICTORY! 🎉", WINDOW_WIDTH / 2 - 60, 150);
        gc.fillText("You defeated the C++ Dragon!", WINDOW_WIDTH / 2 - 100, 200);
        gc.fillText("Java is the supreme language!", WINDOW_WIDTH / 2 - 100, 230);
        gc.fillText("", WINDOW_WIDTH / 2 - 100, 280);
        gc.fillText("Thanks for playing Duke Adventure!", WINDOW_WIDTH / 2 - 120, 320);
        gc.fillText("Made by barshERROR(404)", WINDOW_WIDTH / 2 - 90, 350);
        gc.fillText("For Duke Game Jam 2026", WINDOW_WIDTH / 2 - 80, 380);
    }
    
    private void handleKeyPress(KeyEvent e) {
        KeyCode code = e.getCode();
        
        switch (currentState) {
            case TITLE:
                if (code == KeyCode.SPACE) {
                    currentState = GameState.OVERWORLD;
                }
                break;
                
            case OVERWORLD:
                handleOverworldInput(code);
                break;
                
            case BATTLE:
                handleBattleInput(code);
                break;
                
            case BOSS_BATTLE:
                handleBossBattleInput(code);
                break;
                
            case DIALOG:
                handleDialogInput(code);
                break;
                
            case SHOP:
                handleShopInput(code);
                break;
                
            case QUEST_LOG:
            case COLLECTION:
                if (code == KeyCode.ESCAPE) {
                    currentState = GameState.OVERWORLD;
                }
                break;
                
            case VICTORY:
                break;
        }
    }
    
    private void handleOverworldInput(KeyCode code) {
        switch (code) {
            case W:
            case UP:
                player.move(0, -1, world);
                break;
            case S:
            case DOWN:
                player.move(0, 1, world);
                break;
            case A:
            case LEFT:
                player.move(-1, 0, world);
                break;
            case D:
            case RIGHT:
                player.move(1, 0, world);
                break;
            case SPACE:
                NPC npc = world.getAdjacentNPC(player.getGridX(), player.getGridY());
                if (npc != null) {
                    if (npc.getName().equals("TJ-Bot")) {
                        shop = new ShopSystem(player);
                        currentState = GameState.SHOP;
                    } else {
                        startConversation(npc);
                    }
                }
                break;
            case Q:
                currentState = GameState.QUEST_LOG;
                break;
            case C:
                currentState = GameState.COLLECTION;
                break;
        }
    }
    
    private void handleBattleInput(KeyCode code) {
        if (!currentBattle.isActive()) {
            if (code == KeyCode.SPACE) {
                if (currentBattle.playerWon()) {
                    player.addDuke(currentBattle.getWildDuke());
                    player.winBattle();
                    
                    questSystem.updateDukeCollectionQuest(
                        currentBattle.getWildDuke().getType(),
                        player.getDukeCount()
                    );
                    questSystem.updateQuestProgress(Quest.QuestType.WIN_BATTLES, 1);
                }
                
                // START BATTLE COOLDOWN (FIX!)
                battleCooldown = BATTLE_COOLDOWN_TIME;
                
                currentBattle = null;
                currentState = GameState.OVERWORLD;
            }
        } else if (currentBattle.isPlayerTurn()) {
            switch (code) {
                case A:
                    currentBattle.playerAttack();
                    break;
                case E:
                    // Escape battle - just exit
                    battleCooldown = BATTLE_COOLDOWN_TIME;
                    currentBattle = null;
                    currentState = GameState.OVERWORLD;
                    System.out.println("Got away safely!");
                    break;
                case P:
                    // Just use potion - BattleSystem checks inventory!
                    currentBattle.usePotion();
                    break;
            }
        } else {
            if (code == KeyCode.SPACE) {
                currentBattle.enemyAttack();
            }
        }
    }
    
    private void handleBossBattleInput(KeyCode code) {
        if (bossBattle == null) return;
        
        if (!bossBattle.isBattleActive()) {
            if (code == KeyCode.SPACE) {
                if (bossBattle.getBoss().isDefeated()) {
                    world.defeatBoss();
                    questSystem.updateQuestProgress(Quest.QuestType.DEFEAT_BOSS, 1);
                    currentState = GameState.VICTORY;
                } else {
                    currentState = GameState.OVERWORLD;
                }
                bossBattle = null;
            }
        } else if (bossBattle.isPlayerTurn()) {
            switch (code) {
                case A:
                    bossBattle.playerAttack();
                    break;
                case P:
                    if (player.spendCoins(50)) {
                        bossBattle.usePotion(30);
                    }
                    break;
            }
        } else {
            if (code == KeyCode.SPACE) {
                bossBattle.bossAttack();
            }
        }
    }
    
    private void handleDialogInput(KeyCode code) {
        if (currentConversation == null) return;
        
        DialogSystem.DialogNode node = currentConversation.currentNode;
        
        if (node.choices != null && !node.choices.isEmpty()) {
            int choiceIndex = -1;
            if (code == KeyCode.Q) choiceIndex = 0;
            else if (code == KeyCode.E) choiceIndex = 1;
            else if (code == KeyCode.C && node.choices.size() > 2) choiceIndex = 2;
            
            if (choiceIndex != -1 && choiceIndex < node.choices.size()) {
                currentConversation.selectChoice(choiceIndex);
                
                if (currentConversation.currentNode.questId != null) {
                    questSystem.startQuest(currentConversation.currentNode.questId);
                }
                
                if (!currentConversation.isActive) {
                    player.talkToNPC(talkingToNPC.getName());
                    questSystem.updateQuestProgress(Quest.QuestType.TALK_TO_NPC, 1);
                    currentState = GameState.OVERWORLD;
                    currentConversation = null;
                    talkingToNPC = null;
                }
            }
        } else {
            if (code == KeyCode.SPACE) {
                currentState = GameState.OVERWORLD;
                if (talkingToNPC != null) {
                    player.talkToNPC(talkingToNPC.getName());
                    questSystem.updateQuestProgress(Quest.QuestType.TALK_TO_NPC, 1);
                }
                currentConversation = null;
                talkingToNPC = null;
            }
        }
    }
    
    private void handleShopInput(KeyCode code) {
        if (code == KeyCode.ESCAPE) {
            currentState = GameState.OVERWORLD;
            shop = null;
        } else if (code == KeyCode.DIGIT1) {
            shop.buyItem(0);
        } else if (code == KeyCode.DIGIT2) {
            shop.buyItem(1);
        } else if (code == KeyCode.DIGIT3) {
            shop.buyItem(2);
        } else if (code == KeyCode.DIGIT4) {
            shop.buyItem(3);
        }
    }
    
    private void handleKeyRelease(KeyEvent e) {
        // Not used
    }
    
    private void startWildBattle() {
        Duke wildDuke = Duke.createWildDuke();
        Duke playerDuke = player.getStrongestDuke();
        
        if (playerDuke != null) {
            currentBattle = new BattleSystem(player, wildDuke);
            currentState = GameState.BATTLE;
            System.out.println("Wild " + wildDuke.getDisplayName() + " appeared!");
        }
    }
    
    private void startBossBattle() {
        Duke playerDuke = player.getStrongestDuke();
        if (playerDuke != null) {
            bossBattle = new BossSystem.BossBattle(playerDuke);
            currentState = GameState.BOSS_BATTLE;
            System.out.println("BOSS BATTLE: C++ Dragon!");
        }
    }
    
    private void startConversation(NPC npc) {
        talkingToNPC = npc;
        DialogSystem.DialogNode startNode = npc.getDialogue(questSystem);
        currentConversation = new DialogSystem.Conversation(npc.getName(), startNode);
        currentState = GameState.DIALOG;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
