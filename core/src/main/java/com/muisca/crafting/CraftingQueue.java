package com.muisca.crafting;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.muisca.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class CraftingQueue {

    public static class CraftingJob {
        public final int jobId;
        public final Recipe recipe;
        public final Vector2 workstation;
        public boolean reserved = false;
        public boolean completed = false;

        CraftingJob(int jobId, Recipe recipe, Vector2 workstation) {
            this.jobId = jobId;
            this.recipe = recipe;
            this.workstation = new Vector2(workstation);
        }
    }

    private final RecipeBook recipeBook;
    private final Inventory inventory;
    private final Vector2 workstation;
    private final Array<CraftingJob> jobs = new Array<>();
    private int nextId = 1;
    private final Map<Integer, CraftingJob> jobLookup = new HashMap<>();

    public CraftingQueue(RecipeBook recipeBook, Inventory inventory, Vector2 workstation) {
        this.recipeBook = recipeBook;
        this.inventory = inventory;
        this.workstation = workstation;
    }

    public boolean requestCraft(String recipeId) {
        Recipe recipe = recipeBook.get(recipeId);
        if (recipe == null) {
            return false;
        }
        if (!inventory.consumeAll(recipe.getInputs())) {
            return false;
        }
        CraftingJob job = new CraftingJob(nextId++, recipe, workstation);
        jobs.add(job);
        jobLookup.put(job.jobId, job);
        return true;
    }

    public CraftingJob reserveJob() {
        for (CraftingJob job : jobs) {
            if (!job.completed && !job.reserved) {
                job.reserved = true;
                return job;
            }
        }
        return null;
    }

    public void releaseJob(int jobId) {
        CraftingJob job = jobLookup.get(jobId);
        if (job != null && job.reserved && !job.completed) {
            job.reserved = false;
        }
    }

    public void completeJob(int jobId) {
        CraftingJob job = jobLookup.get(jobId);
        if (job == null || job.completed) {
            return;
        }
        job.completed = true;
        for (com.badlogic.gdx.utils.ObjectIntMap.Entry<String> entry : job.recipe.getOutputs().entries()) {
            inventory.add(entry.key, entry.value);
        }
    }

    public Array<CraftingJob> getJobs() {
        return jobs;
    }
}
