package com.muisca.world;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.MathUtils;
import com.muisca.jobs.JobBoard;
import com.muisca.jobs.JobBoard.HarvestSite;
import com.muisca.town.ElderAura;

public class EnvironmentRegrowthSystem extends EntitySystem implements JobBoard.Listener {

    private final FloraField floraField;
    private final JobBoard jobBoard;
    private final ElderAura elderAura;
    private final float tileSize;
    private float scatterTimer = 0f;
    // Weather-driven multiplier (e.g., rain accelerates regrowth)
    private float weatherRegrowMultiplier = 1f; // 0.5 .. 3.0

    public EnvironmentRegrowthSystem(FloraField floraField, JobBoard jobBoard, ElderAura elderAura, float tileSize) {
        this.floraField = floraField;
        this.jobBoard = jobBoard;
        this.elderAura = elderAura;
        this.tileSize = tileSize;
        jobBoard.addListener(this);
    }

    /**
     * Adjusts environmental regrowth speed based on weather.
     * For example, rain can set multiplier in [1.0 .. 1.6] depending on intensity.
     */
    public void setWeatherRegrowMultiplier(float multiplier) {
        this.weatherRegrowMultiplier = MathUtils.clamp(multiplier, 0.5f, 3.0f);
    }

    @Override
    public void update(float deltaTime) {
        jobBoard.update(deltaTime, elderAura.getFloraBoost() * weatherRegrowMultiplier);
        scatterTimer += deltaTime;
        if (scatterTimer >= 0.2f) {
            scatterTimer = 0f;
            float amount = 0.02f * elderAura.getFloraBoost() * weatherRegrowMultiplier;
            for (int i = 0; i < 4; i++) {
                floraField.randomRegrow(amount);
            }
        }
    }

    @Override
    public void onSiteHarvested(HarvestSite site) {
        floraField.decayCircle(site.position.x / tileSize, site.position.y / tileSize,
                MathUtils.random(4f, 7f), 0.3f);
    }

    @Override
    public void onSiteRegrown(HarvestSite site) {
        floraField.regrowCircle(site.position.x / tileSize, site.position.y / tileSize, MathUtils.random(4f, 7f),
                0.25f * elderAura.getFloraBoost());
    }
}
