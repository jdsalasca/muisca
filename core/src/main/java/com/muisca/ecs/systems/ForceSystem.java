package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.muisca.colony.Colonist;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.EnemyComponent;
import com.muisca.ecs.components.ForceComponent;
import com.muisca.world.WorldMap;

public class ForceSystem extends IteratingSystem {

    private final ComponentMapper<ForceComponent> forceMapper = ComponentMapper.getFor(ForceComponent.class);
    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<EnemyComponent> enemyMapper = ComponentMapper.getFor(EnemyComponent.class);
    private final WorldMap worldMap;
    private final int tileSize;
    private final Vector2 temp = new Vector2();

    public ForceSystem(WorldMap worldMap, int tileSize) {
        super(Family.all(ForceComponent.class).get());
        this.worldMap = worldMap;
        this.tileSize = tileSize;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ForceComponent force = forceMapper.get(entity);
        if (force == null || force.velocity.isZero(0.1f)) {
            force.velocity.setZero();
            return;
        }
        temp.set(force.velocity).scl(deltaTime);
        ColonistComponent colonist = colonistMapper.get(entity);
        if (colonist != null) {
            colonist.colonist.applyImpulse(temp.x, temp.y, worldMap, tileSize);
        } else {
            EnemyComponent enemy = enemyMapper.get(entity);
            if (enemy != null) {
                enemy.position.add(temp);
                clamp(enemy.position);
            }
        }
        float damping = MathUtils.clamp(1f - 4f * deltaTime, 0f, 1f);
        force.velocity.scl(damping);
        if (force.velocity.len2() < 1f) {
            force.velocity.setZero();
        }
    }

    private void clamp(Vector2 position) {
        float maxX = worldMap.getWidth() * tileSize;
        float maxY = worldMap.getHeight() * tileSize;
        position.x = MathUtils.clamp(position.x, 0f, maxX);
        position.y = MathUtils.clamp(position.y, 0f, maxY);
    }
}
