package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.muisca.combat.StatusEffect;
import com.muisca.combat.StatusEffectInstance;

public class StatusComponent implements Component {

    public final Array<StatusEffectInstance> statuses = new Array<>();

    public StatusEffectInstance get(StatusEffect effect) {
        for (StatusEffectInstance instance : statuses) {
            if (instance.effect == effect) {
                return instance;
            }
        }
        return null;
    }

    public void put(StatusEffect effect, float duration, float potency, float tickInterval, String sourceName) {
        StatusEffectInstance existing = get(effect);
        if (existing != null) {
            existing.remaining = duration;
            existing.potency = potency;
            existing.tickInterval = tickInterval <= 0f ? 1f : tickInterval;
            existing.tickTimer = existing.tickInterval;
            existing.sourceName = sourceName;
            return;
        }
        statuses.add(new StatusEffectInstance(effect, duration, potency, tickInterval, sourceName));
    }

    public float getDamageTakenBonus() {
        float bonus = 0f;
        StatusEffectInstance bond = get(StatusEffect.BOND);
        if (bond != null && !bond.isExpired()) {
            bonus += bond.potency;
        }
        return bonus;
    }
}
