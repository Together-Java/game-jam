package com.tuvalutorture.gamejam;

import com.badlogic.gdx.graphics.Texture;

public class Item {
    public enum Items {
        PROCESSOR,
        UNENRICHED_URANIUM,
        ENRICHED_URANIUM,
        PEANUT_BUTTER,
        SILLY_PUTTY,
        SERIOUS_PUTTY,
        CAR_BATTERY,
        CATALYTIC_CONVERTER,
        SINGULARITY
    }

    public Texture texture;
    public Items item, sibling;
    public String acquisitionText;

    public boolean canBeCombined;
    public String siblingPromptText;
    private Item childResult;

    public Item(Items item, Texture texture, String acquisitionText) {
        this.item = item;
        this.texture = texture;
        this.acquisitionText = acquisitionText;
        this.canBeCombined = false;
    }

    public Item combine(Item[] checkArray) {
        if (sibling == null || childResult == null || !canBeCombined) return this;
        for (Item comparison : checkArray) {
            if (comparison.item == sibling) return childResult;
        }
        return this;
    }

    public void createSiblingItem(String siblingPromptText, Item child, Items componentID) {
        this.siblingPromptText = siblingPromptText;
        this.childResult = child;
        this.item = componentID;
        this.canBeCombined = true;
    }
}
