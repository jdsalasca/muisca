package com.muisca.decisions;

import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

public class DecisionOption {
    public String label;
    public ObjectIntMap<String> inventoryCost = new ObjectIntMap<>();
    public ObjectIntMap<String> inventoryReward = new ObjectIntMap<>();
    public ObjectFloatMap<String> reputationMin = new ObjectFloatMap<>();
    public ObjectFloatMap<String> reputationDelta = new ObjectFloatMap<>();
    public ObjectMap<String, Boolean> flagSet = new ObjectMap<>();
    public String nextNode;
}
