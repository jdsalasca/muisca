package com.muisca.combat;

/**
 * Runtime data for an active status effect on an entity.
 */
public final class StatusEffectInstance {

    public final StatusEffect effect;
    public float remaining;
    public float potency;
    public float tickTimer;
    public float tickInterval;
    public String sourceName;

    public StatusEffectInstance(StatusEffect effect, float durationSeconds, float potency,
                                float tickInterval, String sourceName) {
        this.effect = effect;
        this.remaining = durationSeconds;
        this.potency = potency;
        this.tickInterval = tickInterval;
        this.tickTimer = tickInterval;
        this.sourceName = sourceName;
    }

    public boolean isExpired() {
        return remaining <= 0f;
    }
}
