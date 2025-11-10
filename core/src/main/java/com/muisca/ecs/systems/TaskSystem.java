package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.muisca.colony.Colonist;
import com.muisca.crafting.CraftingQueue;
import com.muisca.crafting.CraftingQueue.CraftingJob;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.InputControlComponent;
import com.muisca.ecs.components.StatusComponent;
import com.muisca.ecs.components.TaskComponent;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.jobs.JobBoard;

public class TaskSystem extends EntitySystem {

    private final JobBoard jobBoard;
    private final CraftingQueue craftingQueue;
    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<InputControlComponent> inputMapper = ComponentMapper.getFor(InputControlComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private ImmutableArray<Entity> entities;
    private final Vector2 temp = new Vector2();

    public TaskSystem(JobBoard jobBoard, CraftingQueue craftingQueue) {
        this.jobBoard = jobBoard;
        this.craftingQueue = craftingQueue;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(ColonistComponent.class, TaskComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            InputControlComponent input = inputMapper.get(entity);
            if (input != null && input.selected) {
                continue;
            }
            StatsComponent stats = statsMapper.get(entity);
            if (stats != null && !stats.stats.isAlive()) {
                continue;
            }
            StatusComponent status = statusMapper.get(entity);
            if (status != null && status.isStaggered()) {
                continue;
            }
            Colonist colonist = colonistMapper.get(entity).colonist;
            if (colonist.hasActiveTask()) {
                continue;
            }
            CraftingJob craftJob = craftingQueue.reserveJob();
            if (craftJob != null) {
                colonist.assignCraftTask(craftJob.jobId, craftJob.recipe,
                        craftJob.workstation.x, craftJob.workstation.y);
                continue;
            }
            int jobId = jobBoard.reserveSite();
            if (jobId < 0) {
                continue;
            }
            jobBoard.getSitePosition(jobId, temp);
            colonist.assignHarvestTask(jobId, temp.x, temp.y);
        }
    }
}
