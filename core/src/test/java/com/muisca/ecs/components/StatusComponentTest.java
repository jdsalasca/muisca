package com.muisca.ecs.components;

import com.muisca.combat.StatusEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusComponentTest {

    @Test
    void slowAffectsMovementMultiplier() {
        StatusComponent component = new StatusComponent();
        component.put(StatusEffect.SLOW, 5f, 0.25f, 1f, "test");
        assertEquals(0.75f, component.getMovementMultiplier(), 0.001f);
    }

    @Test
    void staggerStopsMovement() {
        StatusComponent component = new StatusComponent();
        component.put(StatusEffect.STAGGER, 2f, 0f, 1f, "test");
        assertTrue(component.isStaggered());
        assertEquals(0f, component.getMovementMultiplier(), 0.001f);
    }
}
