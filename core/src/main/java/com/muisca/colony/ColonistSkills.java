package com.muisca.colony;

import com.badlogic.gdx.math.MathUtils;

public class ColonistSkills {

    private float gatherLevel = 1f;
    private float craftLevel = 1f;

    public float getGatherMultiplier() {
        return 1f + gatherLevel * 0.05f;
    }

    public float getCraftMultiplier() {
        return 1f + craftLevel * 0.05f;
    }

    public void gainGatherXp(float amount) {
        gatherLevel = MathUtils.clamp(gatherLevel + amount, 1f, 10f);
    }

    public void gainCraftXp(float amount) {
        craftLevel = MathUtils.clamp(craftLevel + amount, 1f, 10f);
    }

    public float getGatherLevel() {
        return gatherLevel;
    }

    public float getCraftLevel() {
        return craftLevel;
    }

    public void setGatherLevel(float level) {
        gatherLevel = MathUtils.clamp(level, 1f, 10f);
    }

    public void setCraftLevel(float level) {
        craftLevel = MathUtils.clamp(level, 1f, 10f);
    }
}
