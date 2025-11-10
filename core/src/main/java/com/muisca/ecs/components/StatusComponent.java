package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.MathUtils;
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

    public boolean isStaggered() {
        StatusEffectInstance stagger = get(StatusEffect.STAGGER);
        return stagger != null && !stagger.isExpired();
    }

    public float getMovementMultiplier() {
        if (isStaggered()) {
            return 0f;
        }
        float multiplier = 1f;
        for (StatusEffectInstance instance : statuses) {
            if (instance.effect == StatusEffect.SLOW && !instance.isExpired()) {
                float slowFactor = MathUtils.clamp(1f - instance.potency, 0f, 1f);
                multiplier *= slowFactor;
            }
        }
        return MathUtils.clamp(multiplier, 0f, 1f);
    }
}
