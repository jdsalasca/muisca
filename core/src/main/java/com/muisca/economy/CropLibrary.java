package com.muisca.economy;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectIntMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Loader/registry for crop definitions.
 */
public class CropLibrary {

    private final Map<String, CropDefinition> crops = new HashMap<>();

    public static CropLibrary load(FileHandle handle) {
        CropLibrary library = new CropLibrary();
        JsonValue root = new JsonReader().parse(handle);
        for (JsonValue cropNode : root.get("crops")) {
            String id = cropNode.getString("id");
            String name = cropNode.getString("name", id);
            float growthSeconds = cropNode.getFloat("growthSeconds", 90f);
            ObjectIntMap<String> outputs = new ObjectIntMap<>();
            JsonValue outputsNode = cropNode.get("outputs");
            if (outputsNode != null) {
                for (JsonValue entry : outputsNode) {
                    outputs.put(entry.name, entry.asInt());
                }
            }
            Array<Season> seasons = new Array<>();
            JsonValue seasonsNode = cropNode.get("seasons");
            if (seasonsNode != null) {
                for (JsonValue seasonNode : seasonsNode) {
                    seasons.add(Season.fromString(seasonNode.asString()));
                }
            }
            CropDefinition definition = new CropDefinition(id, name, growthSeconds, outputs, seasons);
            library.crops.put(id, definition);
        }
        return library;
    }

    public CropDefinition get(String id) {
        return crops.get(id);
    }

    public Array<CropDefinition> all() {
        return new Array<>(crops.values().toArray(new CropDefinition[0]));
    }
}
