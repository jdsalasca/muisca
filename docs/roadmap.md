# Roadmap Operativo (v0.0.1 – v0.0.11)

El objetivo es entregar builds cortas y tocables cada iteración, siempre con foco en escalabilidad (ECS + data-driven), rendimiento (chunk streaming, pooling) y narrativa persistente. Cada release incluye KPI básicos: 60 FPS en 1080p, menos de 2 GB de RAM, escena cargable en ≤6 s.

## Línea de entregas

| Versión | Pilares | Entregable clave | Métricas |
| --- | --- | --- | --- |
| **0.0.1** | Motor | Proyecto Gradle/libGDX, render básico, HUD placeholder. | Build desktop corre en Java 25. |
| **0.0.2** | Mundo + audio | Generador 96×96 tiles con 6 biomas, colonos múltiples (estados), música ambiental prototipo. | ≥3 colonos activos, chunk overlay sin caídas. |
| **0.0.3** | ECS y recursos | Integración Ashley, colonos como entidades, harvesting básico y job queue compartida. | ✅ Engine ECS estable, reservas de recursos visibles. |
| **0.0.4** | Construcción/crafting | Blueprint de estructuras livianas, recetas JSON (madera→tablón→cama), inventario compartido, UI de crafting. | ✅ Inventario global + planos colocables funcionando in-game. |
| **0.0.5** | Decisiones/reputación | Flags persistentes, cadena “puente o peaje”, facción local que modifica comercio; validador de nodos JSON. | ✅ Reputación y decisiones guardadas/cargadas; UI in-game. |
| **0.0.6** | Combate/magia | Stats, talentos iniciales, 2 escuelas (Ceniza/Juramento), 3 enemigos IA, mini-jefe y ecosistema con consejo de ancianos. | ✅ Encuentro jefe ≤3 min, telemetría + guardados persistentes. |
| 0.0.6.1 (parche) | Visual/movimiento | Overlay día/noche, clima lluvia `F3`, passability de agua y worldgen con spawn central. | ✅ HUD actualizado y slice estable en Java 25. |
| **0.0.7** | Economía/agricultura | Cultivos (3 plantas), estaciones ligeras, comercio con reputación + escasez, segundo bioma jugable. | Colonia se sostiene 10 min solo con cultivos. |
| **0.0.8** | Eventos + audio reactivo | Eventos (migrante, ataque menor, tormenta), capas de música/SFX por bioma/clima, progreso de rasgos. | Eventos se disparan cada 6–10 min, sistema de música sin cortes audibles. |
| **0.0.9** | Facciones/guerras | 3 facciones con tensiones, mapa estratégico, asaltos/tributos/treguas, diplomacia UI. | Simulación mínima 20 min sin GC spikes >10 ms. |
| **0.0.10** | UX/accesibilidad | Overlays avanzados (pathing, calor de peligro), remapeo completo, soporte daltonismo, perfiles de entrada. | Encuesta tutorial ≥85 % comprensión, QA accesibilidad checklist verde. |
| **0.0.11** | Optimización + contenidos | Chunk streaming multi-bioma, pooling IA, profiler scripts, paquetes de assets finales (música, FX, sprites), soporte modding data-driven inicial. | Build Analyzer muestra CPU <10 ms en hardware target, pipeline de datos validado en CI. |

> Después de 0.0.11 se consolida **Alpha 0.1** (4 biomas, 30+ recetas, 10 cadenas narrativas) y se abre Beta 0.2 con focus QA/balance.

## Principios obligatorios (respetar en cada sprint)

- **ECS primero**: no hay lógica de gameplay en render. Componentes y sistemas deben residir en `core/src/main/java/com/muisca/ecs/**` (a crear en 0.0.3) y solo comunicarse mediante eventos/data.
- **Data-driven**: Biomas, recetas, hechizos, decisiones y facciones viven en `assets/data/*.json|yaml`. Ningún valor mágico en código salvo fallback.
- **Optimización continua**: cada feature incluye medición (miniprofiler, logs). Prohibido introducir allocaciones por frame en loops críticos; preferir pooling/libGDX `Array`.
- **Audio escalable**: usar `Music` + `Sound` en capas. Cada bioma tendrá stems propios (planeado para 0.0.8), así que mantener rutas y mixers en `assets/audio/banks.json` (pendiente).
- **Testing/validadores**: a partir de 0.0.5 existen pruebas JUnit para flags/crafting/economía y scripts en `tools/` para validar JSON y empaquetar atlas.

## Cadencia y responsabilidades

- **Sprints de 2 semanas** con entregables ejecutables. Cada sprint actualiza `docs/roadmap.md`, `docs/backlog.md`, `CHANGELOG.md`, y `README.md`.
- **Playtests internos**: 0.0.5, 0.0.7 y 0.0.9 deben generar encuestas y telemetría (ver `docs/metrics.md` futuro).
- **Herramientas**: mantener `tools/` con scripts reproducibles (fetch JDK, validadores, generadores de audio) y documentar parámetros.

Este roadmap es la fuente de verdad para los agentes. Si se requiere ajuste mayor, actualizar también el GDD y comunicar en el `CHANGELOG`.
