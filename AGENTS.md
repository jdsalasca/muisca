# Repository Guidelines

> **Todos los agentes deben leer y actualizar este archivo antes y después de cada sesión.** Aquí se coordina el estado del proyecto, las prioridades y las reglas de trabajo.

## 1. Estado de hitos (Roadmap 0.0.1 – 0.0.11)
| Versión | Objetivo | Estado |
| --- | --- | --- |
| 0.0.1 | Bootstrap libGDX + HUD básico | ✅ |
| 0.0.2 | Mundo procedural + audio | ✅ |
| 0.0.3 | ECS + harvesting | ✅ |
| 0.0.4 | Inventario, recetas, estructuras | ✅ |
| 0.0.5 | Decisiones, reputación, guardado | ✅ |
| 0.0.6 | **Combate + magia (EN CURSO)** | 🔜 |
| 0.0.7 | Economía/cultivos + bioma 2 | 🔜 |
| 0.0.8 | Eventos + audio reactivo | 🔜 |
| 0.0.9 | Facciones/guerras | 🔜 |
| 0.0.10 | UX/accesibilidad | 🔜 |
| 0.0.11 | Optimización + modding | 🔜 |

## 2. Sprint activo – v0.0.6 Combate & Magia
Marca cada casilla cuando finalices la tarea (no antes) y enlaza el commit.
- [ ] Componentes de stats/talentos (VIT/STM/ATK/DEF/RES) y HUD asociado.
- [ ] IA de tres enemigos + mini-jefe (gestiona stamina/magia del jugador).
- [ ] Escuelas Ceniza/Juramento: hechizos, estados (quemado/vínculo) y costos.
- [ ] Telemetría de daño (overlay + logging) para balance.
- [ ] Pruebas o validadores para cálculos de daño/resistencias.

## 3. Flujo de trabajo para agentes
1. **Anuncia tu trabajo**: al empezar, añade tu nombre en esta sección describiendo qué casillas del sprint abordarás.
2. **Plan de commits**: define en esta sección (o en la PR) los pasos numerados antes de tocar código; si otra persona entra, sabrá qué queda.
3. **Actualiza documentación**: cualquier feature debe reflejarse en `README.md`, `CHANGELOG.md`, `docs/roadmap.md`, `docs/backlog.md` y, si aplica, `AGENTS.md`.
4. **Registra validaciones**: indica en el commit/PR qué comandos se corrieron (`desktop:classes`, `desktop:run`, tests) o por qué no se pudieron ejecutar.
5. **Hand-off**: antes de salir, marca las casillas que completaste, anota pendientes y deja instrucciones claras aquí.

## 4. Estructura del proyecto
- `core/`: gameplay (ECS, mundo, colonos, crafting, decisiones, guardado).
- `desktop/`: launcher LWJGL3 (`DesktopLauncher`) – imprime la versión de Java utilizada.
- `assets/`: `sprites/`, `tilesets/`, `maps/`, `audio/`, `data/` (recetas, decisiones, estructuras, etc.).
- `docs/`: GDD, roadmap, backlog, AGENTS (este archivo). Mantenerlos sincronizados.
- `tools/`: scripts de toolchain/validación (ej. `fetch-openjdk21.sh`).

## 5. Comandos clave
- `JAVA_HOME=$PWD/.toolchains/linux-openjdk21 ./gradlew desktop:classes` – compila todo.
- `JAVA_HOME=$PWD/.toolchains/linux-openjdk21 ./gradlew desktop:run` – ejecuta slice (usar `timeout 20s` en WSL si no hay GUI).
- `./tools/fetch-openjdk25.sh` / `./tools/fetch-openjdk21.sh` – reinstala JDKs locales bajo `.toolchains/`.
- Añadir pruebas => expón tareas Gradle (`core:test`, etc.) y documenta cómo correrlas.

## 6. Estilo y commits
- Java 25, indentación de 4 espacios. Lógica de juego sólo en componentes/sistemas ECS.
- Prefijos de commit: `feat:`, `chore:`, `docs:`, `fix:`. Cambios grandes → varios commits pequeños.
- Cada PR debe incluir descripción, pruebas/ejecuciones realizadas y enlaces a docs actualizados.

## 7. Testing
- Sin suite formal aún; al agregar lógica sensible (decisiones, crafting, combate), crea pruebas JUnit en `core/src/test/java`.
- Nombra los tests según la unidad (`DecisionEngineTest`, `DamageCalculatorTest`).
- Objetivo: evitar regresiones en reglas, economía y combate.

## 8. Notas finales
- Siempre mantener README/CHANGELOG/roadmap/backlog alineados con el último build jugable.
- Toolchains: `.toolchains/linux-openjdk25` (runtime), `.toolchains/linux-openjdk21` (Gradle). Verifica que README y scripts concuerden.
- Si te quedas sin tiempo, documenta en esta sección qué falta y cómo reproducir el estado actual.
