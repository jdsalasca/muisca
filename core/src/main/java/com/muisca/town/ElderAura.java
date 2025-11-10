package com.muisca.town;

public class ElderAura {

    private float floraBoost = 1f;
    private float spiritBoost = 0f;

    public void reset() {
        floraBoost = 1f;
        spiritBoost = 0f;
    }

    public void apply(ElderProfile profile) {
        floraBoost += profile.floraBonus;
        spiritBoost += profile.spiritBonus;
    }

    public float getFloraBoost() {
        return Math.max(0.25f, floraBoost);
    }

    public float getSpiritBoost() {
        return spiritBoost;
    }
}
