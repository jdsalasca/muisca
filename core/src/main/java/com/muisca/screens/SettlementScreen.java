package com.muisca.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.muisca.MuiscaGame;
import com.muisca.colony.Colonist;
import com.muisca.world.TileType;
import com.muisca.world.WorldGenerator;
import com.muisca.world.WorldMap;

/**
 * v0.0.2 slice showing procedural biomes, multiple colonists, and ambient music.
 */
public class SettlementScreen extends ScreenAdapter implements Disposable {

    private static final int TILE_SIZE = 32;
    private static final int WORLD_WIDTH_TILES = 96;
    private static final int WORLD_HEIGHT_TILES = 96;
    private static final int CHUNK_SIZE = 16;

    private final MuiscaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;

    private final WorldMap worldMap;
    private final Texture[] tileTextures;
    private final Texture colonistTexture;
    private final Array<Colonist> colonists;
    private final Music ambientTrack;

    private final Vector2 inputDirection = new Vector2();

    private float dayTimer = 0f;
    private boolean showChunks = true;
    private boolean showDebug = false;
    private int controlledColonistIndex = 0;

    public SettlementScreen(MuiscaGame game) {
        this.game = game;
        this.batch = game.getSharedBatch();
        this.camera = new OrthographicCamera(1280, 720);
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();

        this.worldMap = new WorldGenerator(WORLD_WIDTH_TILES, WORLD_HEIGHT_TILES, CHUNK_SIZE, 140_921L).generate();
        this.tileTextures = createTileTextures();
        this.colonistTexture = createColonistTexture();
        this.colonists = createColonists();

        this.ambientTrack = Gdx.audio.newMusic(Gdx.files.internal("audio/proto_theme.wav"));
        ambientTrack.setLooping(true);
        ambientTrack.setVolume(0.35f);
        ambientTrack.play();
    }

    @Override
    public void render(float delta) {
        update(delta);

        Color sky = getSkyColor();
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawWorld();
        drawColonists();
        drawHud(delta);
        batch.end();

        if (showChunks) {
            drawChunkGrid();
        }
    }

    private void update(float delta) {
        handleToggles();
        Colonist player = colonists.get(controlledColonistIndex);

        inputDirection.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDirection.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDirection.y -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDirection.x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDirection.x += 1f;

        float speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 280f : 180f;
        player.applyInput(inputDirection, delta, speed, worldMap, TILE_SIZE);

        for (int i = 0; i < colonists.size; i++) {
            if (i == controlledColonistIndex) {
                continue;
            }
            colonists.get(i).updateAutonomy(delta, worldMap, TILE_SIZE);
        }

        dayTimer = (dayTimer + delta * 0.04f) % 1f;

        float maxX = worldMap.getWidth() * TILE_SIZE;
        float maxY = worldMap.getHeight() * TILE_SIZE;
        camera.position.set(player.getPosition(), 0f);
        camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth / 2f, maxX - camera.viewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight / 2f, maxY - camera.viewportHeight / 2f);
        camera.update();
    }

    private void handleToggles() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showChunks = !showChunks;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebug = !showDebug;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            controlledColonistIndex = (controlledColonistIndex + 1) % colonists.size;
        }
    }

    private void drawWorld() {
        for (int x = 0; x < worldMap.getWidth(); x++) {
            for (int y = 0; y < worldMap.getHeight(); y++) {
                TileType type = worldMap.getTile(x, y);
                Texture texture = tileTextures[type.ordinal()];
                batch.draw(texture, x * TILE_SIZE, y * TILE_SIZE);
            }
        }
    }

    private void drawColonists() {
        for (int i = 0; i < colonists.size; i++) {
            Colonist colonist = colonists.get(i);
            boolean selected = i == controlledColonistIndex;
            if (selected) {
                batch.setColor(Color.WHITE);
            } else {
                batch.setColor(1f, 1f, 1f, 0.8f);
            }
            batch.draw(colonistTexture, colonist.getPosition().x - 16, colonist.getPosition().y - 16);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawHud(float delta) {
        StringBuilder builder = new StringBuilder();
        builder.append("Muisca v0.0.2 | WASD mover | Shift correr | Tab cambiar colono | C grilla | F1 debug\n");
        builder.append("Biomas generados: ").append(TileType.values().length)
                .append(" | Música ambiental activa | delta ").append(MathUtils.round(delta * 1000)).append(" ms\n");
        for (int i = 0; i < colonists.size; i++) {
            Colonist colonist = colonists.get(i);
            builder.append(i == controlledColonistIndex ? "> " : "  ");
            builder.append(colonist.getName())
                    .append(" | Hambre ")
                    .append(MathUtils.round(colonist.getHunger() * 100))
                    .append("% | Espíritu ")
                    .append(MathUtils.round(colonist.getSpirit() * 100))
                    .append("% | Fatiga ")
                    .append(MathUtils.round(colonist.getFatigue() * 100))
                    .append("%\n");
        }
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 340);
        builder.setLength(0);
        if (showDebug) {
            builder.append("Cam ")
                    .append(MathUtils.floor(camera.position.x)).append(",")
                    .append(MathUtils.floor(camera.position.y))
                    .append(" | Chunk size ").append(worldMap.getChunkSize())
                    .append(" | Day timer ").append(MathUtils.round(dayTimer * 100));
            font.draw(batch, builder, camera.position.x - 620, camera.position.y - 320);
        }
    }

    private void drawChunkGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 0.1f);
        for (int x = 0; x <= worldMap.getWidth(); x += worldMap.getChunkSize()) {
            float worldX = x * TILE_SIZE;
            shapeRenderer.line(worldX, 0, worldX, worldMap.getHeight() * TILE_SIZE);
        }
        for (int y = 0; y <= worldMap.getHeight(); y += worldMap.getChunkSize()) {
            float worldY = y * TILE_SIZE;
            shapeRenderer.line(0, worldY, worldMap.getWidth() * TILE_SIZE, worldY);
        }
        shapeRenderer.end();
    }

    private Texture[] createTileTextures() {
        TileType[] values = TileType.values();
        Texture[] textures = new Texture[values.length];
        for (TileType type : values) {
            Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
            Color base = type.getColor();
            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    float noise = MathUtils.random(-0.04f, 0.04f);
                    pixmap.setColor(base.r + noise, base.g + noise, base.b + noise, 1f);
                    pixmap.drawPixel(x, y);
                }
            }
            textures[type.ordinal()] = new Texture(pixmap);
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

    private Array<Colonist> createColonists() {
        Array<Colonist> list = new Array<>();
        float centerX = WORLD_WIDTH_TILES * TILE_SIZE / 2f;
        float centerY = WORLD_HEIGHT_TILES * TILE_SIZE / 2f;
        list.add(new Colonist("Ama", centerX, centerY));
        list.add(new Colonist("Quyca", centerX + 96, centerY + 32));
        list.add(new Colonist("Suaga", centerX - 80, centerY - 64));
        return list;
    }

    private Color getSkyColor() {
        float intensity = MathUtils.sin(dayTimer * MathUtils.PI2) * 0.5f + 0.5f;
        return new Color(0.07f + intensity * 0.25f, 0.09f + intensity * 0.25f, 0.12f + intensity * 0.35f, 1f);
    }

    @Override
    public void dispose() {
        for (Texture texture : tileTextures) {
            texture.dispose();
        }
        colonistTexture.dispose();
        font.dispose();
        shapeRenderer.dispose();
        if (ambientTrack != null) {
            ambientTrack.dispose();
        }
    }
}
