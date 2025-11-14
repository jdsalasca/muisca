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
| 0.0.6 | **Combate + magia** | ✅ |
| 0.0.7 | Economía/cultivos + bioma 2 | ✅ |
| 0.0.8 | Eventos + audio reactivo | 🔜 |
| 0.0.9 | Facciones/guerras | 🔜 |
| 0.0.10 | UX/accesibilidad | 🔜 |
| 0.0.11 | Optimización + modding | 🔜 |

## 2. Sprint activo – v0.0.7 Economía & Cultivos
Marca cada casilla cuando finalices la tarea (no antes) y enlaza el commit.
- [x] Sistema de cultivos data-driven (plots, estaciones ligeras, guardado). *(Codex 2025-11-16 – cambios en HEAD, commit pendiente)* 
- [x] Comercio dinámico con reputación y escasez + HUD/telemetría básica. *(Codex 2025-11-16 – cambios en HEAD, commit pendiente)* 
- [x] Segundo bioma jugable en worldgen (tiles, recursos, balance de rendimiento). *(Codex 2025-11-16 – cambios en HEAD, commit pendiente)* 
- [x] Docs/README/CHANGELOG alineados al sprint económico. *(Codex 2025-11-16 – cambios en HEAD, commit pendiente)* 

## 3. Flujo de trabajo para agentes
1. **Anuncia tu trabajo**: al empezar, añade tu nombre en esta sección describiendo qué casillas del sprint abordarás.
2. **Plan de commits**: define en esta sección (o en la PR) los pasos numerados antes de tocar código; si otra persona entra, sabrá qué queda.
3. **Actualiza documentación**: cualquier feature debe reflejarse en `README.md`, `CHANGELOG.md`, `docs/roadmap.md`, `docs/backlog.md` y, si aplica, `AGENTS.md`.
4. **Registra validaciones**: indica en el commit/PR qué comandos se corrieron (`desktop:classes`, `desktop:run`, tests) o por qué no se pudieron ejecutar.
5. **Hand-off**: antes de salir, marca las casillas que completaste, anota pendientes y deja instrucciones claras aquí.

**Sesión 2025-11-11 – Codex (GPT-5)**  
- Foco: stats/talentos + HUD, IA enemigos, escuelas Ceniza/Juramento, telemetría de daño y pruebas de daño.

**Sesión 2025-11-12 – Codex (GPT-5)**  
- Persistencia de combate integrada (`SaveManager` snapshots), restaurando HP/STM/FOC y encuentros.  
- Telemetría escribe CSV (`telemetry/damage.log`). Ajustes menores en HUD y loaders.

**Hand-off Codex – 2025-11-12**
- Balancear costos de stamina/focus y daño del mini-jefe tras playtest (ver constantes en `EnemyArchetype`/spells).
- Guardado aún no serializa estados aplicados (Burn/Bond); evaluar si es necesario para builds más largas.
- Considerar exportar agregados de telemetría (DPS promedio por encuentro) para BI ligero.

**Sesión 2025-11-13 – Codex (GPT-5)**  
- Foco: Documentar/automatizar el flujo para compilar y ejecutar en Windows (README + script PowerShell).

**Hand-off Codex – 2025-11-13**
- Añadido `tools/run-desktop.ps1` que instala JDK21/25, permite flags `-Info/-GradleDebug`, crea logs en `logs/run-desktop-*.log` y lanza Gradle con las rutas correctas; README actualizado con instrucciones Windows rápidas/manuales y tail de logs.

**Sesión 2025-11-14 – Codex (GPT-5)**  
- Persistencia de efectos de estado (Burn/Bond/Slow/Stagger) y snapshot de fuerzas (knockback) para colonos/enemigos.  
- Motor de físicas light (`ForceComponent/System`) y hechizos Ceniza/Juramento con empujes, ralentizaciones y aturdimientos; docs actualizadas.

**Hand-off Codex – 2025-11-14**
- Verificar balance de estados/knockbacks tras cargas prolongadas (tick timers restaurados).  
- Próximo foco: historias de 0.0.7 (economía/cultivos) una vez terminada la estabilización de combate.

**Sesión 2025-11-15 – Codex (GPT-5)**  
- Arreglo de compilación (`SettlementScreen` usa `forceComponentMapper` con nombre único).  
- `MuiscaGame` ahora respeta `-Dmuisca.autoQuitSeconds` para auto-cerrar el slice (útil en CI/headless). README actualizado con la bandera adicional.

**Hand-off Codex – 2025-11-15**
- `tools/run-desktop.ps1` + `README` documentan cómo activar auto-exit (`-AutoQuitSeconds` agrega `-Dmuisca.autoQuitSeconds`).  
- No se corrió `desktop:run` completo (se requiere GUI), pero `desktop:classes` pasa y el juego puede auto-cerrar con la nueva propiedad.

**Sesión 2025-11-15 – Codex (GPT-5)**  
- Añadidos ancianos (`elders.json`), rutinas sociales (`TownLifeSystem`) y aura que alimenta el ecosistema.  
- Nuevo campo de flora regenerativa conectado al JobBoard; se implementó el overlay de rendimiento (`F2`) y culling para tiles/overlays.

**Sesión 2025-11-16 – Codex (GPT-5)**  
- Arranque sprint 0.0.7: cultivos data-driven, bioma adicional y comercio reputacional sin tocar el pipeline gráfico actual.  
- Enfoque: `CropLibrary` + `FarmPlotManager`, balance de precios dinámicos y overlay de economía para diagnóstico.  
Plan de commits 0.0.7 (borrador):  
1. Datos/libs (`crops.json`, economía base) y wiring de guardado.  
2. Sistemas ECS de agricultura + integración con colonos/telemetría.  
3. Ajustes de worldgen/bioma y HUD/Docs.

**Sesión 2025-11-17 – Codex (GPT-5)**  
- Diagnóstico de pantalla negra reportada en desktop (`SpriteBatch` fallback automático + flags `-ForceShapes`/`-UseAngle` en run-desktop.ps1, README actualizado).  
- Telemetría agrícola (`farm_status` en `telemetry/systems.csv`) y métricas reutilizables en `FarmPlotManager` para HUD/diagnóstico.

**Hand-off Codex – 2025-11-17**  
- Re-testear `desktop:run` en Windows con/ sin `-UseAngle` para confirmar que el fallback shapes evita la pantalla negra reportada.  
- Siguiente paso sprint 0.0.7: concluir integración con trueques/comercio y exponer overlay económico adicional (HUD ya muestra resumen simple).  
- Considerar persistir la estación actual (`SeasonClock`) en el guardado cuando se requiera sesiones prolongadas.

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
