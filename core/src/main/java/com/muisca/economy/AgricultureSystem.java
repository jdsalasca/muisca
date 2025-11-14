package com.muisca.economy;

import com.badlogic.ashley.core.EntitySystem;

/**
 * Drives the farm plots and the lightweight season clock.
 */
public class AgricultureSystem extends EntitySystem {

    private final FarmPlotManager farmPlotManager;
    private final SeasonClock seasonClock = new SeasonClock();

    public AgricultureSystem(FarmPlotManager farmPlotManager) {
        this.farmPlotManager = farmPlotManager;
    }

    public void setSeasonDuration(float seconds) {
        seasonClock.setSeasonDuration(seconds);
    }

    public void resetSeason(Season season) {
        seasonClock.reset(season);
    }

    @Override
    public void update(float deltaTime) {
        seasonClock.update(deltaTime);
        farmPlotManager.update(deltaTime, seasonClock.getCurrentSeason());
    }

    public Season getCurrentSeason() {
        return seasonClock.getCurrentSeason();
    }

    public float getSeasonFraction() {
        return seasonClock.getFraction();
    }
}
