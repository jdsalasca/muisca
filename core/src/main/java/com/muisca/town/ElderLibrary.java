package com.muisca.town;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public final class ElderLibrary {

    private final Array<ElderProfile> elders = new Array<>();

    public static ElderLibrary load(FileHandle file) {
        Json json = new Json();
        ElderProfile[] profiles = json.fromJson(ElderProfile[].class, file);
        ElderLibrary library = new ElderLibrary();
        if (profiles != null) {
            for (ElderProfile profile : profiles) {
                library.elders.add(profile);
            }
        }
        return library;
    }

    public ElderProfile getRandom() {
        if (elders.size == 0) {
            return null;
        }
        return elders.random();
    }
}
