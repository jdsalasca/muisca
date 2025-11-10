package com.muisca.world;

/**
 * Immutable tile grid representing the generated overworld.
 */
public class WorldMap {

    private final int width;
    private final int height;
    private final int chunkSize;
    private final TileType[][] tiles;

    public WorldMap(int width, int height, int chunkSize, TileType[][] tiles) {
        this.width = width;
        this.height = height;
        this.chunkSize = chunkSize;
        this.tiles = tiles;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return TileType.BOSQUE_TEMPLADO;
        }
        return tiles[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChunkSize() {
        return chunkSize;
    }
}
