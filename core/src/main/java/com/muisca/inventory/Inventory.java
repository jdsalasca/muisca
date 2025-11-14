package com.muisca.inventory;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.muisca.structures.StructureManager;
import com.muisca.telemetry.InventoryTelemetry;

/**
 * Shared colony inventory (per settlement) used for crafting and construction.
 */
public class Inventory {

    private final ObjectIntMap<String> items = new ObjectIntMap<>();
    private StructureManager storageManager;
    private InventoryTelemetry telemetry;

    public void add(String itemId, int amount) {
        if (amount <= 0) {
            return;
        }
        items.getAndIncrement(itemId, 0, amount);
        if (telemetry != null) {
            telemetry.logAdd(itemId, amount);
        }
    }

    public int getAmount(String itemId) {
        return items.get(itemId, 0);
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
            if (storageManager != null) {
                storageManager.withdrawFromStorage(entry.key, entry.value);
            }
            if (telemetry != null) {
                telemetry.logConsume(entry.key, entry.value);
            }
        }
        return true;
    }

    public void bindStorageManager(StructureManager manager) {
        this.storageManager = manager;
    }

    public void bindTelemetry(InventoryTelemetry telemetry) {
        this.telemetry = telemetry;
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
        Array<String> keys = new Array<>(items.size);
        for (ObjectIntMap.Entry<String> entry : items.entries()) {
            keys.add(entry.key);
        }
        keys.sort();
        for (int i = 0; i < keys.size; i++) {
            if (i > 0) {
                builder.append(" | ");
            }
            String key = keys.get(i);
            builder.append(key).append(": ").append(items.get(key, 0));
        }
        return builder.toString();
    }
}
