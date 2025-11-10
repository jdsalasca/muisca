package com.muisca.structures;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ObjectIntMap;

public class StructureBlueprint {
    private final String id;
    private final String name;
    private final float width;
    private final float height;
    private final Color color;
    private final ObjectIntMap<String> cost;
    private final float storageCapacity;
    private final String category;

    public StructureBlueprint(String id, String name, float width, float height,
                              Color color, ObjectIntMap<String> cost,
                              float storageCapacity, String category) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.color = color;
        this.cost = cost;
        this.storageCapacity = storageCapacity;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }

    public ObjectIntMap<String> getCost() {
        return cost;
    }

    public float getStorageCapacity() {
        return storageCapacity;
    }

    public String getCategory() {
        return category;
    }

    public boolean isStorage() {
        return storageCapacity > 0f;
    }
}
