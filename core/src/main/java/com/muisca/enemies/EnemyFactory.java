package com.muisca.enemies;

import com.badlogic.ashley.core.Entity;
import com.muisca.combat.CombatStats;
import com.muisca.combat.SpellDefinition;
import com.muisca.combat.SpellLibrary;
import com.muisca.combat.TalentId;
import com.muisca.ecs.components.CombatIdentityComponent;
import com.muisca.ecs.components.EnemyComponent;
import com.muisca.ecs.components.ForceComponent;
import com.muisca.ecs.components.SpellbookComponent;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.ecs.components.StatusComponent;
import com.muisca.ecs.components.TalentComponent;

public final class EnemyFactory {

    private final SpellLibrary spellLibrary;

    public EnemyFactory(SpellLibrary spellLibrary) {
        this.spellLibrary = spellLibrary;
    }

    public Entity createEnemy(EnemyArchetype archetype, float x, float y) {
        Entity entity = new Entity();
        entity.add(new CombatIdentityComponent(archetype.displayName, CombatIdentityComponent.Faction.ENEMY));
        entity.add(new EnemyComponent(archetype, x, y));
        entity.add(new ForceComponent());
        CombatStats stats = archetype.createStats();
        TalentComponent talents = new TalentComponent();
        for (TalentId talent : archetype.talents) {
            talents.addTalent(talent);
            talent.apply(stats);
        }
        entity.add(talents);
        entity.add(new StatsComponent(stats));
        entity.add(new StatusComponent());
        SpellbookComponent spellbook = new SpellbookComponent();
        for (String spellId : archetype.spellIds) {
            SpellDefinition spell = spellLibrary.get(spellId);
            if (spell != null) {
                spellbook.addSpell(spell);
            }
        }
        entity.add(spellbook);
        return entity;
    }
}
