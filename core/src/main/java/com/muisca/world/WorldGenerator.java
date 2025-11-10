package com.muisca.world;

import com.badlogic.gdx.math.MathUtils;

/**
 * Deterministic tile generator using layered trigonometric noise.
 */
public class WorldGenerator {

    private final int width;
    private final int height;
    private final int chunkSize;
    private final long seed;

    public WorldGenerator(int width, int height, int chunkSize, long seed) {
        this.width = width;
        this.height = height;
        this.chunkSize = chunkSize;
        this.seed = seed;
    }

    public WorldMap generate() {
        TileType[][] tiles = new TileType[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float elevation = fbm(x, y, 0.045f, 4, 0.55f);
                float humidity = fbm(x + 239, y - 411, 0.07f, 3, 0.65f);
                elevation = normalize(elevation);
                humidity = normalize(humidity);
                tiles[x][y] = TileType.fromSample(elevation, humidity);
            }
        }
        return new WorldMap(width, height, chunkSize, tiles);
    }

    private float fbm(float x, float y, float scale, int octaves, float gain) {
        float value = 0f;
        float amplitude = 1f;
        float frequency = scale;
        for (int i = 0; i < octaves; i++) {
            value += amplitude * sample(x * frequency, y * frequency);
            frequency *= 2f;
            amplitude *= gain;
        }
        return value;
    }

    private float sample(float x, float y) {
        double s = Math.sin((x + seed) * 0.8);
        double c = Math.cos((y - seed) * 0.6);
        double mix = Math.sin((x + y) * 0.35 + seed * 0.0002);
        return (float) (0.5 * s + 0.35 * c + 0.15 * mix);
    }

    private float normalize(float value) {
        return MathUtils.clamp((value + 1f) * 0.5f, 0f, 1f);
    }
}
