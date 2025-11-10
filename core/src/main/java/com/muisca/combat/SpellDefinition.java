package com.muisca.combat;

import com.badlogic.gdx.utils.JsonValue;

public final class SpellDefinition {

    public final String id;
    public final String name;
    public final SpellSchool school;
    public final DamageType damageType;
    public final float baseDamage;
    public final float attackScaling;
    public final float staminaCost;
    public final float focusCost;
    public final float range;
    public final float cooldown;
    public final StatusEffect statusEffect;
    public final float statusDuration;
    public final float statusPotency;
    public final float statusTickInterval;

    public SpellDefinition(String id,
                           String name,
                           SpellSchool school,
                           DamageType damageType,
                           float baseDamage,
                           float attackScaling,
                           float staminaCost,
                           float focusCost,
                           float range,
                           float cooldown,
                           StatusEffect statusEffect,
                           float statusDuration,
                           float statusPotency,
                           float statusTickInterval) {
        this.id = id;
        this.name = name;
        this.school = school;
        this.damageType = damageType;
        this.baseDamage = baseDamage;
        this.attackScaling = attackScaling;
        this.staminaCost = staminaCost;
        this.focusCost = focusCost;
        this.range = range;
        this.cooldown = cooldown;
        this.statusEffect = statusEffect;
        this.statusDuration = statusDuration;
        this.statusPotency = statusPotency;
        this.statusTickInterval = statusTickInterval <= 0f ? 1f : statusTickInterval;
    }

    public boolean appliesStatus() {
        return statusEffect != null && statusDuration > 0f && statusPotency > 0f;
    }

    public static SpellDefinition fromJson(JsonValue value, SpellSchool school) {
        String id = value.getString("id");
        String name = value.getString("name", id);
        DamageType type = DamageType.valueOf(value.getString("damageType", "FIRE"));
        float baseDamage = value.getFloat("baseDamage", 10f);
        float attackScaling = value.getFloat("attackScaling", 0.5f);
        float staminaCost = value.getFloat("staminaCost", 5f);
        float focusCost = value.getFloat("focusCost", 0f);
        float range = value.getFloat("range", 180f);
        float cooldown = value.getFloat("cooldown", 2f);
        StatusEffect statusEffect = null;
        float statusDuration = 0f;
        float statusPotency = 0f;
        float statusTick = 1f;
        JsonValue statusJson = value.get("status");
        if (statusJson != null) {
            statusEffect = StatusEffect.valueOf(statusJson.getString("type"));
            statusDuration = statusJson.getFloat("duration", 0f);
            statusPotency = statusJson.getFloat("potency", 0f);
            statusTick = statusJson.getFloat("tickInterval", 1f);
        }
        return new SpellDefinition(id, name, school, type, baseDamage, attackScaling,
                staminaCost, focusCost, range, cooldown,
                statusEffect, statusDuration, statusPotency, statusTick);
    }
}
