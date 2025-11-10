package com.muisca.crafting;

import com.badlogic.gdx.utils.ObjectIntMap;

public class Recipe {
    private final String id;
    private final String name;
    private final float workTime;
    private final ObjectIntMap<String> inputs;
    private final ObjectIntMap<String> outputs;

    public Recipe(String id, String name, float workTime,
                  ObjectIntMap<String> inputs, ObjectIntMap<String> outputs) {
        this.id = id;
        this.name = name;
        this.workTime = workTime;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getWorkTime() {
        return workTime;
    }

    public ObjectIntMap<String> getInputs() {
        return inputs;
    }

    public ObjectIntMap<String> getOutputs() {
        return outputs;
    }
}
