# Changelog

All notable changes to this project will be documented here. The format loosely follows [Keep a Changelog](https://keepachangelog.com/) while the versioning scheme will track playable milestones.

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
