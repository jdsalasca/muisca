package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.muisca.ecs.components.PlayerCombatComponent;
import com.muisca.ecs.components.SpellbookComponent;
import com.muisca.ecs.components.SpellbookComponent.SpellSlot;
import com.muisca.ecs.components.StatsComponent;

public class CombatResourceSystem extends IteratingSystem {

    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<PlayerCombatComponent> playerMapper = ComponentMapper.getFor(PlayerCombatComponent.class);
    private final ComponentMapper<SpellbookComponent> spellbookMapper = ComponentMapper.getFor(SpellbookComponent.class);

    // Environmental regen scaling (e.g., rain/night penalties)
    private float regenScale = 1f; // 0.2 .. 2.0

    public CombatResourceSystem() {
        super(Family.all(StatsComponent.class).get());
    }

    /**
     * Sets a global scaling factor for stamina/focus regeneration.
     * Values are clamped between 0.2 and 2.0 for stability.
     */
    public void setRegenScale(float scale) {
        this.regenScale = MathUtils.clamp(scale, 0.2f, 2.0f);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatsComponent stats = statsMapper.get(entity);
        if (!stats.stats.isAlive()) {
            return;
        }
        // Apply environmental scaling to regeneration (e.g., rain/night penalties)
        stats.stats.regenerate(deltaTime * regenScale);
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
