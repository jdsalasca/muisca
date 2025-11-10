package com.muisca.structures;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.muisca.inventory.Inventory;

public class StructureManager {

    private final StructureLibrary library;
    private final Inventory inventory;
    private final Array<StructureInstance> instances = new Array<>();

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

    public StructureBlueprint getBlueprint(String id) {
        return library.get(id);
    }
}
