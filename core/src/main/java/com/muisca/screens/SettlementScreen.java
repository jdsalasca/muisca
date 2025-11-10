package com.muisca.screens;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.muisca.MuiscaGame;
import com.muisca.colony.Colonist;
import com.muisca.colony.Colonist.TaskType;
import com.muisca.crafting.CraftingQueue;
import com.muisca.crafting.Recipe;
import com.muisca.crafting.RecipeBook;
import com.muisca.decisions.DecisionEngine;
import com.muisca.decisions.DecisionGraph;
import com.muisca.decisions.DecisionNode;
import com.muisca.decisions.DecisionState;
import com.muisca.combat.CombatStats;
import com.muisca.combat.DamageTelemetry;
import com.muisca.combat.SpellDefinition;
import com.muisca.combat.SpellLibrary;
import com.muisca.combat.StatusEffect;
import com.muisca.combat.StatusEffectInstance;
import com.muisca.combat.TalentId;
import com.muisca.ecs.components.AutonomyComponent;
import com.muisca.ecs.components.CombatIdentityComponent;
import com.muisca.ecs.components.ColonistComponent;
import com.muisca.ecs.components.EnemyComponent;
import com.muisca.ecs.components.ForceComponent;
import com.muisca.ecs.components.InputControlComponent;
import com.muisca.ecs.components.PlayerCombatComponent;
import com.muisca.ecs.components.SpellbookComponent;
import com.muisca.ecs.components.StatsComponent;
import com.muisca.ecs.components.StatusComponent;
import com.muisca.ecs.components.TaskComponent;
import com.muisca.ecs.components.TalentComponent;
import com.muisca.ecs.systems.AutonomySystem;
import com.muisca.ecs.systems.CombatResourceSystem;
import com.muisca.ecs.systems.EnemyAISystem;
import com.muisca.ecs.systems.ForceSystem;
import com.muisca.ecs.systems.InputMovementSystem;
import com.muisca.ecs.systems.PlayerCombatSystem;
import com.muisca.ecs.systems.StatusSystem;
import com.muisca.ecs.systems.TaskSystem;
import com.muisca.inventory.Inventory;
import com.muisca.jobs.JobBoard;
import com.muisca.jobs.JobBoard.HarvestSite;
import com.muisca.reputation.ReputationTracker;
import com.muisca.save.SaveData;
import com.muisca.save.SaveManager;
import com.muisca.save.SaveManager.CombatSnapshot;
import com.muisca.structures.StructureBlueprint;
import com.muisca.structures.StructureInstance;
import com.muisca.structures.StructureLibrary;
import com.muisca.structures.StructureManager;
import com.muisca.world.TileType;
import com.muisca.world.WorldGenerator;
import com.muisca.world.WorldMap;
import com.muisca.enemies.EnemyArchetype;
import com.muisca.enemies.EnemyFactory;

/**
 * v0.0.6 slice – colonos ECS + combate/magia y encuentro prototipo.
 */
public class SettlementScreen extends ScreenAdapter implements Disposable {

    private static final int TILE_SIZE = 32;
    private static final int WORLD_WIDTH_TILES = 96;
    private static final int WORLD_HEIGHT_TILES = 96;
    private static final int CHUNK_SIZE = 16;
    private static final Color ENEMY_COLOR = new Color(0.9f, 0.35f, 0.35f, 0.95f);
    private static final Color ENEMY_DEFEATED_COLOR = new Color(0.35f, 0.35f, 0.35f, 0.7f);
    private static final Color HP_BAR_BG = new Color(0f, 0f, 0f, 0.65f);
    private static final Color HP_BAR_PLAYER = new Color(0.25f, 0.85f, 0.35f, 0.9f);
    private static final Color HP_BAR_ENEMY = new Color(0.9f, 0.4f, 0.2f, 0.9f);
    private static final Color STAMINA_BAR_COLOR = new Color(0.2f, 0.6f, 0.95f, 0.85f);

    private final MuiscaGame game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;

    private final WorldMap worldMap;
    private final Texture[] tileTextures;
    private final Texture colonistTexture;
    private final float worldCenterX;
    private final float worldCenterY;

