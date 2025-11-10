package com.muisca.colony;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.muisca.world.WorldMap;

import java.util.Random;

/**
 * Simple autonomous colonist used for the prototype simulation.
 */
public class Colonist {

    private static final Random RANDOM = new Random(9_873_211L);

    private final String name;
    private final Vector2 position = new Vector2();
    private final Vector2 wanderTarget = new Vector2();

    private float hunger = 0.3f;
    private float spirit = 0.8f;
    private float fatigue = 0.2f;
    private float thinkTimer = 0f;

    public Colonist(String name, float startX, float startY) {
        this.name = name;
        this.position.set(startX, startY);
        chooseNewTarget();
    }

    public String getName() {
        return name;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getHunger() {
        return hunger;
    }

    public float getSpirit() {
        return spirit;
    }

    public float getFatigue() {
        return fatigue;
    }

    public void applyInput(Vector2 direction, float delta, float speed, WorldMap map, int tileSize) {
        if (direction.isZero(0.001f)) {
            rest(delta);
            return;
        }
        direction.nor().scl(speed * delta);
        move(direction.x, direction.y, map, tileSize);
        fatigue = MathUtils.clamp(fatigue + delta * 0.05f, 0f, 1f);
        hunger = MathUtils.clamp(hunger + delta * 0.02f, 0f, 1f);
    }

    public void updateAutonomy(float delta, WorldMap map, int tileSize) {
        thinkTimer -= delta;
        if (thinkTimer <= 0f || position.dst2(wanderTarget) < 64f) {
            chooseNewTarget();
            thinkTimer = MathUtils.random(2f, 4.5f);
        }
        Vector2 desired = new Vector2(wanderTarget).sub(position).limit(70f * delta);
        move(desired.x, desired.y, map, tileSize);
        hunger = MathUtils.clamp(hunger + delta * 0.01f, 0f, 1f);
        fatigue = MathUtils.clamp(fatigue - delta * 0.02f, 0f, 1f);
        spirit = MathUtils.clamp(spirit - delta * 0.005f + MathUtils.random(-0.002f, 0.002f), 0f, 1f);
    }

    private void move(float dx, float dy, WorldMap map, int tileSize) {
        position.add(dx, dy);
        clampToWorld(map, tileSize);
    }

    private void clampToWorld(WorldMap map, int tileSize) {
        float maxX = map.getWidth() * tileSize;
        float maxY = map.getHeight() * tileSize;
        position.x = MathUtils.clamp(position.x, 0, maxX);
        position.y = MathUtils.clamp(position.y, 0, maxY);
    }

    private void rest(float delta) {
        fatigue = MathUtils.clamp(fatigue - delta * 0.04f, 0f, 1f);
        spirit = MathUtils.clamp(spirit + delta * 0.01f, 0f, 1f);
        hunger = MathUtils.clamp(hunger + delta * 0.005f, 0f, 1f);
    }

    private void chooseNewTarget() {
        float range = MathUtils.random(64f, 220f);
        float angle = MathUtils.random(0f, MathUtils.PI2);
        wanderTarget.set(position.x + MathUtils.cos(angle) * range,
                position.y + MathUtils.sin(angle) * range);
    }
}
