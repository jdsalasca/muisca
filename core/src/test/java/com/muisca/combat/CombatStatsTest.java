package com.muisca.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatStatsTest {

    @Test
    void setPoolsClampsValues() {
        CombatStats stats = CombatStats.colonistBaseline();
        stats.setPools(999f, -5f, 12f);
        assertEquals(stats.getMaxHealth(), stats.getHealth(), 0.001f);
        assertEquals(0f, stats.getStaminaPool(), 0.001f);
        assertEquals(12f, stats.getFocusPool(), 0.001f);
    }
}