    private final Engine engine;
    private final Array<Entity> colonistEntities = new Array<>();
    private final ComponentMapper<ColonistComponent> colonistMapper = ComponentMapper.getFor(ColonistComponent.class);
    private final ComponentMapper<InputControlComponent> inputMapper = ComponentMapper.getFor(InputControlComponent.class);
    private final ComponentMapper<StatsComponent> statsMapper = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<StatusComponent> statusMapper = ComponentMapper.getFor(StatusComponent.class);
    private final ComponentMapper<PlayerCombatComponent> playerCombatMapper = ComponentMapper.getFor(PlayerCombatComponent.class);
    private final ComponentMapper<CombatIdentityComponent> identityMapper = ComponentMapper.getFor(CombatIdentityComponent.class);
    private final ComponentMapper<EnemyComponent> enemyMapper = ComponentMapper.getFor(EnemyComponent.class);
    private final ComponentMapper<SpellbookComponent> spellbookMapper = ComponentMapper.getFor(SpellbookComponent.class);
    private final ComponentMapper<TalentComponent> talentMapper = ComponentMapper.getFor(TalentComponent.class);

    private final JobBoard jobBoard;
    private final Inventory inventory = new Inventory();
    private final RecipeBook recipeBook;
    private final CraftingQueue craftingQueue;
    private final StructureLibrary structureLibrary;
    private final StructureManager structureManager;
    private final Vector2 craftStation;
    private final SpellLibrary spellLibrary;
    private final EnemyFactory enemyFactory;
    private final ReputationTracker reputationTracker = new ReputationTracker();
    private final DecisionState decisionState = new DecisionState();
    private final DecisionEngine decisionEngine;
    private final SaveManager saveManager = new SaveManager("slot1");
    private final DamageTelemetry damageTelemetry;

    private Music ambientTrack;
    private final Vector2 inputDirection = new Vector2();
    private float dayTimer = 0f;
    private boolean showChunks = true;
    private boolean showDebug = false;
    private int controlledColonistIndex = 0;
    private String statusMessage = "";
    private float statusTimer = 0f;
    private boolean decisionVisible = false;
    private final Array<Entity> enemyEntities = new Array<>();
    private final Texture enemyTexture;

    public SettlementScreen(MuiscaGame game) {
        this.game = game;
        this.batch = game.getSharedBatch();
        this.camera = new OrthographicCamera(1280, 720);
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        FileHandle telemetryDir = Gdx.files.local("telemetry");
        if (!telemetryDir.exists()) {
            telemetryDir.mkdirs();
        }
        FileHandle telemetryFile = telemetryDir.child("damage.log");
        this.damageTelemetry = new DamageTelemetry(telemetryFile);

        this.worldMap = new WorldGenerator(WORLD_WIDTH_TILES, WORLD_HEIGHT_TILES, CHUNK_SIZE, 140_921L).generate();
        this.tileTextures = createTileTextures();
        this.colonistTexture = createColonistTexture();
        this.enemyTexture = createEnemyTexture();
        float centerX = WORLD_WIDTH_TILES * TILE_SIZE / 2f;
        float centerY = WORLD_HEIGHT_TILES * TILE_SIZE / 2f;
        this.worldCenterX = centerX;
        this.worldCenterY = centerY;
        this.craftStation = new Vector2(centerX + 72f, centerY);

        this.spellLibrary = SpellLibrary.load(Gdx.files.internal("data/spells"));
        this.enemyFactory = new EnemyFactory(spellLibrary);
        this.jobBoard = new JobBoard(worldMap, TILE_SIZE, 18);
        this.recipeBook = RecipeBook.load(Gdx.files.internal("data/recipes/woodworking.json"));
        this.craftingQueue = new CraftingQueue(recipeBook, inventory, craftStation);
        this.structureLibrary = StructureLibrary.load(Gdx.files.internal("data/structures/basic.json"));
        this.structureManager = new StructureManager(structureLibrary, inventory);
        DecisionGraph decisionGraph = DecisionGraph.load(Gdx.files.internal("data/decisions/bridge_toll.json"));
        this.decisionEngine = new DecisionEngine(decisionGraph, inventory, reputationTracker, decisionState);

        this.engine = new Engine();
        engine.addSystem(new InputMovementSystem(worldMap, TILE_SIZE));
        engine.addSystem(new AutonomySystem(worldMap, TILE_SIZE, jobBoard, craftingQueue, inventory));
        engine.addSystem(new TaskSystem(jobBoard, craftingQueue));
        engine.addSystem(new CombatResourceSystem());
        engine.addSystem(new PlayerCombatSystem(damageTelemetry));
        engine.addSystem(new EnemyAISystem(damageTelemetry));
        engine.addSystem(new ForceSystem(worldMap, TILE_SIZE));
        engine.addSystem(new StatusSystem(damageTelemetry));

        createColonist("Ama", centerX, centerY, TalentId.CENIZA_DISCIPLINE, TalentId.JURAMENTO_WARD);
        createColonist("Quyca", centerX + 96, centerY + 32, TalentId.CENIZA_PYRE);
        createColonist("Suaga", centerX - 80, centerY - 64, TalentId.VANGUARD_TRAINING);
        selectColonist(0);

        resetEncounter();

        CombatSnapshot snapshot = saveManager.read(inventory, structureManager, jobBoard, reputationTracker, decisionState);
        craftingQueue.clearJobs();
        applyCombatSnapshot(snapshot);

        ambientTrack = Gdx.audio.newMusic(Gdx.files.internal("audio/proto_theme.wav"));
        ambientTrack.setLooping(true);
        ambientTrack.setVolume(0.4f);
        ambientTrack.play();
    }

