package com.muisca.reputation;

import com.badlogic.gdx.utils.ObjectFloatMap;

public class ReputationTracker {

    private final ObjectFloatMap<String> reputation = new ObjectFloatMap<>();

    public ReputationTracker() {
        reputation.put("liga", 0f);
        reputation.put("vigias", 0f);
        reputation.put("brasa", 0f);
    }

    public float get(String factionId) {
        return reputation.get(factionId, 0f);
    }

    public void set(String factionId, float value) {
        reputation.put(factionId, value);
    }

    public void add(String factionId, float delta) {
        reputation.put(factionId, reputation.get(factionId, 0f) + delta);
    }

    public ObjectFloatMap<String> snapshot() {
        ObjectFloatMap<String> snapshot = new ObjectFloatMap<>();
        for (ObjectFloatMap.Entry<String> entry : reputation.entries()) {
            snapshot.put(entry.key, entry.value);
        }
        return snapshot;
    }

    public void restore(ObjectFloatMap<String> data) {
        reputation.clear();
        for (ObjectFloatMap.Entry<String> entry : data.entries()) {
            reputation.put(entry.key, entry.value);
        }
    }
}
