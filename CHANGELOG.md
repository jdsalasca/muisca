# Changelog

All notable changes to this project will be documented here. The format loosely follows [Keep a Changelog](https://keepachangelog.com/) while the versioning scheme will track playable milestones.

## [0.0.7] - 2025-11-16

### Added
- Agricultura data-driven: `assets/data/crops.json`, `CropLibrary` y `FarmPlotManager` generan parcelas alrededor del poblado; `AgricultureSystem` controla temporadas (templada/lluviosa/seca) y crecimiento.
- Overlay agrícola (`F8`) y HUD actualizado con progreso promedio, parcelas listas y nueva cadena de atajos; colonos priorizan sembrar/cosechar antes de ir al JobBoard.
- `MarketPriceTracker` aplica ajustes por reputación/escasez y expone un resumen en HUD para materias primas (`raw_wood`, `plank`, `maize`, `quinoa`, `coca_leaf`).
- Guardado/carga ahora persiste las parcelas (`SaveData.SaveFarmPlot`) y restaura cultivos en curso.
- Telemetría agrícola: `SystemTelemetry` registra `farm_status` (temporada, parcelas barbecho/creciendo/listas y progreso promedio) cada pocos segundos para balancear economía.

### Changed
- `TaskSystem` y `AutonomySystem` gestionan nuevos tipos de tareas (`FARM_PLANT`, `FARM_HARVEST`) y depositan las cosechas directamente en el inventario compartido.
- `WorldGenerator` incorpora el bioma Valle Nublado con máscara procedural orientada al noreste para desbloquear el segundo piso jugable del roadmap 0.0.7.
- README/roadmap/backlog reflejan el sprint económico (tabla de versiones, atajos, sección "Economía y agricultura").
- Safe boot visual: `SettlementScreen` activa automáticamente el modo shapes si `SpriteBatch` falla y muestra instrucciones; `tools/run-desktop.ps1` incorpora `-ForceShapes` y `-UseAngle` para facilitar los fallbacks (ANGLE/ShapeRenderer).

## [0.0.6.1] - 2025-11-10

### Added
- Overlay de día/noche: oscurecimiento suave basado en el `dayTimer` e intensidad del ciclo.
- Sistema de clima (lluvia): partículas de lluvia dentro del área visible de la cámara; toggle rápido con `F3`.
- HUD actualizado con la instrucción “F3 clima” para descubrir el nuevo toggle.

### Changed
- Movimiento con colisiones por eje: los colonos ahora intentan desplazarse separando los ejes X/Y y evitan entrar a tiles no transitables.
- Passability en tiles: `TileType` incorpora `isPassable()` y `isWater()`; el bioma `LAGUNA_SAGRADA` se marca como no transitable (agua).
- Generación de mundo mejorada: FBM más suave y sesgo radial de elevación que favorece tierra cerca del centro para mejorar el área de spawn.
- Sprint detecta ambas teclas Shift (izquierda y derecha) para mayor compatibilidad de teclado.
 - Soporte Java 25: `desktop:run` añade `--enable-native-access=ALL-UNNAMED` y el helper `run-desktop.ps1` permite seleccionar el runtime con `-RuntimeJdk 21|25` (por defecto 25).

### Fixed
- Se evita caminar sobre agua al validar transitabilidad del tile antes de aplicar movimiento.
- Se corrige el problema intermitente donde la tecla Shift derecha no activaba el sprint en algunos teclados.

## [0.0.6.2] - 2025-11-10

### Added
- Integración gameplay de clima/día-noche:
  - Penalización de regeneración de recursos (stamina/focus) en lluvia y de noche mediante un factor global en `CombatResourceSystem`.
  - Aceleración de regrowth de flora cuando llueve a través de `EnvironmentRegrowthSystem` (multiplicador aplicado a sitios de tala y regeneración aleatoria).
 - Telemetría y formato de logs:
   - Nuevo `SystemTelemetry` (CSV `systems.csv`) para registrar cambios de clima (toggle F3) y actualizaciones del factor de regeneración ambiental por frame.
   - `DamageTelemetry` ahora escribe CSV con encabezado (`timestamp,dir,source,target,amount,type,ability`) y por defecto a `telemetry/damage.csv`.
   - Nuevo `InventoryTelemetry` (CSV `inventory.csv`) integrado en `Inventory`: registra altas `add` y consumos `consume` de ítems con contexto.
   - Mensajes de consola normalizados para diagnóstico rápido: `[System] regen_env_update ...` y `[Damage] ...`.

### Changed
- `SettlementScreen` calcula la intensidad del día y la penalización por lluvia cada frame y aplica:
  - `setRegenScale(...)` al `CombatResourceSystem` para ajustar regen.
  - `setWeatherRegrowMultiplier(...)` al `EnvironmentRegrowthSystem` para aumentar la regeneración ambiental.

### Notes
- Verificado en JDK 25: build y run correctos; se mantienen advertencias de LWJGL relacionadas con `sun.misc.Unsafe` (pendiente actualización de backend). Se crean `telemetry/damage.csv` y `telemetry/systems.csv` durante la sesión de juego.
- Se crea también `telemetry/inventory.csv` al añadir/consumir ítems del inventario durante gameplay.

## [0.0.6.3] - 2025-11-10

### Added
- Overlays de diagnóstico gráfico 100% con ShapeRenderer:
  - F5: "tiles shapes" pinta los tiles visibles con rectángulos translúcidos según flora (valida pipeline sin texturas).
  - F6: "cajas actores" dibuja cajas y cruces sobre colonos y enemigos para verificar posiciones y culling.
  - F4 ya existente: fallback overlay con rectángulo y cruz en el centro del mundo.
- HUD actualizado con atajos F4/F5/F6/F7 para facilitar pruebas.
- Logs de entorno gráfico en boot: versión GL, disponibilidad GL30, tamaño de backbuffer y modo de render continuo.

### Changed
- Render seguro: se agregó gating con `try/catch` alrededor de `SpriteBatch.begin()/end()`; al fallar, se desactiva el batch y se registra el error, quedando operativo el modo "solo shapes".
- DesktopLauncher: backbuffer estable (RGBA 8/8/8/8, depth 16, stencil 0) y emulación GL configurable.
  - Por defecto usa `GLEmulation.GL30` (OpenGL 3.2), más estricto y estable.
  - Opción ANGLE (`-Dmuisca.angle=true`) para entornos Windows/Metal con drivers problemáticos.

### Notes
- Estas medidas endurecen el render frente a pantallas negras y facilitan el diagnóstico inmediato con F5/F6 incluso si el batch o las texturas fallan.

## [0.0.6] - 2025-11-12

### Added
- Componentes de combate (stats, talentos, identidad, hechizos) y sistemas (`CombatResourceSystem`, `PlayerCombatSystem`, `EnemyAISystem`, `StatusSystem`).
- Librerías de hechizos Ceniza/Juramento (`assets/data/spells/*.json`), telemetría de daño y HUD ampliado con barras de vida/recursos.
- Encuentro prototipo con 3 arquetipos enemigos y mini-jefe, más fábricas y texturas dedicadas.
- Pruebas `DamageCalculatorTest` para validar mitigación/resistencias sobre estados Bond/Burn.
- Persistencia de combate: colonos y enemigos serializan HP/VIT, cooldowns y encuentros via `SaveManager` + snapshots.
- Estados (quemado/vínculo) también se serializan/restauran para que los efectos persistentes continúen tras cargar partida.
- Nuevas físicas ligeras (ForceComponent/ForceSystem) con `knockback`, `SLOW` y `STAGGER`; hechizos y enemigos actualizados para aprovecharlas.
- Suite de pruebas `StatusComponentTest` para validar multiplicadores de movimiento/stagger.
- Vida de aldea: biblioteca de sabios (`assets/data/elders.json`), componentes `Elder/TownLife`, sistema de consejo y rutinas sociales.
- Ecosistema regenerativo (`FloraField`, `EnvironmentRegrowthSystem`) enlazado al JobBoard para que la madera y el pasto vuelvan a crecer con el tiempo.

### Changed
- `SettlementScreen` ahora carga libro de hechizos, asigna talentos a colonos, habilita lanzamientos con `Q/E` y muestra overlay con DPS.
- `SaveManager` soporta snapshots de combate y `SettlementScreen` aplica/restaura estas métricas durante `F5/F9`.
- `DamageTelemetry` escribe CSV incremental (`telemetry/damage.log`) además del overlay en pantalla.
- README/roadmap/backlog/AGENTS reflejan sprint 0.0.6 y se documentan nuevos atajos/validaciones.
- Gradle core integra JUnit 5 para las nuevas pruebas automatizadas.
- Render y overlays ahora hacen culling por cámara; el HUD incluye toggles `F2` y los colonos muestran el aura del consejo.

## [0.0.5] - 2025-11-11

### Added
- Sistema de decisiones/reputación (`DecisionGraph`, `DecisionEngine`, JSON `bridge_toll`) con UI en juego (`H` para abrir, números para elegir).
- Seguimiento de reputaciones por facción y flags persistentes; efectos sobre recompensas e inventario.
- Guardado/carga (`SaveManager`, `F5/F9`) que serializa inventario, estructuras, sitios de tala, reputaciones y flags.
 
### Changed
- HUD muestra reputación y se expandió el overlay cuando hay decisiones activas.
- `SettlementScreen` integra el nuevo flujo (toggle decisiones, guardar/cargar) y limpia tareas al restaurar.
- Toolchains siguen apuntando a `.toolchains/linux-openjdk*`; instrucciones actualizadas en README.

## [0.0.4] - 2025-11-10

### Added
- Inventario compartido (`Inventory`) y cola de crafting data-driven (`assets/data/recipes/woodworking.json`) con recetas madera→tablón→cama.
- Sistema de planos (`StructureLibrary/Manager`) con colocación en tiempo real (cama de campaña y caja de almacenaje).
- HUD ampliado que muestra inventario, pedidos pendientes y mensajes contextuales; nuevas teclas: `1/2` recetas, `B/N` planos.
- Nuevos datos JSON (`recipes`, `structures`) y managers de construcción.

### Changed
- `SettlementScreen` ahora orquesta ECS + inventario + crafting + estructuras; colonos puede abortar tareas manualmente.
- `AutonomySystem` y `TaskSystem` priorizan trabajos de crafting antes que tala y depositan recursos en el inventario.
- Toolchains Linux apuntan a `.toolchains/linux-openjdk*`; README actualizado a 0.0.4.

## [0.0.3] - 2025-11-10

### Added
- Integración de Ashley ECS: colonos como entidades con componentes de input, autonomía y tareas.
- Sistema de cola de trabajos (JobBoard) con nodos de recolección compartidos, overlay visual y HUD extendido.
- Nuevos sistemas (`InputMovementSystem`, `AutonomySystem`, `TaskSystem`) y componentes para coordinar IA y control manual.
- Instrumentación del launcher para registrar la versión de Java utilizada al levantar el juego.

### Changed
- `SettlementScreen` ahora delega todo en la ECS (render, HUD y movimiento).
- Versionado del proyecto a 0.0.3; README/roadmap/backlog actualizados para reflejar el nuevo alcance.

## [0.0.2] - 2025-11-09

### Added
- Generador procedural (96×96 tiles) con 6 biomas inspirados en el universo muisca y grilla de chunks conmutables.
- Tres colonos con estados básicos (hambre, espíritu, fatiga), control manual con WASD + sprint y cambio de colono con Tab.
- Simulación de colonos autónomos, HUD expandido y modo debug (F1) para lectura rápida del mundo.
- Música ambiental (`assets/audio/proto_theme.wav`) y documentación extendida (roadmap 0.0.1–0.0.11, principios de optimización).

### Changed
- Título de la build desktop para reflejar v0.0.2.
- Versionado Gradle del proyecto principal.

## [0.0.1] - 2025-11-09

### Added
- Estructura Gradle multi-módulo (`core`, `desktop`, `assets`) apuntando a Java 25.
- Wrapper de Gradle 8.10.x y toolchain local `.jdk/` empaquetada en el repo.
- Vertical slice base: `SettlementScreen` con tiles placeholder, ciclo día/noche, HUD y movimiento WASD con aceleración.
- Documentación inicial (`README`, `docs/roadmap.md`, `docs/backlog.md`, `docs/gdd.md`) para facilitar el traspaso entre agentes.

### Missing / Next
- Implementar ECS real con Ashley/Artemis y sistemas de colonos.
- Persistencia de mundo, biomas múltiples y data JSON para decisiones/crafting.
- Assets de arte y audio definitivos, así como pruebas automatizadas.