    private void createColonist(String name, float x, float y, TalentId... talents) {
        Entity entity = new Entity();
        Colonist colonist = new Colonist(name, x, y);
        entity.add(new ColonistComponent(colonist));
        entity.add(new InputControlComponent());
        entity.add(new AutonomyComponent());
        entity.add(new TaskComponent());
        entity.add(new CombatIdentityComponent(name, CombatIdentityComponent.Faction.PLAYER));
        entity.add(new ForceComponent());
        StatsComponent stats = new StatsComponent(CombatStats.colonistBaseline());
        TalentComponent talentComponent = new TalentComponent();
        if (talents != null) {
            for (TalentId talent : talents) {
                if (talent != null) {
                    talentComponent.addTalent(talent);
                    talent.apply(stats.stats);
                }
            }
        }
        entity.add(talentComponent);
        entity.add(stats);
        SpellbookComponent spellbook = new SpellbookComponent();
        assignDefaultSpells(spellbook);
        entity.add(spellbook);
        entity.add(new PlayerCombatComponent());
        entity.add(new StatusComponent());
        engine.addEntity(entity);
        colonistEntities.add(entity);
    }

    private void assignDefaultSpells(SpellbookComponent spellbook) {
        addSpellToBook(spellbook, "ember_burst");
        addSpellToBook(spellbook, "oath_bind");
    }

    private void addSpellToBook(SpellbookComponent spellbook, String id) {
        if (spellLibrary == null) {
            return;
        }
        SpellDefinition definition = spellLibrary.get(id);
        if (definition != null) {
            spellbook.addSpell(definition);
        }
    }

    private void resetEncounter() {
        clearEnemies();
        spawnEncounter();
    }

    private void spawnEncounter() {
        spawnEnemy(EnemyArchetype.CENIZA_ACOLYTE, worldCenterX + 320f, worldCenterY + 120f);
        spawnEnemy(EnemyArchetype.JURAMENTO_SENTINEL, worldCenterX + 280f, worldCenterY - 80f);
        spawnEnemy(EnemyArchetype.CENIZA_REVENANT, worldCenterX - 260f, worldCenterY + 150f);
        spawnEnemy(EnemyArchetype.WARDEN_OF_UNITY, worldCenterX + 30f, worldCenterY + 260f);
    }

    private void spawnEnemy(EnemyArchetype archetype, float x, float y) {
        Entity entity = enemyFactory.createEnemy(archetype, x, y);
        engine.addEntity(entity);
        enemyEntities.add(entity);
    }

