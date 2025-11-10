package com.muisca.save;

import com.badlogic.gdx.utils.Array;

public class SaveData {
    public Array<SaveItem> inventory = new Array<>();
    public Array<SaveStructure> structures = new Array<>();
    public Array<SaveSite> harvestSites = new Array<>();
    public Array<SaveReputation> reputation = new Array<>();
    public Array<SaveFlag> flags = new Array<>();
    public Array<SaveColonist> colonists = new Array<>();
    public Array<SaveEnemy> enemies = new Array<>();

    public static class SaveItem {
        public String id;
        public int amount;
    }

    public static class SaveStructure {
        public String blueprintId;
        public float x;
        public float y;
    }

    public static class SaveSite {
        public float x;
        public float y;
        public boolean harvested;
        public float regrow;
        public float delay;
    }

    public static class SaveReputation {
        public String faction;
        public float value;
    }

    public static class SaveFlag {
        public String id;
        public boolean value;
    }

    public static class SaveColonist {
        public String name;
        public float x;
        public float y;
        public float health;
        public float stamina;
        public float focus;
        public boolean defeated;
        public float globalCooldown;
        public Array<String> talents = new Array<>();
        public Array<SaveSpellSlot> spells = new Array<>();
        public Array<SaveCombatStatus> statuses = new Array<>();
    }

    public static class SaveEnemy {
        public String archetypeId;
        public float x;
        public float y;
        public float health;
        public float stamina;
        public float focus;
        public Array<SaveSpellSlot> spells = new Array<>();
        public Array<SaveCombatStatus> statuses = new Array<>();
    }

    public static class SaveSpellSlot {
        public String spellId;
        public float cooldown;
    }

    public static class SaveCombatStatus {
        public String effect;
        public float remaining;
        public float potency;
        public float tickTimer;
        public float tickInterval;
        public String source;
    }
}
