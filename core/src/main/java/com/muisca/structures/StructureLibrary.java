package com.muisca.structures;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectIntMap;

import java.util.HashMap;
import java.util.Map;

public class StructureLibrary {
    private final Map<String, StructureBlueprint> blueprints = new HashMap<>();

    public static StructureLibrary load(FileHandle handle) {
        StructureLibrary library = new StructureLibrary();
        JsonValue root = new JsonReader().parse(handle);
        for (JsonValue structure : root.get("structures")) {
            String id = structure.getString("id");
            String name = structure.getString("name", id);
            float width = structure.getFloat("width", 32f);
            float height = structure.getFloat("height", 32f);
            Color color = Color.valueOf(structure.getString("color", "A67C52"));
            float storage = structure.getFloat("storageCapacity", 0f);
            String category = structure.getString("category", "generic");
            ObjectIntMap<String> cost = new ObjectIntMap<>();
            JsonValue costNode = structure.get("cost");
            if (costNode != null) {
                for (JsonValue item : costNode) {
                    cost.put(item.name, item.asInt());
                }
            }
            StructureBlueprint blueprint = new StructureBlueprint(id, name, width, height, color, cost, storage, category);
            library.blueprints.put(id, blueprint);
        }
        return library;
    }

    public StructureBlueprint get(String id) {
        return blueprints.get(id);
    }
}