    private CombatSnapshot captureCombatSnapshot() {
        CombatSnapshot snapshot = new CombatSnapshot();
        for (Entity entity : colonistEntities) {
            SaveData.SaveColonist saveColonist = new SaveData.SaveColonist();
            Colonist colonist = colonistMapper.get(entity).colonist;
            saveColonist.name = colonist.getName();
            saveColonist.x = colonist.getPosition().x;
            saveColonist.y = colonist.getPosition().y;
            StatsComponent stats = statsMapper.get(entity);
            if (stats != null) {
                saveColonist.health = stats.stats.getHealth();
                saveColonist.stamina = stats.stats.getStaminaPool();
                saveColonist.focus = stats.stats.getFocusPool();
                saveColonist.defeated = !stats.stats.isAlive();
            }
            PlayerCombatComponent combat = playerCombatMapper.get(entity);
            if (combat != null) {
                saveColonist.globalCooldown = combat.globalCooldown;
            }
            TalentComponent talents = talentMapper.get(entity);
            if (talents != null) {
                for (TalentId talent : talents.talents) {
                    saveColonist.talents.add(talent.name());
                }
            }
            SpellbookComponent spellbook = spellbookMapper.get(entity);
            if (spellbook != null) {
                for (SpellbookComponent.SpellSlot slot : spellbook.slots) {
                    SaveData.SaveSpellSlot slotSave = new SaveData.SaveSpellSlot();
                    slotSave.spellId = slot.spell.id;
                    slotSave.cooldown = slot.cooldownRemaining;
                    saveColonist.spells.add(slotSave);
                }
            }
            StatusComponent statusComponent = statusMapper.get(entity);
            if (statusComponent != null) {
                for (StatusEffectInstance instance : statusComponent.statuses) {
                    SaveData.SaveCombatStatus saveStatus = new SaveData.SaveCombatStatus();
                    saveStatus.effect = instance.effect.name();
                    saveStatus.remaining = instance.remaining;
                    saveStatus.potency = instance.potency;
                    saveStatus.tickInterval = instance.tickInterval;
                    saveStatus.tickTimer = instance.tickTimer;
                    saveStatus.source = instance.sourceName;
                    saveColonist.statuses.add(saveStatus);
                }
            }
            snapshot.colonists.add(saveColonist);
        }
        for (Entity entity : enemyEntities) {
            SaveData.SaveEnemy saveEnemy = new SaveData.SaveEnemy();
            EnemyComponent enemyComponent = enemyMapper.get(entity);
            if (enemyComponent != null) {
                saveEnemy.archetypeId = enemyComponent.archetype.name();
                saveEnemy.x = enemyComponent.position.x;
                saveEnemy.y = enemyComponent.position.y;
            }
            StatsComponent stats = statsMapper.get(entity);
            if (stats != null) {
                saveEnemy.health = stats.stats.getHealth();
                saveEnemy.stamina = stats.stats.getStaminaPool();
                saveEnemy.focus = stats.stats.getFocusPool();
            }
            SpellbookComponent spellbook = spellbookMapper.get(entity);
            if (spellbook != null) {
                for (SpellbookComponent.SpellSlot slot : spellbook.slots) {
                    SaveData.SaveSpellSlot slotSave = new SaveData.SaveSpellSlot();
                    slotSave.spellId = slot.spell.id;
                    slotSave.cooldown = slot.cooldownRemaining;
                    saveEnemy.spells.add(slotSave);
                }
            }
            StatusComponent statusComponent = statusMapper.get(entity);
            if (statusComponent != null) {
                for (StatusEffectInstance instance : statusComponent.statuses) {
                    SaveData.SaveCombatStatus saveStatus = new SaveData.SaveCombatStatus();
                    saveStatus.effect = instance.effect.name();
                    saveStatus.remaining = instance.remaining;
                    saveStatus.potency = instance.potency;
                    saveStatus.tickInterval = instance.tickInterval;
                    saveStatus.tickTimer = instance.tickTimer;
                    saveStatus.source = instance.sourceName;
                    saveEnemy.statuses.add(saveStatus);
                }
            }
            snapshot.enemies.add(saveEnemy);
        }
        return snapshot;
    }

    private void applyCombatSnapshot(CombatSnapshot snapshot) {
        if (snapshot == null) {
            resetEncounter();
            return;
        }
        boolean hasCombatData = snapshot.colonists.size > 0 || snapshot.enemies.size > 0;
        if (!hasCombatData) {
            resetEncounter();
            return;
        }
        ObjectMap<String, Entity> colonistByName = new ObjectMap<>();
        for (Entity entity : colonistEntities) {
            Colonist colonist = colonistMapper.get(entity).colonist;
            colonistByName.put(colonist.getName(), entity);
        }
        for (SaveData.SaveColonist saveColonist : snapshot.colonists) {
            Entity entity = colonistByName.get(saveColonist.name);
            if (entity == null) {
                continue;
            }
            Colonist colonist = colonistMapper.get(entity).colonist;
            colonist.getPosition().set(saveColonist.x, saveColonist.y);
            colonist.clearTask();
            StatsComponent stats = statsMapper.get(entity);
            if (stats != null) {
                stats.stats.setPools(saveColonist.health, saveColonist.stamina, saveColonist.focus);
                stats.defeated = !stats.stats.isAlive();
            }
            PlayerCombatComponent combat = playerCombatMapper.get(entity);
            if (combat != null) {
                combat.globalCooldown = saveColonist.globalCooldown;
            }
            SpellbookComponent spellbook = spellbookMapper.get(entity);
            if (spellbook != null && saveColonist.spells.size > 0) {
                syncSpellCooldowns(spellbook, saveColonist.spells);
            }
            StatusComponent statusComponent = statusMapper.get(entity);
            if (statusComponent != null) {
                restoreStatuses(statusComponent, saveColonist.statuses);
            }
        }
        if (snapshot.enemies.size > 0) {
            rebuildEnemies(snapshot.enemies);
        } else {
            clearEnemies();
        }
    }

