package com.muisca.town;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.muisca.colony.Colonist;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.ForceComponent;
import com.muisca.ecs.components.StatusComponent;
import com.muisca.world.DayCycle;

public class TownLifeSystem extends EntitySystem {

    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<TownLifeComponent> townMapper = ComponentMapper.getFor(TownLifeComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private final ComponentMapper<ForceComponent> forceMapper = ComponentMapper.getFor(ForceComponent.class);
    private ImmutableArray<Entity> citizens;
    private final DayCycle dayCycle;
    private final Vector2 temp = new Vector2();

    public TownLifeSystem(DayCycle dayCycle) {
        this.dayCycle = dayCycle;
    }

    @Override
    public void addedToEngine(Engine engine) {
        citizens = engine.getEntitiesFor(Family.all(ColonistComponent.class, TownLifeComponent.class, ForceComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (citizens == null) {
            return;
        }
        boolean gathering = dayCycle.isBetween(0.35f, 0.58f);
        for (int i = 0; i < citizens.size(); i++) {
            Entity entity = citizens.get(i);
            Colonist colonist = colonistMapper.get(entity).colonist;
            StatusComponent status = statusMapper.get(entity);
            if (status != null && status.isStaggered()) {
                continue;
            }
            TownLifeComponent town = townMapper.get(entity);
            Vector2 target = gathering ? town.plazaAnchor : town.homeAnchor;
            float dst2 = colonist.getPosition().dst2(target);
            if (dst2 < 36f) {
                continue;
            }
            ForceComponent force = forceMapper.get(entity);
            if (force == null) {
                continue;
            }
            Vector2 direction = temp.set(target).sub(colonist.getPosition()).nor().scl(40f);
            force.velocity.add(direction);
        }
    }
}
