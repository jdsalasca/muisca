package com.muisca.world;

public class DayCycle {

    private float fraction;

    public void setFraction(float value) {
        fraction = value - (float) Math.floor(value); // keep 0-1
    }

    public float getFraction() {
        return fraction;
    }

    public boolean isBetween(float start, float end) {
        if (start <= end) {
            return fraction >= start && fraction <= end;
        }
        return fraction >= start || fraction <= end;
    }
}
