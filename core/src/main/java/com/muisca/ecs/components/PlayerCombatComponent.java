package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;

public class PlayerCombatComponent implements Component {
    public boolean requestPrimary = false;
    public boolean requestSecondary = false;
    public float globalCooldown = 0f;
    public float castRangeBonus = 0f;

    public void resetRequests() {
        requestPrimary = false;
        requestSecondary = false;
    }
}
