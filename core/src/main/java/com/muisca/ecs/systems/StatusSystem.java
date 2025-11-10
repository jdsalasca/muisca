package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.muisca.combat.DamageTelemetry;
import com.muisca.combat.DamageType;
import com.muisca.combat.StatusEffect;
import com.muisca.combat.StatusEffectInstance;
import com.muisca.ecs.components.CombatIdentityComponent;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.ecs.components.StatusComponent;

public class StatusSystem extends IteratingSystem {

    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private final ComponentMapper<CombatIdentityComponent> identityMapper = ComponentMapper.getFor(CombatIdentityComponent.class);
    private final DamageTelemetry telemetry;
    private final Array<StatusEffectInstance> toRemove = new Array<>();

    public StatusSystem(DamageTelemetry telemetry) {
        super(Family.all(StatsComponent.class, StatusComponent.class).get());
        this.telemetry = telemetry;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatsComponent stats = statsMapper.get(entity);
        StatusComponent statusComponent = statusMapper.get(entity);
        CombatIdentityComponent identity = identityMapper.get(entity);
        String targetName = identity != null ? identity.name : "Entidad";
        toRemove.clear();
        for (StatusEffectInstance instance : statusComponent.statuses) {
            instance.remaining -= deltaTime;
            instance.tickTimer -= deltaTime;
            if (instance.effect == StatusEffect.BURN && instance.tickTimer <= 0f && stats.stats.isAlive()) {
                instance.tickTimer = instance.tickInterval;
                float damage = instance.potency;
                stats.stats.applyDamage(damage);
                telemetry.record(instance.sourceName != null ? instance.sourceName : "Quemadura",
                        targetName, damage, DamageType.FIRE, "Quemado", false);
            }
            if (instance.isExpired()) {
                toRemove.add(instance);
            }
        }
        for (StatusEffectInstance instance : toRemove) {
            statusComponent.statuses.removeValue(instance, true);
        }
    }
}
