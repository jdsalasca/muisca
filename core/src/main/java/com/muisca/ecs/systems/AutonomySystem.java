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
import com.muisca.ecs.components.StatusComponent;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.economy.FarmPlotManager;
import com.muisca.inventory.Inventory;
import com.muisca.jobs.JobBoard;
import com.muisca.world.WorldMap;

public class AutonomySystem extends IteratingSystem {

    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<InputControlComponent> inputMapper = ComponentMapper.getFor(InputControlComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final WorldMap worldMap;
    private final int tileSize;
    private final JobBoard jobBoard;
    private final CraftingQueue craftingQueue;
    private final Inventory inventory;
    private final FarmPlotManager farmPlotManager;

    public AutonomySystem(WorldMap worldMap, int tileSize, JobBoard jobBoard,
                          CraftingQueue craftingQueue, Inventory inventory,
                          FarmPlotManager farmPlotManager) {
        super(Family.all(ColonistComponent.class, AutonomyComponent.class).get());
        this.worldMap = worldMap;
        this.tileSize = tileSize;
        this.jobBoard = jobBoard;
        this.craftingQueue = craftingQueue;
        this.inventory = inventory;
        this.farmPlotManager = farmPlotManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InputControlComponent input = inputMapper.get(entity);
        if (input != null && input.selected) {
            return;
        }
        StatsComponent stats = statsMapper.get(entity);
        if (stats != null && !stats.stats.isAlive()) {
            return;
        }
        StatusComponent status = statusMapper.get(entity);
        if (status != null && status.isStaggered()) {
            return;
        }
        float speedMultiplier = status != null ? status.getMovementMultiplier() : 1f;
        float scaledDelta = deltaTime * Math.max(0.1f, speedMultiplier);
        Colonist colonist = colonistMapper.get(entity).colonist;
        if (!colonist.hasActiveTask()) {
            colonist.updateAutonomy(scaledDelta, worldMap, tileSize);
            return;
        }
        if (colonist.getCurrentTask() == TaskType.HARVEST) {
            boolean finished = colonist.updateHarvestTask(scaledDelta, worldMap, tileSize);
            if (finished) {
                jobBoard.completeJob(colonist.getJobId());
                inventory.add("raw_wood", 1);
                colonist.clearTask();
            }
        } else if (colonist.getCurrentTask() == TaskType.CRAFT) {
            boolean finished = colonist.updateCraftTask(scaledDelta, worldMap, tileSize);
            if (finished) {
                craftingQueue.completeJob(colonist.getJobId());
                colonist.clearTask();
            }
        } else if (colonist.getCurrentTask() == TaskType.FARM_PLANT) {
            boolean finished = colonist.updateFarmTask(scaledDelta, worldMap, tileSize);
            if (finished) {
                farmPlotManager.completePlant(colonist.getJobId());
                colonist.clearTask();
            }
        } else if (colonist.getCurrentTask() == TaskType.FARM_HARVEST) {
            boolean finished = colonist.updateFarmTask(scaledDelta, worldMap, tileSize);
            if (finished) {
                farmPlotManager.completeHarvest(colonist.getJobId(), inventory);
                colonist.clearTask();
            }
        }
    }
}
