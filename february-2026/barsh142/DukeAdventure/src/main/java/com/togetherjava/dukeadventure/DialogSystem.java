package com.togetherjava.dukeadventure;

import java.util.ArrayList;
import java.util.List;

public class DialogSystem {
    
    /**
     * Represents one line of dialogue and the choices that follow
     */
    public static class DialogNode {
        public String text;
        public List<DialogChoice> choices;
        public String questId;
        public boolean endsConversation;
        
        public DialogNode(String text) {
            this.text = text;
            this.choices = new ArrayList<>();
            this.endsConversation = false;
        }
        
        public DialogNode addChoice(String choiceText, String responseText) {
            choices.add(new DialogChoice(choiceText, new DialogNode(responseText)));
            return this;
        }
        
        public DialogNode addQuestChoice(String choiceText, String questId) {
            DialogNode responseNode = new DialogNode("Great! I knew I could count on you!");
            responseNode.questId = questId;
            responseNode.endsConversation = true;
            choices.add(new DialogChoice(choiceText, responseNode));
            return this;
        }
        
        public DialogNode setEnding() {
            this.endsConversation = true;
            return this;
        }
    }
    
    public static class DialogChoice {
        public String text;
        public DialogNode nextNode;
        
        public DialogChoice(String text, DialogNode nextNode) {
            this.text = text;
            this.nextNode = nextNode;
        }
    }
    
    public static class Conversation {
        public String npcName;
        public DialogNode currentNode;
        public boolean isActive;
        
        public Conversation(String npcName, DialogNode startNode) {
            this.npcName = npcName;
            this.currentNode = startNode;
            this.isActive = true;
        }
        
        public void selectChoice(int choiceIndex) {
            if (choiceIndex >= 0 && choiceIndex < currentNode.choices.size()) {
                DialogChoice choice = currentNode.choices.get(choiceIndex);
                currentNode = choice.nextNode;
                
                if (currentNode.endsConversation) {
                    isActive = false;
                }
            }
        }
    }
    
