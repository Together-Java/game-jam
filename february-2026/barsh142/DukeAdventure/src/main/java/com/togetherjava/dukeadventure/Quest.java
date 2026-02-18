package com.togetherjava.dukeadventure;

public class Quest {
    public boolean isCompleted() {
        return false;
    }

    public enum QuestType {
        COLLECT_DUKES,  // Collect X Dukes of type Y
        WIN_BATTLES,    // Win X battles
        TALK_TO_NPC,    // Talk to specific NPC
        REACH_LEVEL,    // Collect X total Dukes
        DEFEAT_BOSS     // Defeat the C++ Dragon
    }
    
    public enum QuestStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        CLAIMED
    }
    
    private String id;
    private String name;
    private String description;
    private QuestType type;
    private QuestStatus status;
    
    // Quest requirements
    private int targetCount;
    private int currentCount;
    private String targetType; // For COLLECT_DUKES
    
    // Rewards
    private int coinReward;
    private String itemReward;
    
    // Quest giver
    private String giverName;
    
    public Quest(String id, String name, String description, String giverName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.giverName = giverName;
        this.status = QuestStatus.NOT_STARTED;
        this.currentCount = 0;
        this.targetCount = 1;
        this.coinReward = 0;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public QuestStatus getStatus() { return status; }
    public int getTargetCount() { return targetCount; }
    public int getCurrentCount() { return currentCount; }
    public String getTargetType() { return targetType; }
    public int getCoinReward() { return coinReward; }
    public String getItemReward() { return itemReward; }
    public String getGiverName() { return giverName; }
    
    public void setType(QuestType type) { this.type = type; }
    public void setStatus(QuestStatus status) { this.status = status; }
    public void setTargetCount(int count) { this.targetCount = count; }
    public void setCurrentCount(int count) { this.currentCount = count; }
    public void setTargetType(String type) { this.targetType = type; }
    public void setCoinReward(int coins) { this.coinReward = coins; }
    public void setItemReward(String item) { this.itemReward = item; }

    public void incrementProgress(int amount) {
        if (status == QuestStatus.IN_PROGRESS) {
            currentCount += amount;
            if (currentCount >= targetCount) {
                status = QuestStatus.COMPLETED;
                System.out.println("✓ Quest completed: " + name);
            }
        }
    }

    public boolean isComplete() {
        return status == QuestStatus.COMPLETED;
    }

    public boolean canClaim() {
        return status == QuestStatus.COMPLETED;
    }

    public String getProgressString() {
        return currentCount + "/" + targetCount;
    }

    public int getProgressPercent() {
        if (targetCount == 0) return 0;
        return (int)((currentCount / (double)targetCount) * 100);
    }
}
