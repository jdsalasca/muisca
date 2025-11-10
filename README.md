# Muisca

Simulador de colonia con tintes RPG inspirado en RimWorld, Dwarf Fortress y la cosmovisión de la civilización muisca. El objetivo del proyecto es entregar iteraciones jugables cortas donde las decisiones mágicas, económicas y sociales dejan huella persistente en el mundo.

## Estado del prototipo

| Versión | Fecha | Notas |
| --- | --- | --- |
| 0.0.6 | 2025-11-12 | Sistema de combate/magia (stats/talentos Ceniza/Juramento), hechizos data-driven, encuentro de 3 enemigos + mini-jefe y telemetría de daño con HUD ampliado. |
| 0.0.5 | 2025-11-11 | Sistema de decisiones/reputación (puente/peaje), flags persistentes, guardado/carga (`F5/F9`) e inventario/estructuras serializados. |
| 0.0.4 | 2025-11-10 | Inventario compartido + recetas data-driven (madera→tablón→cama), cola de crafting, planos colocables (cama/caja) y HUD de recursos. |
| 0.0.3 | 2025-11-10 | Colonos migrados a ECS (Ashley), cola de trabajos de recolección con nodos compartidos, overlay de recursos y mejoras HUD/audio. |
| 0.0.2 | 2025-11-09 | Generador de biomas 96×96 tiles, 3 colonos con estados (hambre/espíritu/fatiga), conmutación de colono (Tab), malla chunk y música ambiental prototipo. |
| 0.0.1 | 2025-11-09 | Vertical slice mínima: movimiento top‑down, ciclo día/noche simulado, HUD placeholder y texturas generadas por código. |

## Requisitos

1. **JDK 25** para ejecutar el juego:
   ```bash
   ./tools/fetch-openjdk25.sh          # instala en .toolchains/linux-openjdk25
   export JAVA_HOME="$PWD/.toolchains/linux-openjdk25"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```
2. **JDK 21** para el wrapper de Gradle (8.10 aún no corre sobre 25):
   ```bash
   ./tools/fetch-openjdk21.sh          # instala en .toolchains/linux-openjdk21
   ```
   `gradle.properties` ya apunta a esas rutas, así que Gradle detecta ambos toolchains automáticamente.

## Ejecución local

**Linux/macOS:**
```bash
./gradlew desktop:run
```

**Windows:**
```cmd
gradlew.bat desktop:run
```

Atajos actuales (v0.0.6):

- `WASD` mover al colono seleccionado (ECS).
- `Shift` aplicar sprint temporal.
- `Q`/`E` lanzar hechizos primario/secundario (Ceniza/Juramento).
- `1`/`2` solicitar recetas (tablones/cama); `B`/`N` colocan planos si hay recursos suficientes.
- `H` abre/cierra decisiones (elige con números), `F5` guarda, `F9` carga, `Tab` alterna colonos, `C` grilla, `F1` debug.
- Música ambiental (`assets/audio/proto_theme.wav`) se reproduce en loop al iniciar.

## Estructura

```
muisca/
 ├─ assets/               # sprites/tiles/data/audio
 ├─ core/                 # juego principal (libGDX + ECS futura)
 ├─ desktop/              # launcher LWJGL3
 ├─ docs/                 # GDD, roadmap, backlog
 ├─ CHANGELOG.md
 ├─ README.md
 └─ settings.gradle
```

## Próximos pasos sugeridos

1. Persistir datos de mundo/recursos por chunk y exponer loaders avanzados (`assets/data/biomes.json`, `recipes/` overrides).
2. Desbloquear combate + magia (0.0.6) reutilizando la ECS y las métricas actuales.
3. Expandir economía/cultivos (0.0.7) y segundo bioma con comercio reputacional.
4. Conectar audio modular (capas por bioma/evento) y telemetría de rendimiento.

El roadmap completo con versiones 0.0.1–0.0.11 y las métricas asociadas viven en `docs/roadmap.md`. Revisa también `docs/backlog.md` y `docs/gdd.md` para lineamientos de diseño/optimización obligatorios.

## Validaciones

Pruebas unitarias actuales (cálculos de daño/resistencias):

```bash
./gradlew core:test
```
