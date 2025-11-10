package com.muisca.decisions;

import com.badlogic.gdx.utils.ObjectMap;

public class DecisionState {

    private final ObjectMap<String, Boolean> flags = new ObjectMap<>();

    public boolean getFlag(String id) {
        return flags.get(id, false);
    }

    public void setFlag(String id, boolean value) {
        flags.put(id, value);
    }

    public ObjectMap<String, Boolean> snapshot() {
        ObjectMap<String, Boolean> copy = new ObjectMap<>();
        for (ObjectMap.Entry<String, Boolean> entry : flags.entries()) {
            copy.put(entry.key, entry.value);
        }
        return copy;
    }

    public void restore(ObjectMap<String, Boolean> data) {
        flags.clear();
        for (ObjectMap.Entry<String, Boolean> entry : data.entries()) {
            flags.put(entry.key, entry.value);
        }
    }
}
