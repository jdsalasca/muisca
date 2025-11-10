package com.muisca.inventory;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Shared colony inventory (per settlement) used for crafting and construction.
 */
public class Inventory {

    private final ObjectIntMap<String> items = new ObjectIntMap<>();

    public void add(String itemId, int amount) {
        if (amount <= 0) {
            return;
        }
        items.getAndIncrement(itemId, 0, amount);
    }

    public boolean has(String itemId, int amount) {
        return items.get(itemId, 0) >= amount;
    }

    public boolean hasAll(ObjectIntMap<String> costs) {
        for (ObjectIntMap.Entry<String> entry : costs.entries()) {
            if (items.get(entry.key, 0) < entry.value) {
                return false;
            }
        }
        return true;
    }

    public boolean consumeAll(ObjectIntMap<String> costs) {
        if (!hasAll(costs)) {
            return false;
        }
        for (ObjectIntMap.Entry<String> entry : costs.entries()) {
            items.getAndIncrement(entry.key, 0, -entry.value);
            if (items.get(entry.key, 0) <= 0) {
                items.remove(entry.key, 0);
            }
        }
        return true;
    }

    public ObjectIntMap<String> snapshot() {
        ObjectIntMap<String> copy = new ObjectIntMap<>();
        for (ObjectIntMap.Entry<String> entry : items.entries()) {
            copy.put(entry.key, entry.value);
        }
        return copy;
    }

    public void setFromSnapshot(ObjectIntMap<String> data) {
        items.clear();
        for (ObjectIntMap.Entry<String> entry : data.entries()) {
            if (entry.value > 0) {
                items.put(entry.key, entry.value);
            }
        }
    }

    public String summarize() {
        if (items.size == 0) {
            return "(vacío)";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (ObjectIntMap.Entry<String> entry : items.entries()) {
            if (!first) {
                builder.append(" | ");
            }
            builder.append(entry.key).append(": ").append(entry.value);
            first = false;
        }
        return builder.toString();
    }
}