    private void syncSpellCooldowns(SpellbookComponent spellbook, Array<SaveData.SaveSpellSlot> slots) {
        ObjectMap<String, SpellbookComponent.SpellSlot> byId = new ObjectMap<>();
        for (SpellbookComponent.SpellSlot slot : spellbook.slots) {
            if (slot.spell != null) {
                byId.put(slot.spell.id, slot);
            }
        }
        for (SaveData.SaveSpellSlot slotState : slots) {
            if (slotState.spellId == null) {
                continue;
            }
            SpellbookComponent.SpellSlot slot = byId.get(slotState.spellId);
            if (slot == null) {
                SpellDefinition definition = spellLibrary.get(slotState.spellId);
                if (definition != null) {
                    SpellbookComponent.SpellSlot newSlot = new SpellbookComponent.SpellSlot(definition);
                    newSlot.cooldownRemaining = slotState.cooldown;
                    spellbook.slots.add(newSlot);
                }
                continue;
            }
            slot.cooldownRemaining = slotState.cooldown;
        }
    }

    private void rebuildEnemies(Array<SaveData.SaveEnemy> savedEnemies) {
        clearEnemies();
        for (SaveData.SaveEnemy saveEnemy : savedEnemies) {
            spawnEnemyFromState(saveEnemy);
        }
    }

    private void clearEnemies() {
        for (Entity enemy : enemyEntities) {
            engine.removeEntity(enemy);
        }
        enemyEntities.clear();
    }

    private void spawnEnemyFromState(SaveData.SaveEnemy saveEnemy) {
        if (saveEnemy == null || saveEnemy.archetypeId == null) {
            return;
        }
        EnemyArchetype archetype;
        try {
            archetype = EnemyArchetype.valueOf(saveEnemy.archetypeId);
        } catch (IllegalArgumentException ex) {
            return;
        }
        Entity entity = enemyFactory.createEnemy(archetype, saveEnemy.x, saveEnemy.y);
        engine.addEntity(entity);
        enemyEntities.add(entity);
        StatsComponent stats = statsMapper.get(entity);
        if (stats != null) {
            stats.stats.setPools(saveEnemy.health, saveEnemy.stamina, saveEnemy.focus);
            stats.defeated = !stats.stats.isAlive();
        }
        SpellbookComponent spellbook = spellbookMapper.get(entity);
        if (spellbook != null && saveEnemy.spells.size > 0) {
            syncSpellCooldowns(spellbook, saveEnemy.spells);
        }
        StatusComponent statusComponent = statusMapper.get(entity);
        if (statusComponent != null) {
            restoreStatuses(statusComponent, saveEnemy.statuses);
        }
    }

