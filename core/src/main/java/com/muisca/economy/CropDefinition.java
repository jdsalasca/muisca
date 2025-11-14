package com.muisca.economy;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * Data-holder for a crop entry loaded from assets/data/crops.json.
 */
public class CropDefinition {

    private final String id;
    private final String name;
    private final float growthSeconds;
    private final ObjectIntMap<String> outputs;
    private final Array<Season> preferredSeasons;

    public CropDefinition(String id, String name, float growthSeconds,
                          ObjectIntMap<String> outputs, Array<Season> preferredSeasons) {
        this.id = id;
        this.name = name;
        this.growthSeconds = growthSeconds;
        this.outputs = outputs;
        this.preferredSeasons = preferredSeasons;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getGrowthSeconds() {
        return growthSeconds;
    }

    public ObjectIntMap<String> getOutputs() {
        return outputs;
    }

    public Array<Season> getPreferredSeasons() {
        return preferredSeasons;
    }

    public float getSeasonMultiplier(Season currentSeason) {
        if (currentSeason == null || preferredSeasons.size == 0) {
            return 1f;
        }
        for (Season season : preferredSeasons) {
            if (season == currentSeason) {
                return 1.25f;
            }
        }
        return 0.85f;
    }
}
