package com.muisca.screens;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
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
import com.muisca.ecs.components.AutonomyComponent;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.InputControlComponent;
import com.muisca.ecs.components.TaskComponent;
import com.muisca.ecs.systems.AutonomySystem;
import com.muisca.ecs.systems.InputMovementSystem;
import com.muisca.ecs.systems.TaskSystem;
import com.muisca.jobs.JobBoard;
import com.muisca.jobs.JobBoard.HarvestSite;
import com.muisca.world.TileType;
import com.muisca.world.WorldGenerator;
import com.muisca.world.WorldMap;

/**
 * v0.0.3 slice showing ECS-powered colonists, harvesting queue, and procedural world.
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
    private final Engine engine;
    private final Array<Entity> colonistEntities = new Array<>();
    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<InputControlComponent> inputMapper = ComponentMapper.getFor(InputControlComponent.class);
    private final JobBoard jobBoard;
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
        this.jobBoard = new JobBoard(worldMap, TILE_SIZE, 14);

        this.engine = new Engine();
        engine.addSystem(new InputMovementSystem(worldMap, TILE_SIZE));
        engine.addSystem(new AutonomySystem(worldMap, TILE_SIZE, jobBoard));
        engine.addSystem(new TaskSystem(jobBoard));

        createColonist("Ama", WORLD_WIDTH_TILES * TILE_SIZE / 2f, WORLD_HEIGHT_TILES * TILE_SIZE / 2f);
        createColonist("Quyca", WORLD_WIDTH_TILES * TILE_SIZE / 2f + 96, WORLD_HEIGHT_TILES * TILE_SIZE / 2f + 32);
        createColonist("Suaga", WORLD_WIDTH_TILES * TILE_SIZE / 2f - 80, WORLD_HEIGHT_TILES * TILE_SIZE / 2f - 64);
        selectColonist(0);

        this.ambientTrack = Gdx.audio.newMusic(Gdx.files.internal("audio/proto_theme.wav"));
        ambientTrack.setLooping(true);
        ambientTrack.setVolume(0.35f);
        ambientTrack.play();
    }

    private void createColonist(String name, float x, float y) {
        Entity entity = new Entity();
        entity.add(new ColonistComponent(new Colonist(name, x, y)));
        entity.add(new InputControlComponent());
        entity.add(new AutonomyComponent());
        entity.add(new TaskComponent());
        engine.addEntity(entity);
        colonistEntities.add(entity);
    }

    @Override
    public void render(float delta) {
        updateGame(delta);

        Color sky = getSkyColor();
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawWorld();
        drawColonists();
        drawHud(delta);
        batch.end();

        drawOverlays();
    }

    private void updateGame(float delta) {
        handleToggles();
        applyInput(delta);
        engine.update(delta);
        updateCamera();
        dayTimer = (dayTimer + delta * 0.04f) % 1f;
    }

    private void handleToggles() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showChunks = !showChunks;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebug = !showDebug;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            int next = (controlledColonistIndex + 1) % colonistEntities.size;
            selectColonist(next);
        }
    }

    private void applyInput(float delta) {
        inputDirection.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDirection.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDirection.y -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDirection.x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDirection.x += 1f;

        float speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 300f : 190f;

        for (int i = 0; i < colonistEntities.size; i++) {
            Entity entity = colonistEntities.get(i);
            InputControlComponent input = inputMapper.get(entity);
            if (i == controlledColonistIndex) {
                input.direction.set(inputDirection);
                input.intendedSpeed = inputDirection.isZero(0.001f) ? 0f : speed;
            } else {
                input.direction.setZero();
                input.intendedSpeed = 0f;
            }
        }
    }

    private void updateCamera() {
        Entity selected = colonistEntities.get(controlledColonistIndex);
        Colonist colonist = colonistMapper.get(selected).colonist;
        float maxX = worldMap.getWidth() * TILE_SIZE;
        float maxY = worldMap.getHeight() * TILE_SIZE;
        camera.position.set(colonist.getPosition(), 0f);
        camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth / 2f, maxX - camera.viewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight / 2f, maxY - camera.viewportHeight / 2f);
        camera.update();
    }

    private void selectColonist(int index) {
        controlledColonistIndex = MathUtils.clamp(index, 0, colonistEntities.size - 1);
        for (int i = 0; i < colonistEntities.size; i++) {
            InputControlComponent input = inputMapper.get(colonistEntities.get(i));
            input.selected = i == controlledColonistIndex;
            Colonist colonist = colonistMapper.get(colonistEntities.get(i)).colonist;
            if (input.selected && colonist.hasActiveTask()) {
                jobBoard.releaseJob(colonist.getJobId());
                colonist.clearTask();
            }
            if (!input.selected) {
                input.direction.setZero();
                input.intendedSpeed = 0f;
            }
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
        for (int i = 0; i < colonistEntities.size; i++) {
            Entity entity = colonistEntities.get(i);
            Colonist colonist = colonistMapper.get(entity).colonist;
            InputControlComponent input = inputMapper.get(entity);
            if (input.selected) {
                batch.setColor(Color.WHITE);
            } else {
                batch.setColor(1f, 1f, 1f, 0.85f);
            }
            batch.draw(colonistTexture, colonist.getPosition().x - 16, colonist.getPosition().y - 16);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawHud(float delta) {
        StringBuilder builder = new StringBuilder();
        builder.append("Muisca v0.0.3 | Tab colonos | WASD mover | Shift correr | C grilla | F1 debug\n");
        builder.append("Jobs activos: ").append(jobBoard.getActiveReservations())
                .append(" | Árboles restantes: ").append(jobBoard.getRemainingSites())
                .append(" | Música activa | delta ").append(MathUtils.round(delta * 1000)).append(" ms\n");

        for (int i = 0; i < colonistEntities.size; i++) {
            Colonist colonist = colonistMapper.get(colonistEntities.get(i)).colonist;
            builder.append(i == controlledColonistIndex ? "> " : "  ");
            builder.append(colonist.getName())
                    .append(" | Hambre ").append(MathUtils.round(colonist.getHunger() * 100)).append("%")
                    .append(" | Espíritu ").append(MathUtils.round(colonist.getSpirit() * 100)).append("%")
                    .append(" | Fatiga ").append(MathUtils.round(colonist.getFatigue() * 100)).append("%")
                    .append(" | Tarea ").append(colonist.getCurrentTask()).append('\n');
        }
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 340);
        if (showDebug) {
            font.draw(batch,
                    "Cam " + MathUtils.floor(camera.position.x) + "," + MathUtils.floor(camera.position.y)
                            + " | Chunk " + worldMap.getChunkSize()
                            + " | Day " + MathUtils.round(dayTimer * 100),
                    camera.position.x - 620,
                    camera.position.y - 330);
        }
    }

    private void drawOverlays() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        if (showChunks) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (HarvestSite site : jobBoard.getSites()) {
            if (site.harvested) {
                shapeRenderer.setColor(0.3f, 0.35f, 0.35f, 0.7f);
            } else if (site.reserved) {
                shapeRenderer.setColor(0.9f, 0.67f, 0.2f, 0.8f);
            } else {
                shapeRenderer.setColor(0.3f, 0.8f, 0.4f, 0.8f);
            }
            shapeRenderer.circle(site.position.x, site.position.y, 6f, 12);
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
