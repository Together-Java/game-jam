package com.togetherjava.dukeadventure;

import javafx.scene.image.Image;

public class BossSystem {
    
    public static class Boss {
        private String name;
        private int health;
        private int maxHealth;
        private int attackPower;
        private boolean defeated;
        private Image sprite;
        private AnimationSystem animation;
        
        public Boss() {
            this.name = "C++ Dragon";
            this.maxHealth = 250;  // Much stronger than normal Dukes!
            this.health = maxHealth;
            this.attackPower = 35;
            this.defeated = false;
            
            // Load boss sprite
            loadSprite();
        }
        
        private void loadSprite() {
            Image bossImage = ImageLoader.loadBoss("cpp_dragon.png");
            
            if (bossImage != null) {
                // Check if it's a sprite sheet (4 frames for flying animation)
                if (bossImage.getWidth() >= 512) {
                    // It's a sprite sheet! Create flying animation
                    this.animation = new AnimationSystem(bossImage, 4, 128, 128, 0.2);
                } else {
                    // Single image
                    this.animation = AnimationSystem.fromSingleImage(bossImage);
                }
                this.sprite = bossImage;
            }
        }
        
        public void takeDamage(int damage) {
            health -= damage;
            if (health <= 0) {
                health = 0;
                defeated = true;
            }
            System.out.println(name + " takes " + damage + " damage! HP: " + health + "/" + maxHealth);
        }
        
        public int attack() {
            int damage = attackPower + (int)(Math.random() * 15); // Random 35-50 damage
            return damage;
        }
        
        public void updateAnimation(double deltaTime) {
            if (animation != null) {
                animation.update(deltaTime);
            }
        }
        
        // Getters
        public String getName() { return name; }
        public int getHealth() { return health; }
        public int getMaxHealth() { return maxHealth; }
        public int getAttackPower() { return attackPower; }
        public boolean isDefeated() { return defeated; }
        public Image getSprite() { return sprite; }
        public AnimationSystem getAnimation() { return animation; }
    }
    
    /**
     * Boss battle state
     */
    public static class BossBattle {
        private Boss boss;
        private Duke playerDuke;
        private boolean playerTurn;
        private boolean battleActive;
        private String battleLog;
        
        public BossBattle(Duke playerDuke) {
            this.boss = new Boss();
            this.playerDuke = playerDuke;
            this.playerTurn = true;
            this.battleActive = true;
            this.battleLog = "The C++ Dragon appears! \nThis is the final battle!";
        }
        
        /**
         * Player attacks the boss
         */
        public void playerAttack() {
            if (!playerTurn || !battleActive) return;
            
            int damage = playerDuke.getPower() + (int)(Math.random() * 15);
            boss.takeDamage(damage);
            battleLog = playerDuke.getType() + " Duke attacks for " + damage + " damage!";
            
            if (boss.isDefeated()) {
                battleLog += "\n\nYOU DEFEATED THE C++ DRAGON! \nJava reigns supreme!";
                battleActive = false;
                return;
            }
            
            playerTurn = false;
        }
        
        /**
         * Boss attacks the player
         */
        public void bossAttack() {
            if (playerTurn || !battleActive) return;
            
            int damage = boss.attack();
            playerDuke.takeDamage(damage);
            battleLog += "\n" + boss.getName() + " attacks for " + damage + " damage!";
            
            if (playerDuke.getHealth() <= 0) {
                battleLog += "\n\nYour Duke fainted! \nYou blacked out...";
                battleActive = false;
                return;
            }
            
            playerTurn = true;
        }
        
        /**
         * Use a healing item
         */
        public void usePotion(int healAmount) {
            if (!playerTurn || !battleActive) return;
            
            playerDuke.heal(healAmount);
            battleLog = "You used a potion! Healed " + healAmount + " HP!";
            
            playerTurn = false;
        }

        public void update(double deltaTime) {
            boss.updateAnimation(deltaTime);
        }
        
        // Getters
        public Boss getBoss() { return boss; }
        public Duke getPlayerDuke() { return playerDuke; }
        public boolean isPlayerTurn() { return playerTurn; }
        public boolean isBattleActive() { return battleActive; }
        public String getBattleLog() { return battleLog; }
    }
    
    /**
     * Check if player is ready to fight the boss
     */
    public static boolean canChallengeBoss(Player player) {
        // Need at least 3 Dukes to challenge the boss
        return player.getDukeCount() >= 3;
    }
    
    /**
     * Get boss encounter message
     */
    public static String getBossIntroMessage() {
        return "A massive shadow looms overhead! \n" +
               "The legendary C++ Dragon descends! \n\n" +
               "\"C++ IS SUPERIOR!\" it roars. \n\n" +
               "Defend the honor of Java!";
    }
}
