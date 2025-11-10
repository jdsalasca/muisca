package com.muisca.structures;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.muisca.inventory.Inventory;
import com.badlogic.gdx.utils.ObjectIntMap;
import java.util.Comparator;

public class StructureManager {

    private final StructureLibrary library;
    private final Inventory inventory;
    private final Array<StructureInstance> instances = new Array<>();
    private final Array<StructureInstance> storageBuffer = new Array<>();

    public StructureManager(StructureLibrary library, Inventory inventory) {
        this.library = library;
        this.inventory = inventory;
    }

    public Array<StructureInstance> getInstances() {
        return instances;
    }

    public boolean place(String blueprintId, Vector2 position) {
        StructureBlueprint blueprint = library.get(blueprintId);
        if (blueprint == null) {
            return false;
        }
        if (!inventory.consumeAll(blueprint.getCost())) {
            return false;
        }
        instances.add(new StructureInstance(blueprint, position.x, position.y));
        return true;
    }

    public void addRestored(String blueprintId, Vector2 position) {
        StructureBlueprint blueprint = library.get(blueprintId);
        if (blueprint != null) {
            instances.add(new StructureInstance(blueprint, position.x, position.y));
        }
    }

    public void addRestored(String blueprintId, Vector2 position, ObjectIntMap<String> contents) {
        StructureBlueprint blueprint = library.get(blueprintId);
        if (blueprint != null) {
            StructureInstance instance = new StructureInstance(blueprint, position.x, position.y);
            if (contents != null && blueprint.isStorage()) {
                instance.setStoredItems(contents);
            }
            instances.add(instance);
        }
    }

    public StructureBlueprint getBlueprint(String id) {
        return library.get(id);
    }

    public int depositNearest(Vector2 position, String itemId, int amount) {
        if (amount <= 0) {
            return 0;
        }
        storageBuffer.clear();
        for (StructureInstance instance : instances) {
            if (instance.hasStorage() && instance.getStoredAmount() < instance.getStorageCapacity()) {
                storageBuffer.add(instance);
            }
        }
        final float px = position.x;
        final float py = position.y;
        storageBuffer.sort(Comparator.comparingDouble(i -> distance2(i, px, py)));
        int stored = 0;
        for (StructureInstance instance : storageBuffer) {
            int added = instance.store(itemId, amount - stored);
            stored += added;
            if (stored >= amount) {
                break;
            }
        }
        return stored;
    }

    private double distance2(StructureInstance instance, float px, float py) {
        Rectangle bounds = instance.getBounds();
        float cx = bounds.x + bounds.width / 2f;
        float cy = bounds.y + bounds.height / 2f;
        float dx = cx - px;
        float dy = cy - py;
        return dx * dx + dy * dy;
    }

    public Array<StructureInstance> getStorageInstances() {
        storageBuffer.clear();
        for (StructureInstance instance : instances) {
            if (instance.hasStorage()) {
                storageBuffer.add(instance);
            }
        }
        return storageBuffer;
    }

    /**
     * Withdraws up to 'amount' of the given itemId from available storage structures.
     * Iterates over all storage instances and withdraws until the requested amount is satisfied or
     * storage is exhausted. Returns the total amount withdrawn.
     */
    public int withdrawFromStorage(String itemId, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int withdrawn = 0;
        for (StructureInstance instance : instances) {
            if (!instance.hasStorage()) {
                continue;
            }
            int taken = instance.withdraw(itemId, amount - withdrawn);
            withdrawn += taken;
            if (withdrawn >= amount) {
                break;
            }
        }
        return withdrawn;
    }
}
