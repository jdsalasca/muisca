package com.muisca.save;

import com.badlogic.gdx.utils.Array;

public class SaveData {
    public Array<SaveItem> inventory = new Array<>();
    public Array<SaveStructure> structures = new Array<>();
    public Array<SaveSite> harvestSites = new Array<>();
    public Array<SaveReputation> reputation = new Array<>();
    public Array<SaveFlag> flags = new Array<>();

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
    }

    public static class SaveReputation {
        public String faction;
        public float value;
    }

    public static class SaveFlag {
        public String id;
        public boolean value;
    }
}
