package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.muisca.combat.CombatStats;

public class StatsComponent implements Component {
    public final CombatStats stats;
    public boolean defeated = false;

    public StatsComponent(CombatStats stats) {
        this.stats = stats;
    }
}
