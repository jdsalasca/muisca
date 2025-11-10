package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.muisca.colony.Colonist;
import com.muisca.combat.DamageCalculator;
import com.muisca.combat.DamageTelemetry;
import com.muisca.combat.StatusEffect;
import com.muisca.ecs.components.CombatIdentityComponent;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.EnemyComponent;
import com.muisca.ecs.components.PlayerCombatComponent;
import com.muisca.ecs.components.SpellbookComponent;
import com.muisca.ecs.components.SpellbookComponent.SpellSlot;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.ecs.components.StatusComponent;

public class PlayerCombatSystem extends IteratingSystem {

    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<SpellbookComponent> spellbookMapper = ComponentMapper.getFor(SpellbookComponent.class);
    private final ComponentMapper<PlayerCombatComponent> playerMapper = ComponentMapper.getFor(PlayerCombatComponent.class);
    private final ComponentMapper<CombatIdentityComponent> identityMapper = ComponentMapper.getFor(CombatIdentityComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private final ComponentMapper<EnemyComponent> enemyMapper = ComponentMapper.getFor(EnemyComponent.class);

    private ImmutableArray<Entity> enemyEntities;
    private final DamageTelemetry telemetry;
    private final Vector2 temp = new Vector2();
    private final Vector2 temp2 = new Vector2();

    public PlayerCombatSystem(DamageTelemetry telemetry) {
        super(Family.all(ColonistComponent.class, StatsComponent.class,
                SpellbookComponent.class, PlayerCombatComponent.class,
                CombatIdentityComponent.class).get());
        this.telemetry = telemetry;
    }

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine) {
        super.addedToEngine(engine);
        enemyEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class, StatsComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatsComponent stats = statsMapper.get(entity);
        if (!stats.stats.isAlive()) {
            return;
        }
        PlayerCombatComponent playerCombat = playerMapper.get(entity);
        SpellbookComponent spellbook = spellbookMapper.get(entity);
        CombatIdentityComponent identity = identityMapper.get(entity);
        updateRequests(entity, playerCombat, spellbook, stats, identity);
    }

    private void updateRequests(Entity entity,
                                PlayerCombatComponent playerCombat,
                                SpellbookComponent spellbook,
                                StatsComponent stats,
                                CombatIdentityComponent identity) {
        if (playerCombat.globalCooldown > 0f) {
            playerCombat.resetRequests();
            return;
        }
        if (playerCombat.requestPrimary) {
            castSpellSlot(entity, 0, spellbook, stats, identity);
        }
        if (playerCombat.requestSecondary) {
            castSpellSlot(entity, 1, spellbook, stats, identity);
        }
        playerCombat.resetRequests();
    }

    private void castSpellSlot(Entity casterEntity,
                               int slotIndex,
                               SpellbookComponent spellbook,
                               StatsComponent casterStats,
                               CombatIdentityComponent casterIdentity) {
        SpellSlot slot = spellbook.getSlot(slotIndex);
        if (slot == null || slot.spell == null || slot.cooldownRemaining > 0f) {
            return;
        }
        if (!casterStats.stats.canPay(slot.spell.staminaCost, slot.spell.focusCost)) {
            return;
        }
        Entity target = findTarget(casterEntity, slot.spell.range);
        if (target == null) {
            return;
        }
        StatusComponent targetStatus = statusMapper.get(target);
        StatsComponent targetStats = statsMapper.get(target);
        CombatIdentityComponent targetIdentity = identityMapper.get(target);
        float damage = DamageCalculator.computeSpellDamage(slot.spell, casterStats.stats, targetStats.stats, targetStatus);
        casterStats.stats.spend(slot.spell.staminaCost, slot.spell.focusCost);
        slot.cooldownRemaining = slot.spell.cooldown;
        PlayerCombatComponent player = playerMapper.get(casterEntity);
        if (player != null) {
            player.globalCooldown = 0.35f;
        }
        targetStats.stats.applyDamage(damage);
        telemetry.record(casterIdentity.name, targetIdentity != null ? targetIdentity.name : "enemigo",
                damage, slot.spell.damageType, slot.spell.name, true);
        if (slot.spell.appliesStatus() && targetStatus != null) {
            targetStatus.put(slot.spell.statusEffect, slot.spell.statusDuration,
                    resolveStatusPotency(slot.spell.statusEffect, slot.spell.statusPotency),
                    slot.spell.statusTickInterval, casterIdentity.name);
        }
    }

    private float resolveStatusPotency(StatusEffect effect, float base) {
        if (effect == StatusEffect.BOND) {
            return base; // treat as % bonus (0.15 = +15% damage taken)
        }
        return base;
    }

    private Entity findTarget(Entity casterEntity, float range) {
        if (enemyEntities == null || enemyEntities.size() == 0) {
            return null;
        }
        Entity best = null;
        float bestDst2 = Float.MAX_VALUE;
        Vector2 origin = getPosition(casterEntity, temp);
        float range2 = range * range;
        for (int i = 0; i < enemyEntities.size(); i++) {
            Entity enemy = enemyEntities.get(i);
            StatsComponent targetStats = statsMapper.get(enemy);
            if (targetStats == null || !targetStats.stats.isAlive()) {
                continue;
            }
            Vector2 targetPos = getPosition(enemy, temp2);
            float dst2 = origin.dst2(targetPos);
            if (dst2 < range2 && dst2 < bestDst2) {
                best = enemy;
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
