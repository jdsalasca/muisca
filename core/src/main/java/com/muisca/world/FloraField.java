package com.muisca.world;

import com.badlogic.gdx.math.MathUtils;

public class FloraField {

    private final float[][] grass;
    private final float[][] sprouts;
    private final int width;
    private final int height;

    public FloraField(int width, int height) {
        this.width = width;
        this.height = height;
        this.grass = new float[width][height];
        this.sprouts = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grass[x][y] = MathUtils.random(0.6f, 1f);
                sprouts[x][y] = MathUtils.random(0.2f, 0.8f);
            }
        }
    }

    public void decayCircle(float worldX, float worldY, float radiusTiles, float amount) {
        applyCircle(worldX, worldY, radiusTiles, -amount);
    }

    public void regrowCircle(float worldX, float worldY, float radiusTiles, float amount) {
        applyCircle(worldX, worldY, radiusTiles, amount);
    }

    public void applyCircle(float worldX, float worldY, float radiusTiles, float amount) {
        int minX = MathUtils.clamp((int) (worldX - radiusTiles), 0, width - 1);
        int maxX = MathUtils.clamp((int) (worldX + radiusTiles), 0, width - 1);
        int minY = MathUtils.clamp((int) (worldY - radiusTiles), 0, height - 1);
        int maxY = MathUtils.clamp((int) (worldY + radiusTiles), 0, height - 1);
        float radius2 = radiusTiles * radiusTiles;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                float dx = x - worldX;
                float dy = y - worldY;
                if (dx * dx + dy * dy > radius2) {
                    continue;
                }
                float delta = amount * MathUtils.random(0.6f, 1.1f);
                grass[x][y] = MathUtils.clamp(grass[x][y] + delta, 0f, 1.2f);
                sprouts[x][y] = MathUtils.clamp(sprouts[x][y] + delta * 0.7f, 0f, 1.2f);
            }
        }
    }

    public float sampleGrass(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return 1f;
        }
        return MathUtils.clamp(grass[x][y], 0f, 1f);
    }

    public float sampleSprouts(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return 1f;
        }
        return MathUtils.clamp(sprouts[x][y], 0f, 1f);
    }

    public void randomRegrow(float amount) {
        int x = MathUtils.random(0, width - 1);
        int y = MathUtils.random(0, height - 1);
        grass[x][y] = MathUtils.clamp(grass[x][y] + amount, 0f, 1.2f);
        sprouts[x][y] = MathUtils.clamp(sprouts[x][y] + amount * 0.6f, 0f, 1.2f);
    }
}
