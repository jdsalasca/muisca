package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.muisca.colony.Colonist;
import com.muisca.combat.DamageCalculator;
import com.muisca.combat.DamageTelemetry;
import com.muisca.combat.DamageType;
import com.muisca.combat.StatusEffect;
import com.muisca.ecs.components.CombatIdentityComponent;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.EnemyComponent;
import com.muisca.ecs.components.SpellbookComponent;
import com.muisca.ecs.components.SpellbookComponent.SpellSlot;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.ecs.components.StatusComponent;
import com.muisca.enemies.EnemyArchetype;

public class EnemyAISystem extends IteratingSystem {

    private final ComponentMapper<EnemyComponent> enemyMapper = ComponentMapper.getFor(EnemyComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<SpellbookComponent> spellMapper = ComponentMapper.getFor(SpellbookComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private final ComponentMapper<CombatIdentityComponent> identityMapper = ComponentMapper.getFor(CombatIdentityComponent.class);
    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);

    private ImmutableArray<Entity> colonistEntities;
    private final DamageTelemetry telemetry;
    private final Vector2 temp = new Vector2();
    private final Vector2 temp2 = new Vector2();

    public EnemyAISystem(DamageTelemetry telemetry) {
        super(Family.all(EnemyComponent.class, StatsComponent.class, CombatIdentityComponent.class).get());
        this.telemetry = telemetry;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        colonistEntities = engine.getEntitiesFor(Family.all(ColonistComponent.class, StatsComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        EnemyComponent enemy = enemyMapper.get(entity);
        StatsComponent stats = statsMapper.get(entity);
        CombatIdentityComponent identity = identityMapper.get(entity);
        if (!stats.stats.isAlive()) {
            return;
        }
        enemy.attackTimer = Math.max(0f, enemy.attackTimer - deltaTime);
        Entity target = findTarget(enemy);
        if (target == null) {
            return;
        }
        Vector2 targetPos = getPosition(target, temp2);
        moveTowards(enemy, targetPos, deltaTime);
        float distance = enemy.position.dst(targetPos);
        if (tryCastSpell(entity, target, distance)) {
            return;
        }
        if (distance <= enemy.archetype.meleeRange && enemy.attackTimer <= 0f) {
            performMelee(enemy, stats, identity, target);
        }
    }

    private void moveTowards(EnemyComponent enemy, Vector2 target, float delta) {
        Vector2 position = enemy.position;
        EnemyArchetype archetype = enemy.archetype;
        float distance = position.dst(target);
        Vector2 desired = temp.set(target).sub(position);
        if (distance > archetype.preferredDistance + 10f) {
            desired.nor().scl(archetype.movementSpeed * delta);
            position.add(desired);
        } else if (distance < archetype.preferredDistance - 10f) {
            desired.nor().scl(archetype.movementSpeed * delta);
            position.sub(desired);
        }
    }

    private boolean tryCastSpell(Entity caster, Entity target, float distance) {
        SpellbookComponent spellbook = spellMapper.get(caster);
        if (spellbook == null) {
            return false;
        }
        StatsComponent casterStats = statsMapper.get(caster);
        StatsComponent targetStats = statsMapper.get(target);
        CombatIdentityComponent casterIdentity = identityMapper.get(caster);
        CombatIdentityComponent targetIdentity = identityMapper.get(target);
        StatusComponent targetStatus = statusMapper.get(target);
        for (SpellSlot slot : spellbook.slots) {
            if (slot.spell == null || slot.cooldownRemaining > 0f) {
                continue;
            }
            if (distance > slot.spell.range) {
                continue;
            }
            if (!casterStats.stats.canPay(slot.spell.staminaCost, slot.spell.focusCost)) {
                continue;
            }
            casterStats.stats.spend(slot.spell.staminaCost, slot.spell.focusCost);
            slot.cooldownRemaining = slot.spell.cooldown;
            float damage = DamageCalculator.computeSpellDamage(slot.spell, casterStats.stats, targetStats.stats, targetStatus);
            targetStats.stats.applyDamage(damage);
            telemetry.record(casterIdentity.name, targetIdentity.name, damage, slot.spell.damageType, slot.spell.name, false);
            if (slot.spell.appliesStatus() && targetStatus != null) {
                targetStatus.put(slot.spell.statusEffect, slot.spell.statusDuration,
                        slot.spell.statusPotency, slot.spell.statusTickInterval, casterIdentity.name);
            }
            enemyMapper.get(caster).attackTimer = 0.5f;
            return true;
        }
        return false;
    }

    private void performMelee(EnemyComponent enemy,
                              StatsComponent stats,
                              CombatIdentityComponent identity,
                              Entity target) {
        StatsComponent targetStats = statsMapper.get(target);
        CombatIdentityComponent targetIdentity = identityMapper.get(target);
        if (targetStats == null || targetIdentity == null) {
            return;
        }
        float damage = Math.max(5f, stats.stats.getAttack() * 0.9f);
        damage = Math.max(1f, damage - targetStats.stats.getMitigation(DamageType.PHYSICAL) * 0.3f);
        targetStats.stats.applyDamage(damage);
        telemetry.record(identity.name, targetIdentity.name, damage, DamageType.PHYSICAL, "Golpe", false);
        enemy.attackTimer = 1.2f;
    }

    private Entity findTarget(EnemyComponent enemy) {
        if (colonistEntities == null || colonistEntities.size() == 0) {
            return null;
        }
        Entity best = null;
        float bestDst2 = Float.MAX_VALUE;
        Vector2 origin = enemy.position;
        float range2 = enemy.archetype.engagementRange * enemy.archetype.engagementRange;
        for (int i = 0; i < colonistEntities.size(); i++) {
            Entity colonist = colonistEntities.get(i);
            StatsComponent stats = statsMapper.get(colonist);
            if (stats == null || !stats.stats.isAlive()) {
                continue;
            }
            Vector2 pos = getPosition(colonist, temp);
            float dst2 = origin.dst2(pos);
            if (dst2 < range2 && dst2 < bestDst2) {
                best = colonist;
                bestDst2 = dst2;
            }
        }
        return best;
    }

    private Vector2 getPosition(Entity entity, Vector2 out) {
        ColonistComponent colonist = colonistMapper.get(entity);
        if (colonist != null) {
            Colonist data = colonist.colonist;
            return out.set(data.getPosition());
        }
        EnemyComponent enemy = enemyMapper.get(entity);
        if (enemy != null) {
            return out.set(enemy.position);
        }
        return out.set(0f, 0f);
    }
}
