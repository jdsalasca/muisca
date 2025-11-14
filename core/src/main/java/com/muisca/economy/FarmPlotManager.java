package com.muisca.economy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.muisca.inventory.Inventory;

/**
 * Controls farm plots, their rotations and job reservations.
 */
public class FarmPlotManager {

    public static class FarmTask {
        public final int plotId;
        public final FarmTaskType type;
        public final String cropId;
        public final Vector2 position = new Vector2();

        public FarmTask(int plotId, FarmTaskType type, String cropId, Vector2 position) {
            this.plotId = plotId;
            this.type = type;
            this.cropId = cropId;
            this.position.set(position);
        }
    }

    public enum FarmTaskType {
        PLANT,
        HARVEST
    }

    /**
     * Snapshot of farm status used by HUD/telemetry without allocating new arrays.
     */
    public static class FarmMetrics {
        public int fallow;
        public int growing;
        public int ready;
        public int total;
        public float averageGrowth;

        public void reset() {
            fallow = 0;
            growing = 0;
            ready = 0;
            total = 0;
            averageGrowth = 0f;
        }
    }

    private final CropLibrary cropLibrary;
    private final Array<CropDefinition> cropRotation;
    private final Array<FarmPlot> plots = new Array<>();
    private int nextPlotId = 1;
    private int cropCursor = 0;

    public FarmPlotManager(CropLibrary cropLibrary) {
        this.cropLibrary = cropLibrary;
        this.cropRotation = cropLibrary.all();
    }

    public FarmPlot addPlot(float x, float y) {
        FarmPlot plot = new FarmPlot(nextPlotId++, x, y);
        plot.setPlannedCropId(nextCropId());
        plots.add(plot);
        return plot;
    }

    public void spawnDefaultPlots(Vector2 center, int count, float spacing) {
        clear();
        int perRow = Math.max(1, count / 2);
        int created = 0;
        float startX = center.x - (perRow - 1) * spacing * 0.5f;
        float startY = center.y + spacing;
        for (int row = 0; row < 2 && created < count; row++) {
            for (int col = 0; col < perRow && created < count; col++) {
                float x = startX + col * spacing;
                float y = startY + row * spacing;
                addPlot(x, y);
                created++;
            }
        }
    }

    public void clear() {
        plots.clear();
        nextPlotId = 1;
        cropCursor = 0;
    }

    public void addRestoredPlot(FarmPlot plot) {
        if (plot.getPlannedCropId() == null) {
            plot.setPlannedCropId(nextCropId());
        }
        plots.add(plot);
        nextPlotId = Math.max(nextPlotId, plot.getId() + 1);
        cropCursor = Math.max(0, Math.min(cropCursor, cropRotation.size - 1));
    }

    public Array<FarmPlot> getPlots() {
        return plots;
    }

    public FarmTask reservePlantingTask() {
        for (FarmPlot plot : plots) {
            if (plot.getState() == FarmPlot.State.FALLOW && !plot.isReserved()) {
                if (plot.getPlannedCropId() == null) {
                    plot.setPlannedCropId(nextCropId());
                }
                plot.setReserved(true);
                return new FarmTask(plot.getId(), FarmTaskType.PLANT, plot.getPlannedCropId(), plot.getPosition());
            }
        }
        return null;
    }

    public FarmTask reserveHarvestTask() {
        for (FarmPlot plot : plots) {
            if (plot.getState() == FarmPlot.State.READY && !plot.isReserved()) {
                plot.setReserved(true);
                return new FarmTask(plot.getId(), FarmTaskType.HARVEST, plot.getCropId(), plot.getPosition());
            }
        }
        return null;
    }

    public void release(int plotId) {
        FarmPlot plot = findPlot(plotId);
        if (plot != null) {
            plot.setReserved(false);
        }
    }

    public void completePlant(int plotId) {
        FarmPlot plot = findPlot(plotId);
        if (plot == null) {
            return;
        }
        if (plot.getPlannedCropId() == null) {
            plot.setPlannedCropId(nextCropId());
        }
        plot.setCropId(plot.getPlannedCropId());
        plot.setState(FarmPlot.State.GROWING);
        plot.setGrowth(0f);
        plot.setReserved(false);
    }

    public void completeHarvest(int plotId, Inventory inventory) {
        FarmPlot plot = findPlot(plotId);
        if (plot == null || plot.getCropId() == null) {
            return;
        }
        CropDefinition crop = cropLibrary.get(plot.getCropId());
        if (crop != null) {
            for (ObjectIntMap.Entry<String> output : crop.getOutputs().entries()) {
                inventory.add(output.key, output.value);
            }
        }
        plot.setState(FarmPlot.State.FALLOW);
        plot.setCropId(null);
        plot.setGrowth(0f);
        plot.setReserved(false);
        plot.setPlannedCropId(nextCropId());
    }

    public void update(float delta, Season season) {
        for (FarmPlot plot : plots) {
            if (plot.getState() != FarmPlot.State.GROWING || plot.getCropId() == null) {
                continue;
            }
            CropDefinition crop = cropLibrary.get(plot.getCropId());
            if (crop == null) {
                plot.setState(FarmPlot.State.FALLOW);
                plot.setCropId(null);
                plot.setGrowth(0f);
                continue;
            }
            float seasonMultiplier = crop.getSeasonMultiplier(season);
            float growth = plot.getGrowth() + (delta / Math.max(1f, crop.getGrowthSeconds())) * seasonMultiplier;
            plot.setGrowth(MathUtils.clamp(growth, 0f, 1f));
            if (plot.getGrowth() >= 1f) {
                plot.setState(FarmPlot.State.READY);
            }
        }
    }

    /**
     * Computes farm metrics (counts per state + average growth) into the provided buffer.
     */
    public FarmMetrics captureMetrics(FarmMetrics out) {
        FarmMetrics metrics = out == null ? new FarmMetrics() : out;
        metrics.reset();
        if (plots.size == 0) {
            return metrics;
        }
        float progress = 0f;
        for (FarmPlot plot : plots) {
            switch (plot.getState()) {
                case FALLOW:
                    metrics.fallow++;
                    break;
                case GROWING:
                    metrics.growing++;
                    break;
                case READY:
                    metrics.ready++;
                    break;
                default:
                    break;
            }
            progress += plot.getGrowth();
        }
        metrics.total = plots.size;
        metrics.averageGrowth = progress / plots.size;
        return metrics;
    }

    private FarmPlot findPlot(int id) {
        for (FarmPlot plot : plots) {
            if (plot.getId() == id) {
                return plot;
            }
        }
        return null;
    }

    private String nextCropId() {
        if (cropRotation.size == 0) {
            return null;
        }
        CropDefinition def = cropRotation.get(cropCursor);
        cropCursor = (cropCursor + 1) % cropRotation.size;
        return def.getId();
    }
}