    private void restoreStatuses(StatusComponent statusComponent, Array<SaveData.SaveCombatStatus> statuses) {
        statusComponent.statuses.clear();
        if (statuses == null || statuses.size == 0) {
            return;
        }
        for (SaveData.SaveCombatStatus saveStatus : statuses) {
            if (saveStatus.effect == null) {
                continue;
            }
            StatusEffect effect;
            try {
                effect = StatusEffect.valueOf(saveStatus.effect);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            StatusEffectInstance instance = new StatusEffectInstance(effect,
                    Math.max(0f, saveStatus.remaining),
                    saveStatus.potency,
                    saveStatus.tickInterval,
                    saveStatus.source);
            instance.remaining = saveStatus.remaining;
            instance.tickTimer = saveStatus.tickTimer;
            statusComponent.statuses.add(instance);
        }
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
        drawStructures();
        drawEnemies();
        drawColonists();
        drawHud(delta);
        batch.end();

        drawOverlays();
    }

    private void updateGame(float delta) {
        handleToggles();
        handleSaveLoadInput();
        handleActions();
        applyInput(delta);
        engine.update(delta);
        damageTelemetry.update(delta);
        updateCamera();
        dayTimer = (dayTimer + delta * 0.04f) % 1f;
        statusTimer = Math.max(0f, statusTimer - delta);
    }

    private void handleToggles() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showChunks = !showChunks;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebug = !showDebug;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            selectColonist((controlledColonistIndex + 1) % colonistEntities.size);
        }
    }

    private void handleActions() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            toggleDecisionOverlay();
        }
        if (decisionVisible) {
            handleDecisionInput();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            queueRecipe("plank_bundle");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            queueRecipe("camp_bed");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            placeStructure("camp_bed");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            placeStructure("storage_crate");
        }
    }

    private void queueRecipe(String recipeId) {
        Recipe recipe = recipeBook.get(recipeId);
        if (recipe == null) {
            showStatus("Receta desconocida: " + recipeId);
            return;
        }
        if (craftingQueue.requestCraft(recipeId)) {
            showStatus("Pedido de " + recipe.getName() + " registrado.");
        } else {
            showStatus("Recursos insuficientes para " + recipe.getName());
        }
    }

    private void placeStructure(String blueprintId) {
        StructureBlueprint blueprint = structureManager.getBlueprint(blueprintId);
        if (blueprint == null) {
            showStatus("Plano desconocido.");
            return;
        }
        Colonist colonist = colonistMapper.get(colonistEntities.get(controlledColonistIndex)).colonist;
        if (structureManager.place(blueprintId, colonist.getPosition())) {
            showStatus("Construido: " + blueprint.getName());
        } else {
            showStatus("Faltan recursos para " + blueprint.getName());
        }
    }

    private void handleSaveLoadInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            saveManager.write(inventory, structureManager, jobBoard, reputationTracker, decisionState,
                    captureCombatSnapshot());
            showStatus("Partida guardada.");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            CombatSnapshot snapshot = saveManager.read(inventory, structureManager, jobBoard, reputationTracker, decisionState);
            craftingQueue.clearJobs();
            for (Entity entity : colonistEntities) {
                colonistMapper.get(entity).colonist.clearTask();
            }
            decisionVisible = false;
            selectColonist(controlledColonistIndex);
            applyCombatSnapshot(snapshot);
            showStatus("Partida cargada.");
        }
    }

    private void applyInput(float delta) {
        inputDirection.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDirection.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDirection.y -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDirection.x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDirection.x += 1f;
        float speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 320f : 200f;
        for (int i = 0; i < colonistEntities.size; i++) {
            InputControlComponent input = inputMapper.get(colonistEntities.get(i));
            if (i == controlledColonistIndex) {
                input.direction.set(inputDirection);
                input.intendedSpeed = inputDirection.isZero(0.001f) ? 0f : speed;
            } else {
                input.direction.setZero();
                input.intendedSpeed = 0f;
            }
            PlayerCombatComponent combat = playerCombatMapper.get(colonistEntities.get(i));
            StatsComponent stats = statsMapper.get(colonistEntities.get(i));
            if (combat != null) {
                if (decisionVisible || (stats != null && !stats.stats.isAlive())) {
                    combat.resetRequests();
                } else if (i == controlledColonistIndex) {
                    combat.requestPrimary = Gdx.input.isKeyJustPressed(Input.Keys.Q);
                    combat.requestSecondary = Gdx.input.isKeyJustPressed(Input.Keys.E);
                } else {
                    combat.resetRequests();
                }
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
        Entity entity = colonistEntities.get(controlledColonistIndex);
        Colonist colonist = colonistMapper.get(entity).colonist;
        if (colonist.hasActiveTask()) {
            if (colonist.getCurrentTask() == TaskType.HARVEST) {
                jobBoard.releaseJob(colonist.getJobId());
            } else if (colonist.getCurrentTask() == TaskType.CRAFT) {
                craftingQueue.releaseJob(colonist.getJobId());
            }
            colonist.clearTask();
        }
        for (int i = 0; i < colonistEntities.size; i++) {
            InputControlComponent input = inputMapper.get(colonistEntities.get(i));
            input.selected = i == controlledColonistIndex;
            if (!input.selected) {
                input.direction.setZero();
                input.intendedSpeed = 0f;
            }
        }
    }

    private void drawWorld() {
        for (int x = 0; x < worldMap.getWidth(); x++) {
            for (int y = 0; y < worldMap.getHeight(); y++) {
                Texture texture = tileTextures[worldMap.getTile(x, y).ordinal()];
                batch.draw(texture, x * TILE_SIZE, y * TILE_SIZE);
            }
        }
    }

    private void drawStructures() {
        for (StructureInstance instance : structureManager.getInstances()) {
            Rectangle rect = instance.getBounds();
            batch.setColor(instance.getBlueprint().getColor());
            batch.draw(colonistTexture, rect.x, rect.y, rect.width, rect.height);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawEnemies() {
        for (Entity entity : enemyEntities) {
            EnemyComponent enemy = enemyMapper.get(entity);
            if (enemy == null) continue;
            StatsComponent stats = statsMapper.get(entity);
            batch.setColor((stats != null && stats.stats.isAlive()) ? ENEMY_COLOR : ENEMY_DEFEATED_COLOR);
            batch.draw(enemyTexture, enemy.position.x - 16, enemy.position.y - 16);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawColonists() {
        for (int i = 0; i < colonistEntities.size; i++) {
            Colonist colonist = colonistMapper.get(colonistEntities.get(i)).colonist;
            InputControlComponent input = inputMapper.get(colonistEntities.get(i));
            batch.setColor(input.selected ? Color.WHITE : new Color(1f, 1f, 1f, 0.85f));
            batch.draw(colonistTexture, colonist.getPosition().x - 16, colonist.getPosition().y - 16);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawHud(float delta) {
        StringBuilder builder = new StringBuilder();
        builder.append("Muisca v0.0.6 | Tab colonos | WASD mover | Shift correr | Q/E hechizos | 1=Tablones | 2=Cama | B/N planos | C grilla | F1 debug\n");
        builder.append("Inventario: ").append(inventory.summarize()).append('\n');
        builder.append("Pedidos carpintería: ");
        boolean first = true;
        for (CraftingQueue.CraftingJob job : craftingQueue.getJobs()) {
            if (job.completed) continue;
            if (!first) builder.append(" | ");
            builder.append(job.recipe.getName());
            builder.append(job.reserved ? " (en progreso)" : " (pendiente)");
            first = false;
        }
        if (first) builder.append("(sin pedidos)");
        builder.append('\n');
        builder.append("Sitios de tala vivos: ").append(jobBoard.getRemainingSites())
                .append(" | Reservas activas: ").append(jobBoard.getActiveReservations());
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 340);

        if (statusTimer > 0f) {
            font.draw(batch, statusMessage, camera.position.x - 620, camera.position.y + 310);
        }

        builder.setLength(0);
        builder.append("Reputación | Liga: ").append(MathUtils.round(reputationTracker.get("liga")))
                .append(" | Vigías: ").append(MathUtils.round(reputationTracker.get("vigias")))
                .append(" | Brasa: ").append(MathUtils.round(reputationTracker.get("brasa")));
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 285);

        builder.setLength(0);
        for (int i = 0; i < colonistEntities.size; i++) {
            Entity colonistEntity = colonistEntities.get(i);
            Colonist colonist = colonistMapper.get(colonistEntity).colonist;
            StatsComponent stats = statsMapper.get(colonistEntity);
            builder.append(i == controlledColonistIndex ? "> " : "  ");
            builder.append(colonist.getName())
                    .append(" | Hambre ").append(MathUtils.round(colonist.getHunger() * 100)).append("%")
                    .append(" | Espíritu ").append(MathUtils.round(colonist.getSpirit() * 100)).append("%")
                    .append(" | Fatiga ").append(MathUtils.round(colonist.getFatigue() * 100)).append("%");
            if (stats != null) {
                builder.append(" | HP ").append(MathUtils.round(stats.stats.getHealth())).append('/')
                        .append(MathUtils.round(stats.stats.getMaxHealth()))
                        .append(" | STM ").append(MathUtils.round(stats.stats.getStaminaPool())).append('/')
                        .append(MathUtils.round(stats.stats.getMaxStamina()))
                        .append(" | FOC ").append(MathUtils.round(stats.stats.getFocusPool())).append('/')
                        .append(MathUtils.round(stats.stats.getMaxFocus()))
                        .append(" | ATK ").append(MathUtils.round(stats.stats.getAttack()))
                        .append(" | DEF ").append(MathUtils.round(stats.stats.getDefense()))
                        .append(" | RES ").append(MathUtils.round(stats.stats.getResistance()));
                if (!stats.stats.isAlive()) {
                    builder.append(" | ⚠ Derribado");
                }
            }
            builder.append(" | Tarea ").append(colonist.getCurrentTask()).append('\n');
        }
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 260);

        if (decisionVisible) {
            drawDecisionOverlay();
        }

        builder.setLength(0);
        builder.append(damageTelemetry.buildOverlayText());
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 210);

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
        for (StructureInstance instance : structureManager.getInstances()) {
            Rectangle rect = instance.getBounds();
            shapeRenderer.setColor(instance.getBlueprint().getColor());
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.setColor(1f, 0.85f, 0.3f, 0.9f);
        shapeRenderer.circle(craftStation.x, craftStation.y, 6f, 12);
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
        for (int i = 0; i < colonistEntities.size; i++) {
            Entity colonistEntity = colonistEntities.get(i);
            StatsComponent stats = statsMapper.get(colonistEntity);
            if (stats == null) continue;
            Vector2 pos = colonistMapper.get(colonistEntity).colonist.getPosition();
            drawBar(pos.x - 18f, pos.y + 22f, 36f, 3f, stats.stats.getHealthRatio(), HP_BAR_PLAYER);
            drawBar(pos.x - 18f, pos.y + 17f, 36f, 2f, stats.stats.getStaminaRatio(), STAMINA_BAR_COLOR);
        }
        for (Entity enemy : enemyEntities) {
            EnemyComponent enemyComponent = enemyMapper.get(enemy);
            StatsComponent stats = statsMapper.get(enemy);
            if (enemyComponent == null || stats == null) continue;
            drawBar(enemyComponent.position.x - 18f, enemyComponent.position.y + 20f, 36f, 3f,
                    stats.stats.getHealthRatio(), stats.stats.isAlive() ? HP_BAR_ENEMY : ENEMY_DEFEATED_COLOR);
        }
        shapeRenderer.end();
    }

    private void drawBar(float x, float y, float width, float height, float ratio, Color fillColor) {
        float clamped = MathUtils.clamp(ratio, 0f, 1f);
        shapeRenderer.setColor(HP_BAR_BG);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * clamped, height);
    }

    private void drawDecisionOverlay() {
        DecisionNode node = decisionEngine.getCurrentNode();
        if (node == null) {
            font.draw(batch, "[No hay decisiones activas]", camera.position.x - 620, camera.position.y - 280);
            return;
        }
        StringBuilder panel = new StringBuilder();
        panel.append("DECISIÓN: ").append(node.title).append("\n")
                .append(node.description).append("\n");
        for (int i = 0; i < node.options.size; i++) {
            panel.append(i + 1).append(") ").append(node.options.get(i).label).append("\n");
        }
        panel.append("Pulsa número para elegir, H para cerrar.");
        font.draw(batch, panel, camera.position.x - 620, camera.position.y - 250);
    }

    private void toggleDecisionOverlay() {
        if (!decisionVisible && decisionEngine.getCurrentNode() == null) {
            decisionEngine.resetToEntry();
        }
        decisionVisible = !decisionVisible;
    }

    private void handleDecisionInput() {
        DecisionNode node = decisionEngine.getCurrentNode();
        if (node == null) {
            decisionVisible = false;
            return;
        }
        for (int i = 0; i < node.options.size; i++) {
            int key = Input.Keys.NUM_1 + i;
            int numpad = Input.Keys.NUMPAD_1 + i;
            if (Gdx.input.isKeyJustPressed(key) || Gdx.input.isKeyJustPressed(numpad)) {
                DecisionEngine.Result result = decisionEngine.chooseOption(i);
                showStatus(result.message);
                if (result.success && (decisionEngine.getCurrentNode() == null
                        || decisionEngine.getCurrentNode().options.size == 0)) {
                    decisionVisible = false;
                }
                return;
            }
        }
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

    private Texture createEnemyTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.8f, 0.22f, 0.18f, 1f));
        pixmap.fillCircle(16, 16, 12);
        pixmap.setColor(new Color(0.2f, 0.02f, 0.02f, 1f));
        pixmap.drawCircle(16, 16, 12);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Color getSkyColor() {
        float intensity = MathUtils.sin(dayTimer * MathUtils.PI2) * 0.5f + 0.5f;
        return new Color(0.07f + intensity * 0.25f, 0.09f + intensity * 0.25f, 0.12f + intensity * 0.35f, 1f);
    }

    private void showStatus(String message) {
        this.statusMessage = message;
        this.statusTimer = 2.5f;
    }

    @Override
    public void dispose() {
        for (Texture texture : tileTextures) {
            texture.dispose();
        }
        colonistTexture.dispose();
        enemyTexture.dispose();
        font.dispose();
        shapeRenderer.dispose();
        if (ambientTrack != null) {
            ambientTrack.dispose();
        }
    }
}
