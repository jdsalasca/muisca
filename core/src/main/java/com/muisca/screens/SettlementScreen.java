package com.muisca.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.muisca.MuiscaGame;

import java.util.Random;

/**
 * Minimal vertical slice for v0.0.1 showing movement and placeholder world tiles.
 */
public class SettlementScreen extends ScreenAdapter implements Disposable {

    private static final int WORLD_WIDTH_TILES = 40;
    private static final int WORLD_HEIGHT_TILES = 25;
    private static final int TILE_SIZE = 32;

    private final MuiscaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final BitmapFont font;

    private final Texture[] biomeTiles;
    private final Texture colonistTexture;

    private float playerX = WORLD_WIDTH_TILES * TILE_SIZE / 2f;
    private float playerY = WORLD_HEIGHT_TILES * TILE_SIZE / 2f;
    private float stamina = 1f;
    private float dayTimer = 0f;

    public SettlementScreen(MuiscaGame game) {
        this.game = game;
        this.batch = game.getSharedBatch();
        this.camera = new OrthographicCamera(1280, 720);
        this.font = new BitmapFont();
        this.biomeTiles = createBiomeTextures();
        this.colonistTexture = createColonistTexture();
    }

    @Override
    public void render(float delta) {
        update(delta);

        Color sky = getSkyColor();
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTiles();
        batch.draw(colonistTexture, playerX - 16, playerY - 16);
        drawHud();
        batch.end();
    }

    private void update(float delta) {
        float speed = 200f;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed *= 1.35f;
        }

        float dx = 0;
        float dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1f;

        if (dx != 0 || dy != 0) {
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= len;
            dy /= len;
            playerX = MathUtils.clamp(playerX + dx * speed * delta, 0, WORLD_WIDTH_TILES * TILE_SIZE);
            playerY = MathUtils.clamp(playerY + dy * speed * delta, 0, WORLD_HEIGHT_TILES * TILE_SIZE);
            stamina = MathUtils.clamp(stamina - delta * 0.05f, 0f, 1f);
        } else {
            stamina = MathUtils.clamp(stamina + delta * 0.1f, 0f, 1f);
        }

        dayTimer = (dayTimer + delta * 0.1f) % 1f;
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    private Color getSkyColor() {
        float intensity = MathUtils.sin(dayTimer * MathUtils.PI2) * 0.5f + 0.5f;
        return new Color(0.08f + intensity * 0.2f, 0.09f + intensity * 0.25f, 0.12f + intensity * 0.35f, 1f);
    }

    private void drawTiles() {
        for (int y = 0; y < WORLD_HEIGHT_TILES; y++) {
            for (int x = 0; x < WORLD_WIDTH_TILES; x++) {
                Texture tile = biomeTiles[(x + y) % biomeTiles.length];
                batch.draw(tile, x * TILE_SIZE, y * TILE_SIZE);
            }
        }
    }

    private void drawHud() {
        String info = "WASD mover | Shift correr | Espacio pausa (placeholder)\n"
                + "Colonos: 2 | Karma neutro | Stamina: " + MathUtils.round(stamina * 100) + "%";
        font.draw(batch, info, camera.position.x - 620, camera.position.y + 330);
    }

    private Texture[] createBiomeTextures() {
        Color[] palette = {
                new Color(0.38f, 0.45f, 0.24f, 1f), // bosque
                new Color(0.52f, 0.49f, 0.35f, 1f), // paramo
                new Color(0.2f, 0.25f, 0.3f, 1f),   // umbra
                new Color(0.46f, 0.36f, 0.28f, 1f)  // estepa
        };
        Texture[] textures = new Texture[palette.length];
        Random random = new Random(42);
        for (int i = 0; i < palette.length; i++) {
            Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
            Color base = palette[i];
            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    float noise = (random.nextFloat() - 0.5f) * 0.05f;
                    pixmap.setColor(base.r + noise, base.g + noise, base.b + noise, 1f);
                    pixmap.drawPixel(x, y);
                }
            }
            textures[i] = new Texture(pixmap);
            pixmap.dispose();
        }
        return textures;
    }

    private Texture createColonistTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.88f, 0.74f, 0.42f, 1f));
        pixmap.fillRectangle(8, 10, 16, 20);
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        pixmap.fillRectangle(10, 6, 12, 6);
        pixmap.setColor(new Color(0.15f, 0.25f, 0.4f, 1f));
        pixmap.fillRectangle(11, 24, 4, 8);
        pixmap.fillRectangle(17, 24, 4, 8);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture biomeTile : biomeTiles) {
            biomeTile.dispose();
        }
        colonistTexture.dispose();
        font.dispose();
    }
}
