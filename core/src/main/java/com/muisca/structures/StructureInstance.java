package com.muisca.structures;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectIntMap;

public class StructureInstance {
    private final StructureBlueprint blueprint;
    private final Rectangle bounds;
    private final ObjectIntMap<String> storedItems = new ObjectIntMap<>();
    private float storedAmount = 0f;

    public StructureInstance(StructureBlueprint blueprint, float x, float y) {
        this.blueprint = blueprint;
        this.bounds = new Rectangle(
                x - blueprint.getWidth() / 2f,
                y - blueprint.getHeight() / 2f,
                blueprint.getWidth(),
                blueprint.getHeight());
    }

    public StructureBlueprint getBlueprint() {
        return blueprint;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean hasStorage() {
        return blueprint.isStorage();
    }

    public float getStorageCapacity() {
        return blueprint.getStorageCapacity();
    }

    public float getStoredAmount() {
        return storedAmount;
    }

    public int store(String itemId, int amount) {
        if (!hasStorage() || amount <= 0) {
            return 0;
        }
        float space = Math.max(0f, getStorageCapacity() - storedAmount);
        int stored = Math.min((int) space, amount);
        if (stored > 0) {
            storedItems.getAndIncrement(itemId, 0, stored);
            storedAmount += stored;
        }
        return stored;
    }

    public ObjectIntMap<String> getStoredItems() {
        ObjectIntMap<String> snapshot = new ObjectIntMap<>();
        for (ObjectIntMap.Entry<String> entry : storedItems.entries()) {
            snapshot.put(entry.key, entry.value);
        }
        return snapshot;
    }

    public int withdraw(String itemId, int amount) {
        if (!hasStorage() || amount <= 0) {
            return 0;
        }
        int available = storedItems.get(itemId, 0);
        int taken = Math.min(available, amount);
        if (taken > 0) {
            storedItems.getAndIncrement(itemId, 0, -taken);
            if (storedItems.get(itemId, 0) <= 0) {
                storedItems.remove(itemId, 0);
            }
            storedAmount = Math.max(0f, storedAmount - taken);
        }
        return taken;
    }

    public void setStoredItems(ObjectIntMap<String> contents) {
        storedItems.clear();
        storedAmount = 0f;
        if (contents == null) {
            return;
        }
        for (ObjectIntMap.Entry<String> entry : contents.entries()) {
            storedItems.put(entry.key, entry.value);
            storedAmount += entry.value;
        }
    }
}
