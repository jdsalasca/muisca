package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.muisca.enemies.EnemyArchetype;

public class EnemyComponent implements Component {
    public final EnemyArchetype archetype;
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public float attackTimer = 0f;
    public float thinkTimer = 0f;

    public EnemyComponent(EnemyArchetype archetype, float x, float y) {
        this.archetype = archetype;
        this.position.set(x, y);
    }
}
