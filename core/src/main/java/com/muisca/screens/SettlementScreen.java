package com.muisca.screens;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
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
import java.util.Locale;
import com.muisca.MuiscaGame;
import com.muisca.town.ElderAura;
import com.muisca.town.ElderComponent;
import com.muisca.town.ElderCouncilSystem;
import com.muisca.town.ElderLibrary;
import com.muisca.town.ElderProfile;
import com.muisca.town.TownLifeComponent;
import com.muisca.town.TownLifeSystem;
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
import com.muisca.world.DayCycle;
import com.muisca.world.EnvironmentRegrowthSystem;
import com.muisca.world.FloraField;
import com.muisca.world.TileType;
import com.muisca.world.WorldGenerator;
import com.muisca.world.WorldMap;
import com.muisca.enemies.EnemyArchetype;
import com.muisca.enemies.EnemyFactory;
import com.muisca.telemetry.SystemTelemetry;
import com.muisca.telemetry.InventoryTelemetry;

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
    private final ComponentMapper<ForceComponent> forceComponentMapper = ComponentMapper.getFor(ForceComponent.class);

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
    private DecisionEngine decisionEngine;
    private final SaveManager saveManager = new SaveManager("slot1");
    private final DamageTelemetry damageTelemetry;
    private final SystemTelemetry systemTelemetry;
    private final InventoryTelemetry inventoryTelemetry;
    private final FloraField floraField;
    private final ElderLibrary elderLibrary;
    private final ElderAura elderAura = new ElderAura();
    private final DayCycle dayCycle = new DayCycle();
    private final Vector2 townPlaza;
    private int eldersAssigned = 0;
    private final Color tempColor = new Color();

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
    private boolean showPerfOverlay = false;
    // Overlay de diagnóstico para visualizar algo aunque falle el render de mundo
    private boolean showFallbackOverlay = true;
    private int renderFrames = 0;
    private float renderSecondsAccum = 0f;
    // Nuevos flags de depuración gráfica y seguridad de render
    private boolean showWorldShapeOverlay = false;   // F5: pinta tiles con ShapeRenderer
    private boolean showActorBoxes = false;          // F6: pinta cajas de actores
    private boolean disableBatch = false;            // F7: deshabilita SpriteBatch para aislar problemas
    private boolean batchAlive = true;               // se pone en false si begin/end arroja excepción
    // Simple weather and lighting
    private boolean isRaining = false;
    private float rainIntensity = 0.7f; // 0..1
    private final Array<Vector2> rainDrops = new Array<>();
    private float rainSpawnAccumulator = 0f;
    // References to systems that need environment modifiers
    private CombatResourceSystem combatResourceSystem;
    private EnvironmentRegrowthSystem environmentRegrowthSystem;

    // Estado de arranque y diagnóstico
    private boolean initOk = true;
    private final Array<String> initErrors = new Array<>();
    private InputAdapter debugKeyLogger;

    public SettlementScreen(MuiscaGame game) {
        this.game = game;
        this.batch = game.getSharedBatch();
        this.camera = new OrthographicCamera(1280, 720);
        this.font = new BitmapFont();
        this.shapeRenderer = new ShapeRenderer();
        // Log de entorno gráfico para diagnósticos (GL y backbuffer)
        try {
            String glInfo = "GLVersion=" + Gdx.graphics.getGLVersion().getDebugVersionString()
                    + " | GL30Available=" + Gdx.graphics.isGL30Available()
                    + " | BackBuffer=" + Gdx.graphics.getBackBufferWidth() + "x" + Gdx.graphics.getBackBufferHeight()
                    + " | Continuous=" + Gdx.graphics.isContinuousRendering();
            Gdx.app.log("Graphics", glInfo);
        } catch (Exception ignore) {
            // No interrumpir la carga si el entorno no soporta alguna consulta
        }
        FileHandle telemetryDir = Gdx.files.local("telemetry");
        if (!telemetryDir.exists()) {
            telemetryDir.mkdirs();
        }
        FileHandle damageCsv = telemetryDir.child("damage.csv");
        this.damageTelemetry = new DamageTelemetry(damageCsv);
        FileHandle systemsCsv = telemetryDir.child("systems.csv");
        this.systemTelemetry = new SystemTelemetry(systemsCsv);
        FileHandle inventoryCsv = telemetryDir.child("inventory.csv");
        this.inventoryTelemetry = new InventoryTelemetry(inventoryCsv);

        this.worldMap = new WorldGenerator(WORLD_WIDTH_TILES, WORLD_HEIGHT_TILES, CHUNK_SIZE, 140_921L).generate();
        this.tileTextures = createTileTextures();
        this.colonistTexture = createColonistTexture();
        this.enemyTexture = createEnemyTexture();
        float centerX = WORLD_WIDTH_TILES * TILE_SIZE / 2f;
        float centerY = WORLD_HEIGHT_TILES * TILE_SIZE / 2f;
        this.worldCenterX = centerX;
        this.worldCenterY = centerY;
        this.craftStation = new Vector2(centerX + 72f, centerY);
        this.floraField = new FloraField(WORLD_WIDTH_TILES, WORLD_HEIGHT_TILES);
        // Carga robusta de elders
        ElderLibrary eldersTmp;
        try {
            eldersTmp = ElderLibrary.load(Gdx.files.internal("data/elders.json"));
            Gdx.app.log("Startup", "Elders cargados correctamente");
        } catch (Exception ex) {
            recordInitError("data/elders.json", ex);
            eldersTmp = new ElderLibrary();
        }
        this.elderLibrary = eldersTmp;
        this.townPlaza = new Vector2(centerX + 32f, centerY - 48f);

        // Carga robusta de spells
        SpellLibrary spellsTmp;
        try {
            spellsTmp = SpellLibrary.load(Gdx.files.internal("data/spells"));
            Gdx.app.log("Startup", "Spells cargados correctamente");
        } catch (Exception ex) {
            recordInitError("data/spells", ex);
            spellsTmp = new SpellLibrary();
        }
        this.spellLibrary = spellsTmp;
        this.enemyFactory = new EnemyFactory(spellLibrary);
        this.jobBoard = new JobBoard(worldMap, TILE_SIZE, 18);
        // Carga robusta de recetas
        RecipeBook recipesTmp;
        try {
            recipesTmp = RecipeBook.load(Gdx.files.internal("data/recipes/woodworking.json"));
            Gdx.app.log("Startup", "Recetas cargadas correctamente");
        } catch (Exception ex) {
            recordInitError("data/recipes/woodworking.json", ex);
            recipesTmp = new RecipeBook();
        }
        this.recipeBook = recipesTmp;
        this.craftingQueue = new CraftingQueue(recipeBook, inventory, craftStation);
        // Carga robusta de estructuras
        StructureLibrary structuresTmp;
        try {
            structuresTmp = StructureLibrary.load(Gdx.files.internal("data/structures/basic.json"));
            Gdx.app.log("Startup", "Estructuras cargadas correctamente");
        } catch (Exception ex) {
            recordInitError("data/structures/basic.json", ex);
            structuresTmp = new StructureLibrary();
        }
        this.structureLibrary = structuresTmp;
        this.structureManager = new StructureManager(structureLibrary, inventory);
        this.inventory.bindTelemetry(inventoryTelemetry);
        // Carga robusta de decisiones
        DecisionEngine decisionTmp = null;
        try {
            DecisionGraph decisionGraph = DecisionGraph.load(Gdx.files.internal("data/decisions/bridge_toll.json"));
            decisionTmp = new DecisionEngine(decisionGraph, inventory, reputationTracker, decisionState);
            Gdx.app.log("Startup", "Decisiones cargadas correctamente");
        } catch (Exception ex) {
            recordInitError("data/decisions/bridge_toll.json", ex);
        }
        this.decisionEngine = decisionTmp;

        // Logs de arranque contundentes para validar estado del mundo y texturas
        try {
            Gdx.app.log("Boot", "WorldMap " + worldMap.getWidth() + "x" + worldMap.getHeight()
                    + " | Tiles=" + (tileTextures != null ? tileTextures.length : -1)
                    + " | Colonists (pre)=" + colonistEntities.size
                    + " | Structures=" + structureManager.getInstances().size);
            if (tileTextures != null && tileTextures.length > 0) {
                Gdx.app.log("Boot", "Tile[0] presente=" + (tileTextures[0] != null));
            }
        } catch (Exception e) {
            recordInitError("Boot/initial-log", e);
        }

        this.engine = new Engine();
        engine.addSystem(new InputMovementSystem(worldMap, TILE_SIZE));
        engine.addSystem(new AutonomySystem(worldMap, TILE_SIZE, jobBoard, craftingQueue, inventory));
        engine.addSystem(new TaskSystem(jobBoard, craftingQueue));
        combatResourceSystem = new CombatResourceSystem();
        engine.addSystem(combatResourceSystem);
        engine.addSystem(new PlayerCombatSystem(damageTelemetry));
        engine.addSystem(new EnemyAISystem(damageTelemetry));
        engine.addSystem(new ForceSystem(worldMap, TILE_SIZE));
        engine.addSystem(new StatusSystem(damageTelemetry));
        engine.addSystem(new TownLifeSystem(dayCycle));
        engine.addSystem(new ElderCouncilSystem(elderAura, inventory));
        environmentRegrowthSystem = new EnvironmentRegrowthSystem(floraField, jobBoard, elderAura, TILE_SIZE);
        engine.addSystem(environmentRegrowthSystem);

        createColonist("Ama", centerX, centerY, TalentId.CENIZA_DISCIPLINE, TalentId.JURAMENTO_WARD);
        createColonist("Quyca", centerX + 96, centerY + 32, TalentId.CENIZA_PYRE);
        createColonist("Suaga", centerX - 80, centerY - 64, TalentId.VANGUARD_TRAINING);
        selectColonist(0);

        resetEncounter();

        CombatSnapshot snapshot = saveManager.read(inventory, structureManager, jobBoard, reputationTracker, decisionState);
        craftingQueue.clearJobs();
        applyCombatSnapshot(snapshot);

        // Carga robusta de música ambiente
        try {
            ambientTrack = Gdx.audio.newMusic(Gdx.files.internal("audio/proto_theme.wav"));
            ambientTrack.setLooping(true);
            ambientTrack.setVolume(0.4f);
            ambientTrack.play();
        } catch (Exception ex) {
            recordInitError("audio/proto_theme.wav", ex);
        }

        // Registrar InputProcessor de diagnóstico (logger de teclas)
        debugKeyLogger = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                Gdx.app.log("Input", "keyDown: " + Input.Keys.toString(keycode));
                return false;
            }
            @Override
            public boolean keyUp(int keycode) {
                Gdx.app.log("Input", "keyUp: " + Input.Keys.toString(keycode));
                return false;
            }
        };
        Gdx.input.setInputProcessor(new InputMultiplexer(debugKeyLogger));
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
        TownLifeComponent townLife = new TownLifeComponent();
        townLife.homeAnchor.set(x, y);
        townLife.plazaAnchor.set(townPlaza);
        entity.add(townLife);
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
        if (eldersAssigned < 1 && elderLibrary != null) {
            ElderProfile profile = elderLibrary.getRandom();
            if (profile != null) {
                entity.add(new ElderComponent(profile));
                eldersAssigned++;
                showStatus("Se unió el sabio " + profile.name);
            }
        }
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
            ForceComponent force = forceComponentMapper.get(entity);
            if (force != null) {
                force.velocity.setZero();
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
        ForceComponent force = forceComponentMapper.get(entity);
        if (force != null) {
            force.velocity.setZero();
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

        // Render seguro con SpriteBatch con gating y captura de errores
        batch.setProjectionMatrix(camera.combined);
        boolean willUseBatch = batchAlive && !disableBatch;
        if (willUseBatch) {
            try {
                batch.begin();
                drawWorld();
                drawStructures();
                drawEnemies();
                drawColonists();
                drawHud(delta);
            } catch (Exception ex) {
                batchAlive = false;
                recordInitError("SpriteBatch-render", ex);
                Gdx.app.error("Render", "SpriteBatch falló, activando modo solo shapes", ex);
            } finally {
                try {
                    if (batch.isDrawing()) {
                        batch.end();
                    }
                } catch (Exception endEx) {
                    batchAlive = false;
                    recordInitError("SpriteBatch-end", endEx);
                }
            }
        }

        // Fallback visual para confirmar pipeline de render aunque algo de mundo falle
        if (showFallbackOverlay) {
            drawFallbackOverlay();
        }

        // Overlays adicionales 100% con ShapeRenderer, independientes del SpriteBatch
        if (showWorldShapeOverlay) {
            drawWorldShapeOverlay();
        }
        if (showActorBoxes) {
            drawActorBoxesOverlay();
        }
        drawOverlays();

        // Heartbeat de render cada ~1s
        renderFrames++;
        renderSecondsAccum += delta;
        if (renderSecondsAccum >= 1.0f) {
            try {
                Gdx.app.log("Render", "frames=" + renderFrames
                        + " | cam=" + MathUtils.floor(camera.position.x) + "," + MathUtils.floor(camera.position.y)
                        + " | colonists=" + colonistEntities.size
                        + " | enemies=" + enemyEntities.size
                        + " | chunks=" + worldMap.getChunkSize());
            } catch (Exception e) {
                // No debe romper el render por el log
            }
            renderSecondsAccum = 0f;
        }
    }

    private void updateGame(float delta) {
        handleToggles();
        handleSaveLoadInput();
        handleActions();
        applyInput(delta);
        dayTimer = (dayTimer + delta * 0.04f) % 1f;
        dayCycle.setFraction(dayTimer);
        updateWeather(delta);
        // Apply environment-driven gameplay modifiers (regen and flora regrowth)
        float dayIntensity = MathUtils.sin(dayTimer * MathUtils.PI2) * 0.5f + 0.5f; // 0 (midnight)..1 (noon)
        float rainPenalty = isRaining ? 0.20f * rainIntensity : 0f; // up to -20% regen
        float nightPenalty = 0.15f * (1f - dayIntensity); // up to -15% regen at midnight
        float regenScale = MathUtils.clamp(1f - rainPenalty - nightPenalty, 0.6f, 1.0f);
        if (combatResourceSystem != null) {
            combatResourceSystem.setRegenScale(regenScale);
        }
        float weatherRegrowMultiplier = isRaining ? (1f + 0.6f * rainIntensity) : 1f; // up to +60% in heavy rain
        if (environmentRegrowthSystem != null) {
            environmentRegrowthSystem.setWeatherRegrowMultiplier(weatherRegrowMultiplier);
        }
        if (systemTelemetry != null) {
            systemTelemetry.logRegenScale(dayIntensity, isRaining, rainIntensity, regenScale, weatherRegrowMultiplier);
        }
        engine.update(delta);
        damageTelemetry.update(delta);
        applyElderSpirit(delta);
        updateCamera();
        statusTimer = Math.max(0f, statusTimer - delta);
    }

    private void handleToggles() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            showChunks = !showChunks;
            Gdx.app.log("Input", "C -> showChunks=" + showChunks);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebug = !showDebug;
            Gdx.app.log("Input", "F1 -> showDebug=" + showDebug);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            showPerfOverlay = !showPerfOverlay;
            Gdx.app.log("Input", "F2 -> showPerfOverlay=" + showPerfOverlay);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            showWorldShapeOverlay = !showWorldShapeOverlay;
            Gdx.app.log("Input", "F5 -> showWorldShapeOverlay=" + showWorldShapeOverlay);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            showActorBoxes = !showActorBoxes;
            Gdx.app.log("Input", "F6 -> showActorBoxes=" + showActorBoxes);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
            disableBatch = !disableBatch;
            Gdx.app.log("Input", "F7 -> disableBatch=" + disableBatch + ", batchAlive=" + batchAlive);
            showStatus(disableBatch ? "Modo solo shapes" : "SpriteBatch reactivado");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            showFallbackOverlay = !showFallbackOverlay;
            Gdx.app.log("Input", "F4 -> showFallbackOverlay=" + showFallbackOverlay);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            isRaining = !isRaining;
            showStatus(isRaining ? "Clima: lluvia ligera" : "Clima: despejado");
            if (systemTelemetry != null) {
                systemTelemetry.logWeatherToggle(isRaining, rainIntensity);
            }
            Gdx.app.log("Input", "F3 -> isRaining=" + isRaining + ", rainIntensity=" + rainIntensity);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            selectColonist((controlledColonistIndex + 1) % colonistEntities.size);
            Gdx.app.log("Input", "TAB -> controlledColonistIndex=" + controlledColonistIndex);
        }
    }

    // Dibuja los tiles visibles con ShapeRenderer, usando el mismo cálculo de color del mundo
    private void drawWorldShapeOverlay() {
        try {
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            float camLeft = camera.position.x - halfW;
            float camRight = camera.position.x + halfW;
            float camBottom = camera.position.y - halfH;
            float camTop = camera.position.y + halfH;
            int minX = Math.max(0, (int) (camLeft / TILE_SIZE));
            int maxX = Math.min(worldMap.getWidth() - 1, (int) (camRight / TILE_SIZE) + 1);
            int minY = Math.max(0, (int) (camBottom / TILE_SIZE));
            int maxY = Math.min(worldMap.getHeight() - 1, (int) (camTop / TILE_SIZE) + 1);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    float lush = floraField.sampleGrass(x, y);
                    float bloom = floraField.sampleSprouts(x, y);
                    tempColor.set(0.85f + lush * 0.15f,
                            0.85f + bloom * 0.1f,
                            0.9f + lush * 0.05f,
                            0.25f);
                    shapeRenderer.setColor(tempColor);
                    shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            shapeRenderer.end();
        } catch (Exception ex) {
            Gdx.app.error("Overlay", "drawWorldShapeOverlay error", ex);
        }
    }

    // Pinta cajas y cruces sobre colonos y enemigos para validar posiciones sin texturas
    private void drawActorBoxesOverlay() {
        try {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            // Colonists
            shapeRenderer.setColor(0.3f, 0.95f, 0.35f, 0.9f);
            for (int i = 0; i < colonistEntities.size; i++) {
                Colonist c = colonistMapper.get(colonistEntities.get(i)).colonist;
                Vector2 p = c.getPosition();
                if (!isOnScreen(p.x, p.y, 40f)) continue;
                shapeRenderer.rect(p.x - 16f, p.y - 16f, 32f, 32f);
                shapeRenderer.line(p.x - 16f, p.y, p.x + 16f, p.y);
                shapeRenderer.line(p.x, p.y - 16f, p.x, p.y + 16f);
            }
            // Enemies
            shapeRenderer.setColor(0.95f, 0.35f, 0.35f, 0.9f);
            for (Entity e : enemyEntities) {
                EnemyComponent ec = enemyMapper.get(e);
                if (ec == null) continue;
                Vector2 p = ec.position;
                if (!isOnScreen(p.x, p.y, 40f)) continue;
                shapeRenderer.rect(p.x - 16f, p.y - 16f, 32f, 32f);
                shapeRenderer.line(p.x - 16f, p.y, p.x + 16f, p.y);
                shapeRenderer.line(p.x, p.y - 16f, p.x, p.y + 16f);
            }
            shapeRenderer.end();
        } catch (Exception ex) {
            Gdx.app.error("Overlay", "drawActorBoxesOverlay error", ex);
        }
    }

    private void handleActions() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            toggleDecisionOverlay();
            Gdx.app.log("Input", "H -> decisionVisible=" + decisionVisible);
        }
        if (decisionVisible) {
            handleDecisionInput();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            queueRecipe("plank_bundle");
            Gdx.app.log("Input", "1 -> queue plank_bundle");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            queueRecipe("camp_bed");
            Gdx.app.log("Input", "2 -> queue camp_bed");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            placeStructure("camp_bed");
            Gdx.app.log("Input", "B -> place camp_bed");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            placeStructure("storage_crate");
            Gdx.app.log("Input", "N -> place storage_crate");
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
            Gdx.app.log("Input", "F5 -> Guardar partida");
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
            Gdx.app.log("Input", "F9 -> Cargar partida");
        }
    }

    // Registro de error de arranque con fallback de estado
    private void recordInitError(String context, Exception ex) {
        initOk = false;
        String msg = "Fallo al cargar " + context + ": " + ex.getMessage();
        initErrors.add(msg);
        Gdx.app.error("Startup", msg, ex);
        showStatus("Error de inicio: " + context);
    }

    private void applyInput(float delta) {
        inputDirection.setZero();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) inputDirection.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) inputDirection.y -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) inputDirection.x -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) inputDirection.x += 1f;
        boolean fast = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        float speed = fast ? 320f : 200f;
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

    private void applyElderSpirit(float delta) {
        float boost = elderAura.getSpiritBoost();
        if (boost <= 0.001f) {
            return;
        }
        for (Entity entity : colonistEntities) {
            StatsComponent stats = statsMapper.get(entity);
            if (stats == null || !stats.stats.isAlive()) {
                continue;
            }
            Colonist colonist = colonistMapper.get(entity).colonist;
            colonist.adjustSpirit(boost * delta * 0.2f);
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
        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;
        float camLeft = camera.position.x - halfW;
        float camRight = camera.position.x + halfW;
        float camBottom = camera.position.y - halfH;
        float camTop = camera.position.y + halfH;
        int minX = Math.max(0, (int) (camLeft / TILE_SIZE));
        int maxX = Math.min(worldMap.getWidth() - 1, (int) (camRight / TILE_SIZE) + 1);
        int minY = Math.max(0, (int) (camBottom / TILE_SIZE));
        int maxY = Math.min(worldMap.getHeight() - 1, (int) (camTop / TILE_SIZE) + 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Texture texture = tileTextures[worldMap.getTile(x, y).ordinal()];
                float lush = floraField.sampleGrass(x, y);
                float bloom = floraField.sampleSprouts(x, y);
                tempColor.set(0.85f + lush * 0.15f,
                        0.85f + bloom * 0.1f,
                        0.9f + lush * 0.05f,
                        1f);
                batch.setColor(tempColor);
                batch.draw(texture, x * TILE_SIZE, y * TILE_SIZE);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawStructures() {
        for (StructureInstance instance : structureManager.getInstances()) {
            Rectangle rect = instance.getBounds();
            float centerX = rect.x + rect.width / 2f;
            float centerY = rect.y + rect.height / 2f;
            if (!isOnScreen(centerX, centerY, 64f)) {
                continue;
            }
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
            if (!isOnScreen(enemy.position.x, enemy.position.y, 48f)) {
                continue;
            }
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
        builder.append("Muisca v0.0.6 | Tab colonos | WASD mover | Shift correr | Q/E hechizos | 1=Tablones | 2=Cama | B/N planos | C grilla | F1 debug | F2 perf | F3 clima | F4 fallback | F5 tiles shapes | F6 cajas actores | F7 solo shapes\n");
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
        builder.setLength(0);
        builder.append("Consejo: Flora x")
                .append(String.format(Locale.US, "%.2f", elderAura.getFloraBoost()))
                .append(" | Espíritu +")
                .append(String.format(Locale.US, "%.2f", elderAura.getSpiritBoost()));
        font.draw(batch, builder, camera.position.x - 620, camera.position.y + 190);

        if (showDebug) {
            font.draw(batch,
                    "Cam " + MathUtils.floor(camera.position.x) + "," + MathUtils.floor(camera.position.y)
                            + " | Chunk " + worldMap.getChunkSize()
                            + " | Day " + MathUtils.round(dayTimer * 100),
                    camera.position.x - 620,
                    camera.position.y - 330);
        }
        if (showPerfOverlay) {
            builder.setLength(0);
            int fps = Gdx.graphics.getFramesPerSecond();
            float javaHeap = Gdx.app.getJavaHeap() / (1024f * 1024f);
            float nativeHeap = Gdx.app.getNativeHeap() / (1024f * 1024f);
            builder.append("FPS ").append(fps)
                    .append(" | Java ").append(String.format(Locale.US, "%.1f MB", javaHeap))
                    .append(" | Native ").append(String.format(Locale.US, "%.1f MB", nativeHeap));
            font.draw(batch, builder, camera.position.x + 240, camera.position.y + 340);
        }

        // Safe Boot HUD: mostrar errores de arranque si los hubo (lado derecho)
        if (!initOk) {
            builder.setLength(0);
            builder.append("[SAFE BOOT] Algunos assets no cargaron.");
            font.draw(batch, builder, camera.position.x + 240, camera.position.y + 320);
            int maxList = Math.min(3, initErrors.size);
            for (int i = 0; i < maxList; i++) {
                font.draw(batch, "- " + initErrors.get(i), camera.position.x + 240, camera.position.y + 300 - 20f * i);
            }
            font.draw(batch, "Atajos: F1 debug, F2 perf, F3 clima, WASD, 1/2 craft, B/N construir", camera.position.x + 240, camera.position.y + 240);
        }
    }

    private void drawOverlays() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        if (showChunks) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
            float chunkWorld = worldMap.getChunkSize() * TILE_SIZE;
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            float camLeft = Math.max(0f, camera.position.x - halfW);
            float camRight = Math.min(worldMap.getWidth() * TILE_SIZE, camera.position.x + halfW);
            float camBottom = Math.max(0f, camera.position.y - halfH);
            float camTop = Math.min(worldMap.getHeight() * TILE_SIZE, camera.position.y + halfH);
            int minChunkX = Math.max(0, (int) (camLeft / chunkWorld));
            int maxChunkX = Math.min((int) Math.ceil(camRight / chunkWorld) + 1, worldMap.getWidth() / worldMap.getChunkSize());
            int minChunkY = Math.max(0, (int) (camBottom / chunkWorld));
            int maxChunkY = Math.min((int) Math.ceil(camTop / chunkWorld) + 1, worldMap.getHeight() / worldMap.getChunkSize());
            for (int x = minChunkX; x <= maxChunkX; x++) {
                float worldX = x * chunkWorld;
                shapeRenderer.line(worldX, camBottom, worldX, camTop);
            }
            for (int y = minChunkY; y <= maxChunkY; y++) {
                float worldY = y * chunkWorld;
                shapeRenderer.line(camLeft, worldY, camRight, worldY);
            }
            shapeRenderer.end();
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (StructureInstance instance : structureManager.getInstances()) {
            Rectangle rect = instance.getBounds();
            float centerX = rect.x + rect.width / 2f;
            float centerY = rect.y + rect.height / 2f;
            if (!isOnScreen(centerX, centerY, 64f)) {
                continue;
            }
            shapeRenderer.setColor(instance.getBlueprint().getColor());
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.setColor(1f, 0.85f, 0.3f, 0.9f);
        if (isOnScreen(craftStation.x, craftStation.y, 48f)) {
            shapeRenderer.circle(craftStation.x, craftStation.y, 6f, 12);
        }
        for (HarvestSite site : jobBoard.getSites()) {
            if (!isOnScreen(site.position.x, site.position.y, 48f)) {
                continue;
            }
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
            if (!isOnScreen(pos.x, pos.y, 40f)) {
                continue;
            }
            drawBar(pos.x - 18f, pos.y + 22f, 36f, 3f, stats.stats.getHealthRatio(), HP_BAR_PLAYER);
            drawBar(pos.x - 18f, pos.y + 17f, 36f, 2f, stats.stats.getStaminaRatio(), STAMINA_BAR_COLOR);
        }
        for (Entity enemy : enemyEntities) {
            EnemyComponent enemyComponent = enemyMapper.get(enemy);
            StatsComponent stats = statsMapper.get(enemy);
            if (enemyComponent == null || stats == null) continue;
            if (!isOnScreen(enemyComponent.position.x, enemyComponent.position.y, 40f)) {
                continue;
            }
            drawBar(enemyComponent.position.x - 18f, enemyComponent.position.y + 20f, 36f, 3f,
                    stats.stats.getHealthRatio(), stats.stats.isAlive() ? HP_BAR_ENEMY : ENEMY_DEFEATED_COLOR);
        }
        shapeRenderer.end();

        // Day/Night tint overlay (draw last to darken scene)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float intensity = MathUtils.sin(dayTimer * MathUtils.PI2) * 0.5f + 0.5f; // 0 (midnight) .. 1 (noon)
        float darkness = MathUtils.clamp(0.65f * (1f - intensity) + (isRaining ? 0.1f : 0f), 0f, 0.75f);
        if (darkness > 0.01f) {
            shapeRenderer.setColor(0f, 0f, 0f, darkness);
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            float camLeft = camera.position.x - halfW;
            float camBottom = camera.position.y - halfH;
            shapeRenderer.rect(camLeft, camBottom, camera.viewportWidth, camera.viewportHeight);
        }

        // Rain overlay
        if (isRaining && rainDrops.size > 0) {
            shapeRenderer.setColor(0.6f, 0.7f, 0.9f, 0.45f);
            for (Vector2 drop : rainDrops) {
                shapeRenderer.rectLine(drop.x, drop.y, drop.x + 0f, drop.y - 10f, 1.5f);
            }
        }
        shapeRenderer.end();
    }

    private void updateWeather(float delta) {
        if (!isRaining) {
            rainDrops.clear();
            rainSpawnAccumulator = 0f;
            return;
        }
        // Move existing drops
        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;
        float camLeft = camera.position.x - halfW;
        float camRight = camera.position.x + halfW;
        float camBottom = camera.position.y - halfH;
        float camTop = camera.position.y + halfH;
        for (int i = rainDrops.size - 1; i >= 0; i--) {
            Vector2 d = rainDrops.get(i);
            d.y -= (220f + MathUtils.random(-40f, 40f)) * delta;
            d.x += MathUtils.random(-10f, 10f) * delta; // slight wind jitter
            if (d.y < camBottom - 20f || d.x < camLeft - 20f || d.x > camRight + 20f) {
                rainDrops.removeIndex(i);
            }
        }
        // Spawn new drops based on intensity and viewport area
        rainSpawnAccumulator += delta * rainIntensity;
        int spawnCount = (int) (rainSpawnAccumulator * 120f);
        if (spawnCount > 0) {
            rainSpawnAccumulator -= spawnCount / 120f;
            for (int i = 0; i < spawnCount; i++) {
                float x = MathUtils.random(camLeft, camRight);
                float y = MathUtils.random(camTop - 10f, camTop + 30f);
                rainDrops.add(new Vector2(x, y));
            }
        }
    }

    // Dibujo de emergencia para diagnosticar pantalla negra: un rectángulo y una cruz en el centro
    private void drawFallbackOverlay() {
        try {
            float cx = worldCenterX;
            float cy = worldCenterY;
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.1f, 0.8f, 0.2f, 0.25f);
            shapeRenderer.rect(cx - 40, cy - 40, 80, 80);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.2f, 0.95f, 0.3f, 0.8f);
            shapeRenderer.line(cx - 60, cy, cx + 60, cy);
            shapeRenderer.line(cx, cy - 60, cx, cy + 60);
            shapeRenderer.end();
        } catch (Exception e) {
            // No interferir con el render normal; solo es diagnóstico
            Gdx.app.log("Fallback", "Error dibujando overlay: " + e.getMessage());
        }
    }

    private boolean isOnScreen(float worldX, float worldY, float margin) {
        float halfW = camera.viewportWidth / 2f + margin;
        float halfH = camera.viewportHeight / 2f + margin;
        return Math.abs(worldX - camera.position.x) <= halfW
                && Math.abs(worldY - camera.position.y) <= halfH;
    }

    private void drawBar(float x, float y, float width, float height, float ratio, Color fillColor) {
        float clamped = MathUtils.clamp(ratio, 0f, 1f);
        shapeRenderer.setColor(HP_BAR_BG);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * clamped, height);
    }

    private void drawDecisionOverlay() {
        if (decisionEngine == null) {
            font.draw(batch, "[Decisiones deshabilitadas]", camera.position.x - 620, camera.position.y - 280);
            return;
        }
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
        if (decisionEngine == null) {
            decisionVisible = false;
            showStatus("Decisiones no disponibles.");
            return;
        }
        if (!decisionVisible && decisionEngine.getCurrentNode() == null) {
            decisionEngine.resetToEntry();
        }
        decisionVisible = !decisionVisible;
    }

    private void handleDecisionInput() {
        if (decisionEngine == null) {
            decisionVisible = false;
            return;
        }
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
        pixmap.setColor(new Color(0.12f, 0.12f, 0.12f, 1f));
        pixmap.fillCircle(16, 16, 15);
        pixmap.setColor(new Color(0.9f, 0.78f, 0.55f, 1f));
        pixmap.fillCircle(16, 18, 11);
        pixmap.setColor(new Color(0.17f, 0.3f, 0.55f, 1f));
        pixmap.fillRectangle(11, 6, 10, 10);
        pixmap.setColor(new Color(0.2f, 0.2f, 0.25f, 1f));
        pixmap.fillRectangle(12, 4, 8, 4);
        pixmap.setColor(new Color(0.65f, 0.33f, 0.18f, 1f));
        pixmap.fillRectangle(14, 22, 4, 8);
        pixmap.fillRectangle(7, 22, 4, 8);
        pixmap.fillRectangle(21, 22, 4, 8);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createEnemyTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.05f, 0.05f, 1f));
        pixmap.fillCircle(16, 16, 15);
        pixmap.setColor(new Color(0.86f, 0.32f, 0.21f, 1f));
        pixmap.fillCircle(16, 16, 12);
        pixmap.setColor(new Color(0.3f, 0.05f, 0.05f, 1f));
        pixmap.drawCircle(16, 16, 12);
        pixmap.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        pixmap.fillCircle(12, 18, 2);
        pixmap.fillCircle(20, 18, 2);
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
