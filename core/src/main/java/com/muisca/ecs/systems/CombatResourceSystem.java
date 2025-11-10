package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.muisca.ecs.components.PlayerCombatComponent;
import com.muisca.ecs.components.SpellbookComponent;
import com.muisca.ecs.components.SpellbookComponent.SpellSlot;
import com.muisca.ecs.components.StatsComponent;

public class CombatResourceSystem extends IteratingSystem {

    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<PlayerCombatComponent> playerMapper = ComponentMapper.getFor(PlayerCombatComponent.class);
    private final ComponentMapper<SpellbookComponent> spellbookMapper = ComponentMapper.getFor(SpellbookComponent.class);

    public CombatResourceSystem() {
        super(Family.all(StatsComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatsComponent stats = statsMapper.get(entity);
        if (!stats.stats.isAlive()) {
            return;
        }
        stats.stats.regenerate(deltaTime);
        SpellbookComponent spellbook = spellbookMapper.get(entity);
        if (spellbook != null) {
            for (SpellSlot slot : spellbook.slots) {
                slot.cooldownRemaining = Math.max(0f, slot.cooldownRemaining - deltaTime);
            }
        }
        PlayerCombatComponent player = playerMapper.get(entity);
        if (player != null) {
            player.globalCooldown = Math.max(0f, player.globalCooldown - deltaTime);
        }
    }
}
