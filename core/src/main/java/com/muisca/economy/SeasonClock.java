package com.muisca.economy;

/**
 * Simple looping season clock. Each slice keeps the current season for a fixed duration
 * (default 120 seconds) to keep the prototype deterministic.
 */
public class SeasonClock {

    private final Season[] order = new Season[] {
            Season.TEMPERATE,
            Season.RAINY,
            Season.DRY
    };
    private float seasonDuration = 120f;
    private float timer = 0f;
    private int index = 0;

    public void setSeasonDuration(float seconds) {
        this.seasonDuration = Math.max(10f, seconds);
    }

    public void reset(Season startSeason) {
        for (int i = 0; i < order.length; i++) {
            if (order[i] == startSeason) {
                index = i;
                break;
            }
        }
        timer = 0f;
    }

    public void update(float delta) {
        timer += delta;
        if (timer >= seasonDuration) {
            timer -= seasonDuration;
            index = (index + 1) % order.length;
        }
    }

    public Season getCurrentSeason() {
        return order[index];
    }

    public float getFraction() {
        return timer / seasonDuration;
    }
}
