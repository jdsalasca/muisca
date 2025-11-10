package com.muisca.combat;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Loads spell definitions for all schools from JSON files.
 */
public final class SpellLibrary {

    private final ObjectMap<String, SpellDefinition> spellsById = new ObjectMap<>();
    private final ObjectMap<SpellSchool, Array<SpellDefinition>> spellsBySchool = new ObjectMap<>();

    public SpellDefinition get(String id) {
        return spellsById.get(id);
    }

    public Array<SpellDefinition> getBySchool(SpellSchool school) {
        Array<SpellDefinition> array = spellsBySchool.get(school);
        if (array == null) {
            array = new Array<>();
            spellsBySchool.put(school, array);
        }
        return array;
    }

    public static SpellLibrary load(FileHandle directory) {
        SpellLibrary library = new SpellLibrary();
        JsonReader reader = new JsonReader();
        for (FileHandle file : directory.list("json")) {
            JsonValue root = reader.parse(file);
            SpellSchool school = SpellSchool.valueOf(root.getString("school").toUpperCase());
            JsonValue spells = root.get("spells");
            if (spells == null) {
                continue;
            }
            for (JsonValue spellValue : spells) {
                SpellDefinition def = SpellDefinition.fromJson(spellValue, school);
                library.spellsById.put(def.id, def);
                library.getBySchool(school).add(def);
            }
        }
        return library;
    }
}
