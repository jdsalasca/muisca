package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.muisca.colony.Colonist;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.InputControlComponent;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.world.WorldMap;

public class InputMovementSystem extends IteratingSystem {

    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<InputControlComponent> inputMapper = ComponentMapper.getFor(InputControlComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final WorldMap worldMap;
    private final int tileSize;
    private final Vector2 temp = new Vector2();

    public InputMovementSystem(WorldMap worldMap, int tileSize) {
        super(Family.all(ColonistComponent.class, InputControlComponent.class).get());
        this.worldMap = worldMap;
        this.tileSize = tileSize;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InputControlComponent input = inputMapper.get(entity);
        Colonist colonist = colonistMapper.get(entity).colonist;
        StatsComponent stats = statsMapper.get(entity);
        if (stats != null && !stats.stats.isAlive()) {
            input.direction.setZero();
            input.intendedSpeed = 0f;
            return;
        }

        if (!input.selected) {
            return;
        }
        temp.set(input.direction);
        colonist.applyInput(temp, deltaTime, input.intendedSpeed, worldMap, tileSize);
    }
}
