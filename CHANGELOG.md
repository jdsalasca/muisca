# Changelog

All notable changes to this project will be documented here. The format loosely follows [Keep a Changelog](https://keepachangelog.com/) while the versioning scheme will track playable milestones.

# Changelog

All notable changes to this project will be documented here. The format loosely follows [Keep a Changelog](https://keepachangelog.com/) while the versioning scheme will track playable milestones.

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