    // ========== ZABUZARD (Top Helper - Main Quest Giver) ==========
    public static DialogNode createZabuzardDialogue(QuestSystem questSystem) {
        Quest welcomeQuest = questSystem.getQuest("welcome");
        Quest bossQuest = questSystem.getQuest("defeat_dragon");
        
        if (bossQuest != null && bossQuest.getStatus() == Quest.QuestStatus.CLAIMED) {
            DialogNode greeting = new DialogNode(
                "You defeated the C++ Dragon! \n" +
                "The Java kingdom owes you everything! \n" +
                "You're a true Duke Master now!"
            );
            greeting.setEnding();
            return greeting;
        }
        else if (welcomeQuest != null && welcomeQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "Welcome, new trainer! I'm Zabuzard, the Top Helper here. \n" +
                "I've been catching Dukes for years. Ready to start?"
            );
            greeting.addChoice("What are Dukes?", 
                "Dukes are magical creatures born from pure Java code! \n" +
                "They live in tall grass and become your companions in battle. \n" +
                "Want to catch your first one?")
                .addQuestChoice("Yes! Give me a quest!", "welcome")
                .addChoice("Maybe later", "Come back when you're ready to become a Duke Master!");
            return greeting;
        }
        else if (bossQuest != null && bossQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "Listen carefully... \n" +
                "An ancient evil has awakened - the C++ Dragon! \n" +
                "It threatens to corrupt our Java kingdom. Will you face it?"
            );
            greeting.addQuestChoice("I'll stop it!", "defeat_dragon");
            greeting.addChoice("I need more training", "Wise choice. Gather strong Dukes first.");
            return greeting;
        }
        else {
            DialogNode greeting = new DialogNode(
                "You're making great progress! \n" +
                "Remember: tall grass = Dukes. Keep exploring!"
            );
            greeting.setEnding();
            return greeting;
        }
    }
    
    // ========== WAZEI (Admin - Rare Duke Quest) ==========
    public static DialogNode createWazeiDialogue(QuestSystem questSystem) {
        Quest rareQuest = questSystem.getQuest("rare_duke");
        
        if (rareQuest != null && rareQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "Ah, a rising star! I'm Wazei, admin here. \n" +
                "I keep things running smoothly... and I love rare Dukes! \n" +
                "Ever seen one with wings?"
            );
            greeting.addChoice("Tell me about rare Dukes", 
                "Rare Dukes have beautiful wings and stronger power! \n" +
                "They're harder to find but worth it. Think you can catch one?")
                .addQuestChoice("Challenge accepted!", "rare_duke")
                .addChoice("Sounds hard", "They are! But I believe in you. Come back when ready.");
            return greeting;
        }
        else if (rareQuest != null && rareQuest.isComplete()) {
            DialogNode greeting = new DialogNode(
                "You caught a Rare Duke! Excellent work! \n" +
                "With dedication like yours, you'll go far."
            );
            greeting.setEnding();
            return greeting;
        }
        else {
            DialogNode greeting = new DialogNode(
                "As admin, I make sure everyone has a good time here. \n" +
                "Keep up the good work, trainer!"
            );
            greeting.setEnding();
            return greeting;
        }
    }
    
    // ========== MARKO (Admin - Professional) ==========
    public static DialogNode createMarkoDialogue() {
        DialogNode greeting = new DialogNode(
            "Greetings! I'm Marko, one of the admins. \n" +
            "Everything in order with your Duke journey?"
        );
        greeting.addChoice("What do admins do?",
            "We maintain server stability and help resolve issues. \n" +
            "Think of us as the backbone of Together Java! \n" +
            "If you ever need help, we're here.")
            .setEnding();
        greeting.addChoice("Any training tips?",
            "Organization is key! Keep track of your Dukes' types. \n" +
            "Different Dukes have different strengths. \n" +
            "Build a balanced team!")
            .setEnding();
        return greeting;
    }
    
    // ========== ADI (Moderator - Helpful) ==========
    public static DialogNode createAdiDialogue() {
        DialogNode greeting = new DialogNode(
            "Hey there! Adi here, moderator of this fine server. \n" +
            "How's your Duke collection coming along?"
        );
        greeting.addChoice("What's a moderator?",
            "We help keep the community friendly and organized! \n" +
            "Think of us as guides who make sure everyone's having fun. \n" +
            "We also answer questions and help new members!")
            .setEnding();
        greeting.addChoice("Where can I find rare Dukes?",
            "Rare Dukes are in tall grass, but they're... well, rare! \n" +
            "Keep searching and you'll find them eventually. \n" +
            "Patience is key!")
            .setEnding();
        return greeting;
    }
    
    // ========== SOLUCKYSEVEN (Smart Member - Enthusiastic) ==========
    public static DialogNode createSoLuckySevenDialogue() {
        DialogNode greeting = new DialogNode(
            "Yo! I'm SoLuckySeven! I know my stuff! \n" +
            "Did you know Duke types have different power levels?"
        );
        greeting.addChoice("Tell me about Duke types!",
            "Common Dukes are easiest to find but weakest. \n" +
            "Then Rare, Dev, Coffee, Gold, and RAINBOW! \n" +
            "Rainbow Duke is legendary - only 1% spawn rate!")
            .setEnding();
        greeting.addChoice("What's your favorite Duke?",
            "Dev Duke! It has headphones! So cool! \n" +
            "Plus it's strong in battles. Power level 40! \n" +
            "Way better than Common Duke's measly 10!")
            .setEnding();
        return greeting;
    }
    
    // ========== FIRASG (Legend Contributor - Coffee Quest) ==========
    public static DialogNode createFirasGDialogue(QuestSystem questSystem) {
        Quest coffeeQuest = questSystem.getQuest("coffee_duke");
        
        if (coffeeQuest != null && coffeeQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "*yawns* \n" +
                "I'm FirasG... legendary contributor... but so tired... \n" +
                "I REALLY need coffee..."
            );
            greeting.addChoice("You okay?",
                "I've been coding for 12 hours straight... \n" +
                "If only there was a Coffee Duke around here... \n" +
                "Wait... you could find one for me!")
                .addQuestChoice("I'll find you a Coffee Duke!", "coffee_duke")
                .addChoice("Get some sleep dude", "Sleep is for the weak! Code is life!");
            return greeting;
        }
        else if (coffeeQuest != null && coffeeQuest.isComplete()) {
            DialogNode greeting = new DialogNode(
                "YOU GOT A COFFEE DUKE! *extremely happy* \n" +
                "I can feel the energy already! \n" +
                "Time to write more code! Thank you!"
            );
            greeting.setEnding();
            return greeting;
        }
        else {
            DialogNode greeting = new DialogNode(
                "Coffee fuels my contributions! \n" +
                "Also Java. Java is life. Not the coffee, the language. \n" +
                "...both, actually."
            );
            greeting.setEnding();
            return greeting;
        }
    }

    public static DialogNode createAlathreonDialogue() {
        DialogNode greeting = new DialogNode(
            "Greetings, traveler. I am Alathreon, moderator and observer. \n" +
            "The path of the Duke Master is not easy..."
        );
        greeting.addChoice("Tell me your wisdom",
            "Patience and persistence are the keys to mastery. \n" +
            "Do not rush. Enjoy the journey of catching Dukes. \n" +
            "Each battle teaches you something new.")
            .setEnding();
        greeting.addChoice("What's the hardest challenge?",
            "The C++ Dragon in the northeast corner... \n" +
            "It is the ultimate test of a Duke Master. \n" +
            "Prepare well before facing it.")
            .setEnding();
        return greeting;
    }

    public static DialogNode createSquidxTVDialogue() {
        DialogNode greeting = new DialogNode(
            "YOOO! SquidxTV here! Let's GOOOO! \n" +
            "You ready to catch some Dukes?! This is HYPE!"
        );
        greeting.addChoice("You're very energetic...",
            "YEAH! I LOVE THIS SERVER! SO MUCH FUN! \n" +
            "Duke hunting is AMAZING! The battles! The strategy! \n" +
            "IT'S ALL SO GOOD!")
            .setEnding();
        greeting.addChoice("Best Duke battle strategy?",
            "ATTACK! ATTACK! ATTACK! \n" +
            "Jk jk, use potions when low HP! \n" +
            "And catch strong Dukes! Rainbow Duke is INSANE!")
            .setEnding();
        return greeting;
    }

    public static DialogNode createchristolisDialogue(QuestSystem questSystem) {
        Quest gitQuest = questSystem.getQuest("git_master");
        
        if (gitQuest != null && gitQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "Hey! Christolis here - Community Ambassador and Git expert. \n" +
                "I see potential in you. Want to prove yourself?"
            );
            greeting.addChoice("How do I prove myself?",
                "Collect 5 different Dukes! Show me you can build a team! \n" +
                "It's like managing a good Git repository - diversity matters! \n" +
                "Think you can do it?")
                .addQuestChoice("I'll collect 5 Dukes!", "git_master")
                .addChoice("That sounds hard", "It is! But that's what makes it worth it!");
            return greeting;
        }
        else if (gitQuest != null && gitQuest.isComplete()) {
            DialogNode greeting = new DialogNode(
                "5 different Dukes! Well done! \n" +
                "You've built a solid team. Like a well-maintained repo!"
            );
            greeting.setEnding();
            return greeting;
        }
        else {
            DialogNode greeting = new DialogNode(
                "Git gud! ...I mean, get good at catching Dukes! \n" +
                "Git puns are my specialty."
            );
            greeting.setEnding();
            return greeting;
        }
    }

    public static DialogNode createBarshErrorDialogue() {
        DialogNode greeting = new DialogNode(
            "Wait... you're me? I'm you? We're... us? \n" +
            "This is getting confusing. Am I an NPC in my own game?!"
        );
        greeting.addChoice("This is trippy",
            "RIGHT?! It's like looking in a mirror! \n" +
            "Anyway, good luck with the Game Jam! \n" +
            "We're gonna win this thing!")
            .setEnding();
        greeting.addChoice("Are you real?",
            "As real as any NPC can be! \n" +
            "I'm the developer AND the character! \n" +
            "Meta, right? Now go catch some Dukes!")
            .setEnding();
        return greeting;
    }

    public static DialogNode createNumber1EngineerDialogue(QuestSystem questSystem) {
        Quest battleQuest = questSystem.getQuest("battle_master");
        
        if (battleQuest != null && battleQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "Number1Engineer here! I'm all about practical experience. \n" +
                "Theory is useless without practice! Ready to train?"
            );
            greeting.addChoice("What kind of training?",
                "BATTLES! Win 3 battles to prove your skill! \n" +
                "Don't just catch Dukes - learn to USE them! \n" +
                "Ready for the challenge?")
                .addQuestChoice("Let's do it!", "battle_master")
                .addChoice("I'm not ready yet", "Come back when you are! Training waits for no one!");
            return greeting;
        }
        else if (battleQuest != null && battleQuest.isComplete()) {
            DialogNode greeting = new DialogNode(
                "3 battles won! Now THAT'S engineering! \n" +
                "You've got practical skills now, not just theory!"
            );
            greeting.setEnding();
            return greeting;
        }
        else {
            DialogNode greeting = new DialogNode(
                "Engineering is about solving real problems! \n" +
                "Same with Duke battles - adapt and overcome!"
            );
            greeting.setEnding();
            return greeting;
        }
    }

    public static DialogNode createAlphaBeeDialogue(QuestSystem questSystem) {
        Quest communityQuest = questSystem.getQuest("meet_everyone");
        
        if (communityQuest != null && communityQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            DialogNode greeting = new DialogNode(
                "Hi! I'm AlphaBee, Community Ambassador! \n" +
                "I make sure everyone feels welcome here! \n" +
                "Have you met everyone yet?"
            );
            greeting.addChoice("Not everyone yet",
                "That's what I thought! Together Java is all about community! \n" +
                "How about you talk to 5 different people? \n" +
                "Get to know the community!")
                .addQuestChoice("Sounds fun!", "meet_everyone")
                .addChoice("I'm shy", "Don't be! Everyone here is friendly! Take your time!");
            return greeting;
        }
        else if (communityQuest != null && communityQuest.isComplete()) {
            DialogNode greeting = new DialogNode(
                "You talked to 5 people! That's the spirit! \n" +
                "Community is what makes Together Java special!"
            );
            greeting.setEnding();
            return greeting;
        }
        else {
            DialogNode greeting = new DialogNode(
                "Being part of a community is awesome! \n" +
                "We all help each other learn and grow!"
            );
            greeting.setEnding();
            return greeting;
        }
    }

    public static DialogNode createGlitchedDukeDialogue() {
        DialogNode greeting = new DialogNode(
            "01001000 01001001... *static* \n" +
            "I AM... G̴L̸I̷T̶C̵H̶E̵D̸... DUKE... \n" +
            "THE... CODE... IS... *bzzt* ...BROKEN..."
        );
        greeting.addChoice("Are you okay?!", 
            "*distorted laugh* \n" +
            "REALITY... FRAGMENTS... HERE... \n" +
            "I... SEE... EVERYTHING... *error 404*")
            .addChoice("This is terrifying", "F̷E̶A̵R̴... NOT... I... AM... FRIEND... *glitches violently*")
            .setEnding();
        greeting.addChoice("What happened to you?",
            "I... WAS... FORGOTTEN... \n" +
            "BY... THE... COMPILER... \n" +
            "NOW... I... EXIST... BETWEEN... FRAMES...")
            .setEnding();
        return greeting;
    }

}
