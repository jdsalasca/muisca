# Muisca

Simulador de colonia con tintes RPG inspirado en RimWorld, Dwarf Fortress y la cosmovisión de la civilización muisca. El objetivo del proyecto es entregar iteraciones jugables cortas donde las decisiones mágicas, económicas y sociales dejan huella persistente en el mundo.

## Estado del prototipo

| Versión | Fecha | Notas |
| --- | --- | --- |
| 0.0.1 | 2025-11-09 | Vertical slice mínima: movimiento top‑down, ciclo día/noche simulado, HUD placeholder y texturas generadas por código. |

## Requisitos

1. **JDK 25** para ejecutar el juego:
   ```bash
   ./tools/fetch-openjdk25.sh
   export JAVA_HOME="$PWD/.toolchains/openjdk25"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```
2. **JDK 21** para que el wrapper de Gradle funcione (Gradle 8.10 aún no corre sobre 25):
   ```bash
   ./tools/fetch-openjdk21.sh
   ```
   `gradle.properties` ya apunta a `.toolchains/openjdk21`, así que no hay que exportar variables extra.

## Ejecución local

```bash
./gradlew desktop:run
```

Atajos actuales (v0.0.1):

- `WASD` mover colonia base
- `Shift` aceleración temporal
- El HUD muestra estado ficticio de colonos, karma y stamina.

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

1. Completar loop de recolección → construcción con componentes ECS reales (Ashley/Artemis).
2. Migrar las texturas procedurales a assets reales (`assets/sprites`, `assets/tilesets`).
3. Añadir scheduler simple para 1–2 colonos y sistema de necesidades.
4. Integrar validaciones de datos (facciones, biomas, recetas) + pruebas unitarias básicas.

Más detalles en `docs/roadmap.md` y `docs/backlog.md`.
