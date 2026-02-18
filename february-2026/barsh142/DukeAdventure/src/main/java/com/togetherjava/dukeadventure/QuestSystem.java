package com.togetherjava.dukeadventure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestSystem {
    private Map<String, Quest> allQuests;
    private List<Quest> activeQuests;
    private List<Quest> completedQuests;
    
    public QuestSystem() {
        allQuests = new HashMap<>();
        activeQuests = new ArrayList<>();
        completedQuests = new ArrayList<>();
        
        initializeQuests();
    }
    
    /**
     * Initialize all quests in the game
     */
    private void initializeQuests() {
        // Quest 1: Zabuzard's Welcome Quest
        Quest welcomeQuest = new Quest(
            "welcome",
            "Welcome to Duke Adventure!",
            "Zabuzard wants you to catch your first Duke. Find tall grass and catch any Duke!",
            "Zabuzard"
        );
        welcomeQuest.setType(Quest.QuestType.COLLECT_DUKES);
        welcomeQuest.setTargetCount(1);
        welcomeQuest.setCoinReward(50);
        allQuests.put("welcome", welcomeQuest);
        
        // Quest 2: Wazei's Rare Duke Challenge
        Quest rareQuest = new Quest(
            "rare_duke",
            "Rare Duke Hunt",
            "Wazei challenges you to catch a Rare Duke! They have wings and are harder to find.",
            "Wazei"
        );
        rareQuest.setType(Quest.QuestType.COLLECT_DUKES);
        rareQuest.setTargetCount(1);
        rareQuest.setTargetType("rare");
        rareQuest.setCoinReward(100);
        allQuests.put("rare_duke", rareQuest);
        
        // Quest 3: FirasG's Coffee Quest
        Quest coffeeQuest = new Quest(
            "coffee_duke",
            "Need My Coffee!",
            "FirasG is tired and needs coffee. Catch a Coffee Duke for him!",
            "FirasG"
        );
        coffeeQuest.setType(Quest.QuestType.COLLECT_DUKES);
        coffeeQuest.setTargetCount(1);
        coffeeQuest.setTargetType("coffee");
        coffeeQuest.setCoinReward(75);
        allQuests.put("coffee_duke", coffeeQuest);
        
        // Quest 4: Christolis's Git Expert Quest
        Quest gitQuest = new Quest(
            "git_master",
            "Become a Duke Master",
            "Christolis wants you to prove yourself. Collect 5 different Dukes!",
            "Christolis"
        );
        gitQuest.setType(Quest.QuestType.REACH_LEVEL);
        gitQuest.setTargetCount(5);
        gitQuest.setCoinReward(150);
        allQuests.put("git_master", gitQuest);
        
        // Quest 5: Number1Engineer's Battle Quest
        Quest battleQuest = new Quest(
            "battle_master",
            "Battle Training",
            "Number1Engineer wants you to get battle experience. Win 3 battles!",
            "Number1Engineer"
        );
        battleQuest.setType(Quest.QuestType.WIN_BATTLES);
        battleQuest.setTargetCount(3);
        battleQuest.setCoinReward(100);
        allQuests.put("battle_master", battleQuest);
        
        // Quest 6: AlphaBee's Community Quest
        Quest communityQuest = new Quest(
            "meet_everyone",
            "Meet the Community",
            "AlphaBee wants you to meet everyone. Talk to 5 different NPCs!",
            "AlphaBee"
        );
        communityQuest.setType(Quest.QuestType.TALK_TO_NPC);
        communityQuest.setTargetCount(5);
        communityQuest.setCoinReward(80);
        allQuests.put("meet_everyone", communityQuest);
        
        // FINAL QUEST: Defeat C++ Dragon
        Quest bossQuest = new Quest(
            "defeat_dragon",
            "The Final Battle",
            "The evil C++ Dragon threatens the Java kingdom! You must defeat it! \n" +
            "Find it in the northeast corner of the map when you have at least 3 Dukes.",
            "Zabuzard"
        );
        bossQuest.setType(Quest.QuestType.DEFEAT_BOSS);
        bossQuest.setTargetCount(1);
        bossQuest.setCoinReward(500);
        bossQuest.setItemReward("Victory Crown");
        allQuests.put("defeat_dragon", bossQuest);
    }

    public void startQuest(String questId) {
        Quest quest = allQuests.get(questId);
        if (quest != null && quest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            quest.setStatus(Quest.QuestStatus.IN_PROGRESS);
            activeQuests.add(quest);
            System.out.println("✓ Quest started: " + quest.getName());
        }
    }

    public void updateQuestProgress(Quest.QuestType type, int amount) {
        for (Quest quest : activeQuests) {
            if (quest.getType() == type && quest.getStatus() == Quest.QuestStatus.IN_PROGRESS) {
                quest.incrementProgress(amount);
            }
        }
    }

    public void updateDukeCollectionQuest(String dukeType, int totalDukesCollected) {
        for (Quest quest : activeQuests) {
            if (quest.getStatus() != Quest.QuestStatus.IN_PROGRESS) continue;
            
            if (quest.getType() == Quest.QuestType.COLLECT_DUKES) {
                // If quest has specific target type
                if (quest.getTargetType() != null && quest.getTargetType().equals(dukeType)) {
                    quest.incrementProgress(1);
                }
                // If quest just needs any Duke
                else if (quest.getTargetType() == null) {
                    quest.incrementProgress(1);
                }
            }
            
            // Check level-up quests (total collection)
            if (quest.getType() == Quest.QuestType.REACH_LEVEL) {
                quest.setCurrentCount(totalDukesCollected);
                if (totalDukesCollected >= quest.getTargetCount()) {
                    quest.setStatus(Quest.QuestStatus.COMPLETED);
                }
            }
        }
    }

    public int claimQuest(String questId, Player player) {
        Quest quest = allQuests.get(questId);
        if (quest != null && quest.canClaim()) {
            quest.setStatus(Quest.QuestStatus.CLAIMED);
            activeQuests.remove(quest);
            completedQuests.add(quest);
            
            // Give rewards
            player.addCoins(quest.getCoinReward());
            
            System.out.println("✓ Quest claimed: " + quest.getName());
            System.out.println("  Reward: " + quest.getCoinReward() + " coins");
            
            return quest.getCoinReward();
        }
        return 0;
    }

    public Quest getQuest(String questId) {
        return allQuests.get(questId);
    }

    public List<Quest> getActiveQuests() {
        return new ArrayList<>(activeQuests);
    }

    public List<Quest> getCompletedQuests() {
        return new ArrayList<>(completedQuests);
    }

    public boolean isQuestActive(String questId) {
        Quest quest = allQuests.get(questId);
        return quest != null && quest.getStatus() == Quest.QuestStatus.IN_PROGRESS;
    }

    public void claimReward(String questId, Player player) {
        Quest quest = allQuests.get(questId);
        if (quest != null && quest.isCompleted() && quest.getStatus() != Quest.QuestStatus.CLAIMED) {
            // Give coins
            player.addCoins(quest.getCoinReward());
            
            // Give item if any
            if (quest.getItemReward() != null && !quest.getItemReward().isEmpty()) {
                player.addItem(quest.getItemReward(), 1);
            }
            
            // Mark as claimed
            quest.setStatus(Quest.QuestStatus.CLAIMED);
            
            System.out.println("🎉 Quest complete! +" + quest.getCoinReward() + " coins!");
            if (quest.getItemReward() != null) {
                System.out.println("  + " + quest.getItemReward());
            }
        }
    }

    public boolean isQuestCompleted(String questId) {
        Quest quest = allQuests.get(questId);
        return quest != null && quest.getStatus() == Quest.QuestStatus.COMPLETED;
    }
}
