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

    public StructureBlueprint(String id, String name, float width, float height,
                              Color color, ObjectIntMap<String> cost) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.color = color;
        this.cost = cost;
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
}
