package com.muisca.crafting;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectIntMap;

import java.util.HashMap;
import java.util.Map;

public class RecipeBook {

    private final Map<String, Recipe> recipes = new HashMap<>();

    public static RecipeBook load(FileHandle handle) {
        JsonValue root = new JsonReader().parse(handle);
        RecipeBook book = new RecipeBook();
        for (JsonValue recipeValue : root.get("recipes")) {
            String id = recipeValue.getString("id");
            String name = recipeValue.getString("name", id);
            float workTime = recipeValue.getFloat("workTime", 1.0f);
            ObjectIntMap<String> inputs = parseMap(recipeValue.get("inputs"));
            ObjectIntMap<String> outputs = parseMap(recipeValue.get("outputs"));
            Recipe recipe = new Recipe(id, name, workTime, inputs, outputs);
            book.recipes.put(id, recipe);
        }
        return book;
    }

    private static ObjectIntMap<String> parseMap(JsonValue node) {
        ObjectIntMap<String> map = new ObjectIntMap<>();
        if (node == null) {
            return map;
        }
        for (JsonValue child : node) {
            map.put(child.name, child.asInt());
        }
        return map;
    }

    public Recipe get(String id) {
        return recipes.get(id);
    }

    public Array<Recipe> all() {
        return new Array<>(recipes.values().toArray(new Recipe[0]));
    }
}
