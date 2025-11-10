package com.muisca.town;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.inventory.Inventory;

public class ElderCouncilSystem extends EntitySystem {

    private final ComponentMapper<ElderComponent> elderMapper = ComponentMapper.getFor(ElderComponent.class);
    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private ImmutableArray<Entity> elders;
    private final ElderAura aura;
    private final Inventory inventory;

    public ElderCouncilSystem(ElderAura aura, Inventory inventory) {
        this.aura = aura;
        this.inventory = inventory;
    }

    @Override
    public void addedToEngine(Engine engine) {
        elders = engine.getEntitiesFor(Family.all(ElderComponent.class, ColonistComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (elders == null) {
            return;
        }
        aura.reset();
        for (int i = 0; i < elders.size(); i++) {
            Entity entity = elders.get(i);
            ElderComponent elder = elderMapper.get(entity);
            aura.apply(elder.profile);
            elder.ritualTimer += deltaTime;
            if (elder.ritualTimer >= elder.profile.ritualInterval) {
                elder.ritualTimer = 0f;
                inventory.add(elder.profile.ritualResource, 1);
            }
        }
    }
}
