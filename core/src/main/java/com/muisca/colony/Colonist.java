package com.muisca.colony;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.muisca.crafting.Recipe;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.muisca.world.WorldMap;

/**
 * Data model + simple behaviour helpers for colonists used inside the ECS systems.
 */
public class Colonist {

    public enum TaskType {
        IDLE,
        HARVEST,
        CRAFT
    }

    private final String name;
    private final Vector2 position = new Vector2();
    private final Vector2 wanderTarget = new Vector2();
    private final Vector2 taskTarget = new Vector2();
    private final Vector2 temp = new Vector2();
    private final ColonistSkills skills = new ColonistSkills();
    private final ObjectIntMap<String> pack = new ObjectIntMap<>();
    private final int packCapacity = 12;
    private String role = "Colono";

    private float hunger = 0.35f;
    private float spirit = 0.8f;
    private float fatigue = 0.25f;
    private float thinkTimer = 0f;

    private TaskType currentTask = TaskType.IDLE;
    private float taskProgress = 0f;
    private int jobId = -1;
    private Recipe activeRecipe;

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

    public ColonistSkills getSkills() {
        return skills;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public TaskType getCurrentTask() {
        return currentTask;
    }

    public boolean hasActiveTask() {
        return currentTask != TaskType.IDLE;
    }

    public int getJobId() {
        return jobId;
    }

    public Vector2 getTaskTarget() {
        return taskTarget;
    }

    public float getTaskProgress() {
        return taskProgress;
    }

    public void applyInput(Vector2 direction, float delta, float speed, WorldMap map, int tileSize) {
        if (direction.isZero(0.001f)) {
            rest(delta);
            return;
        }
        temp.set(direction).nor().scl(speed * delta);
        move(temp.x, temp.y, map, tileSize);
        fatigue = MathUtils.clamp(fatigue + delta * 0.05f, 0f, 1f);
        hunger = MathUtils.clamp(hunger + delta * 0.02f, 0f, 1f);
    }

    public void updateAutonomy(float delta, WorldMap map, int tileSize) {
        thinkTimer -= delta;
        if (thinkTimer <= 0f || position.dst2(wanderTarget) < 64f) {
            chooseNewTarget();
            thinkTimer = MathUtils.random(2f, 4.5f);
        }
        temp.set(wanderTarget).sub(position).limit(70f * delta);
        move(temp.x, temp.y, map, tileSize);
        hunger = MathUtils.clamp(hunger + delta * 0.01f, 0f, 1f);
        fatigue = MathUtils.clamp(fatigue - delta * 0.02f, 0f, 1f);
        spirit = MathUtils.clamp(spirit - delta * 0.005f + MathUtils.random(-0.002f, 0.002f), 0f, 1f);
    }

    public boolean updateHarvestTask(float delta, WorldMap map, int tileSize) {
        temp.set(taskTarget).sub(position);
        if (temp.len2() > 25f) {
            temp.limit(90f * delta);
            move(temp.x, temp.y, map, tileSize);
            fatigue = MathUtils.clamp(fatigue + delta * 0.01f, 0f, 1f);
            hunger = MathUtils.clamp(hunger + delta * 0.005f, 0f, 1f);
            return false;
        }
        taskProgress += delta * skills.getGatherMultiplier();
        if (taskProgress >= 1.2f) {
            spirit = MathUtils.clamp(spirit + 0.08f, 0f, 1f);
            hunger = MathUtils.clamp(hunger - 0.08f, 0f, 1f);
            fatigue = MathUtils.clamp(fatigue + 0.02f, 0f, 1f);
            return true;
        }
        return false;
    }

    public void assignHarvestTask(int jobId, float x, float y) {
        this.jobId = jobId;
        this.currentTask = TaskType.HARVEST;
        this.taskTarget.set(x, y);
        this.taskProgress = 0f;
        this.activeRecipe = null;
    }

    public boolean updateCraftTask(float delta, WorldMap map, int tileSize) {
        if (activeRecipe == null) {
            return true;
        }
        temp.set(taskTarget).sub(position);
        if (temp.len2() > 16f) {
            temp.limit(85f * delta);
            move(temp.x, temp.y, map, tileSize);
            return false;
        }
        taskProgress += delta * skills.getCraftMultiplier();
        return taskProgress >= activeRecipe.getWorkTime();
    }

    public void assignCraftTask(int jobId, Recipe recipe, float x, float y) {
        this.jobId = jobId;
        this.currentTask = TaskType.CRAFT;
        this.activeRecipe = recipe;
        this.taskTarget.set(x, y);
        this.taskProgress = 0f;
    }

    public void clearTask() {
        currentTask = TaskType.IDLE;
        jobId = -1;
        taskProgress = 0f;
        activeRecipe = null;
    }

    public Recipe getActiveRecipe() {
        return activeRecipe;
    }

    public void adjustSpirit(float delta) {
        spirit = MathUtils.clamp(spirit + delta, 0f, 1f);
    }

    public int getPackLoad() {
        int total = 0;
        for (ObjectIntMap.Entry<String> entry : pack.entries()) {
            total += entry.value;
        }
        return total;
    }

    public int storeInPack(String itemId, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int load = getPackLoad();
        int space = Math.max(0, packCapacity - load);
        int stored = Math.min(space, amount);
        if (stored > 0) {
            pack.getAndIncrement(itemId, 0, stored);
        }
        return stored;
    }

    public ObjectIntMap<String> drainPack() {
        ObjectIntMap<String> snapshot = new ObjectIntMap<>();
        for (ObjectIntMap.Entry<String> entry : pack.entries()) {
            snapshot.put(entry.key, entry.value);
        }
        pack.clear();
        return snapshot;
    }

    public ObjectIntMap<String> peekPack() {
        return pack;
    }

    private void move(float dx, float dy, WorldMap map, int tileSize) {
        // Attempt axis-separated movement, preventing entry into non-passable tiles (e.g., water).
        float targetX = position.x + dx;
        float targetY = position.y + dy;

        // Try X movement
        if (dx != 0f) {
            float tryX = MathUtils.clamp(targetX, 0f, map.getWidth() * tileSize);
            int tileX = (int) (tryX / tileSize);
            int tileY = (int) (position.y / tileSize);
            if (map.getTile(tileX, tileY).isPassable()) {
                position.x = tryX;
            }
        }
        // Try Y movement
        if (dy != 0f) {
            float tryY = MathUtils.clamp(targetY, 0f, map.getHeight() * tileSize);
            int tileX = (int) (position.x / tileSize);
            int tileY = (int) (tryY / tileSize);
            if (map.getTile(tileX, tileY).isPassable()) {
                position.y = tryY;
            }
        }
        clampToWorld(map, tileSize);
    }

    public void applyImpulse(float dx, float dy, WorldMap map, int tileSize) {
        move(dx, dy, map, tileSize);
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
