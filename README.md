# Muisca

Simulador de colonia con tintes RPG inspirado en RimWorld, Dwarf Fortress y la cosmovisión de la civilización muisca. El objetivo del proyecto es entregar iteraciones jugables cortas donde las decisiones mágicas, económicas y sociales dejan huella persistente en el mundo.

## Estado del prototipo

| Versión | Fecha | Notas |
| --- | --- | --- |
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

Atajos actuales (v0.0.3):

- `WASD` mover al colono seleccionado (ECS).
- `Shift` aplicar sprint temporal.
- `Tab` alternar entre colonos, `C` mostrar/ocultar grilla de chunks, `F1` datos de depuración.
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

1. Persistir datos de mundo/recursos por chunk y exponer loaders (`assets/data/biomes.json`, `recipes/`).
2. Añadir inventario compartido + estaciones (0.0.4) usando los componentes actuales.
3. Implementar cadena de decisiones + reputación (0.0.5) con validadores automáticos.
4. Conectar audio modular (capas por bioma/evento) y telemetría de rendimiento.

El roadmap completo con versiones 0.0.1–0.0.11 y las métricas asociadas viven en `docs/roadmap.md`. Revisa también `docs/backlog.md` y `docs/gdd.md` para lineamientos de diseño/optimización obligatorios.
