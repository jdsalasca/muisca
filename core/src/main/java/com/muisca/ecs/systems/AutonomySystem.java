package com.muisca.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.muisca.colony.Colonist;
import com.muisca.colony.Colonist.TaskType;
import com.muisca.crafting.CraftingQueue;
import com.muisca.ecs.components.AutonomyComponent;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.InputControlComponent;
import com.muisca.inventory.Inventory;
import com.muisca.jobs.JobBoard;
import com.muisca.world.WorldMap;

public class AutonomySystem extends IteratingSystem {

    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<InputControlComponent> inputMapper = ComponentMapper.getFor(InputControlComponent.class);
    private final WorldMap worldMap;
    private final int tileSize;
    private final JobBoard jobBoard;
    private final CraftingQueue craftingQueue;
    private final Inventory inventory;

    public AutonomySystem(WorldMap worldMap, int tileSize, JobBoard jobBoard,
                          CraftingQueue craftingQueue, Inventory inventory) {
        super(Family.all(ColonistComponent.class, AutonomyComponent.class).get());
        this.worldMap = worldMap;
        this.tileSize = tileSize;
        this.jobBoard = jobBoard;
        this.craftingQueue = craftingQueue;
        this.inventory = inventory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InputControlComponent input = inputMapper.get(entity);
        if (input != null && input.selected) {
            return;
        }
        Colonist colonist = colonistMapper.get(entity).colonist;
        if (!colonist.hasActiveTask()) {
            colonist.updateAutonomy(deltaTime, worldMap, tileSize);
            return;
        }
        if (colonist.getCurrentTask() == TaskType.HARVEST) {
            boolean finished = colonist.updateHarvestTask(deltaTime, worldMap, tileSize);
            if (finished) {
                jobBoard.completeJob(colonist.getJobId());
                inventory.add("raw_wood", 1);
                colonist.clearTask();
            }
        } else if (colonist.getCurrentTask() == TaskType.CRAFT) {
            boolean finished = colonist.updateCraftTask(deltaTime, worldMap, tileSize);
            if (finished) {
                craftingQueue.completeJob(colonist.getJobId());
                colonist.clearTask();
            }
        }
    }
}
