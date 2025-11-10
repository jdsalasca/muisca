# Muisca

Simulador de colonia con tintes RPG inspirado en RimWorld, Dwarf Fortress y la cosmovisión de la civilización muisca. El objetivo del proyecto es entregar iteraciones jugables cortas donde las decisiones mágicas, económicas y sociales dejan huella persistente en el mundo.

## Estado del prototipo

| Versión | Fecha | Notas |
| --- | --- | --- |
| 0.0.6.1 | 2025-11-10 | Parche visual/jugable: overlay día/noche, clima lluvia con `F3`, prevención de caminar sobre agua, generación de mundo suavizada con spawn central y sprint con ambas teclas Shift. |
| 0.0.6 | 2025-11-12 | Sistema de combate/magia (stats/talentos Ceniza/Juramento), hechizos data-driven, encuentro de 3 enemigos + mini-jefe, telemetría a archivo y guardados que preservan stats/cooldowns/estados, consejeros ancianos y ecosistema regenerativo. |
| 0.0.5 | 2025-11-11 | Sistema de decisiones/reputación (puente/peaje), flags persistentes, guardado/carga (`F5/F9`) e inventario/estructuras serializados. |
| 0.0.4 | 2025-11-10 | Inventario compartido + recetas data-driven (madera→tablón→cama), cola de crafting, planos colocables (cama/caja) y HUD de recursos. |
| 0.0.3 | 2025-11-10 | Colonos migrados a ECS (Ashley), cola de trabajos de recolección con nodos compartidos, overlay de recursos y mejoras HUD/audio. |
| 0.0.2 | 2025-11-09 | Generador de biomas 96×96 tiles, 3 colonos con estados (hambre/espíritu/fatiga), conmutación de colono (Tab), malla chunk y música ambiental prototipo. |
| 0.0.1 | 2025-11-09 | Vertical slice mínima: movimiento top‑down, ciclo día/noche simulado, HUD placeholder y texturas generadas por código. |

## Requisitos

1. **JDK 25** para ejecutar el juego:
   - Linux/macOS:  
     ```bash
     ./tools/fetch-openjdk25.sh          # instala en .toolchains/linux-openjdk25
     export JAVA_HOME="$PWD/.toolchains/linux-openjdk25"
     export PATH="$JAVA_HOME/bin:$PATH"
     ```
   - Windows:  
     ```powershell
     tools\fetch-openjdk25.bat           # instala en .toolchains\openjdk25
     ```
2. **JDK 21** para el wrapper de Gradle (8.10 aún no corre sobre 25):
   - Linux/macOS:
     ```bash
     ./tools/fetch-openjdk21.sh          # instala en .toolchains/linux-openjdk21
     ```
   - Windows:
     ```powershell
     tools\fetch-openjdk21.bat           # instala en .toolchains\openjdk21
     ```
   `gradle.properties` ya apunta a esas rutas, así que Gradle detecta ambos toolchains automáticamente en Linux/macOS. En Windows los scripts/sketches de abajo se encargan de forzar el `java.home` correcto.

## Ejecución local

**Linux/macOS:**
```bash
./gradlew desktop:run
```

**Windows (rápido, recomendado):**
```powershell
pwsh -ExecutionPolicy Bypass -File tools/run-desktop.ps1          # desktop:run por defecto (abre el slice)
pwsh -File tools/run-desktop.ps1 desktop:classes                  # sólo compila
pwsh -File tools/run-desktop.ps1 -GradleInfo -LogFile logs/build.log    # agrega flags/log personalizado
pwsh -File tools/run-desktop.ps1 -GradleInfo -AutoQuitSeconds 8         # corre desktop:run y se cierra tras 8s (CI/headless)
```
El helper `tools/run-desktop.ps1` instala/actualiza ambos JDKs si faltan, exporta `JAVA_HOME` al JDK 25 y pasa `-Dorg.gradle.java.home` con la ruta del JDK 21 antes de invocar `gradlew.bat`. Cada ejecución escribe la salida completa de Gradle (y del juego si corres `desktop:run`) en `logs/run-desktop-<timestamp>.log`. Para ver el stream en vivo desde otra terminal:
```powershell
Get-Content -Wait logs\run-desktop-YYYYMMDD-HHMMSS.log
```
Flags útiles del helper:

- `-GradleInfo` / `-GradleDebug`: propagan `--info` / `--debug` a Gradle para seguir el progreso detallado (configuración, tareas, timings).
- `-LogFile <ruta>`: define manualmente dónde guardar los logs (útil para adjuntar a bugs).
- Argumentos extra (`desktop:run --scan`) se pasan directo a Gradle. Añade `-AutoQuitSeconds <seg>` (o directamente `-Dmuisca.autoQuitSeconds=<seg>`) para que el slice se cierre automáticamente cuando corres `desktop:run` sin GUI.

**Windows (manual):**
```cmd
set JAVA_HOME=%CD%\.toolchains\openjdk25
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat -Dorg.gradle.java.home=%CD%\.toolchains\openjdk21 desktop:run
```

Atajos actuales (v0.0.6):

- `WASD` mover al colono seleccionado (ECS).
- `Shift` aplicar sprint temporal.
- `Q`/`E` lanzar hechizos primario/secundario (Ceniza/Juramento).
- `1`/`2` solicitar recetas (tablones/cama); `B`/`N` colocan planos si hay recursos suficientes.
- `H` abre/cierra decisiones (elige con números), `F5` guarda, `F9` carga, `Tab` alterna colonos, `C` grilla, `F1` debug, `F2` overlay de rendimiento, `F3` clima (lluvia ON/OFF).
- Música ambiental (`assets/audio/proto_theme.wav`) se reproduce en loop al iniciar.

### Vida de la aldea y combate v0.0.6

- Elders (`assets/data/elders.json`) se unen a la colonia y aportan doctrinas persistentes (bonos de espíritu/flora + recursos rituales). Consulta el HUD para ver el aura activa del consejo.
- Los colonos tienen rutinas sociales (`TownLifeSystem`) que los llevan de sus hogares a la plaza central en horas medias para reforzar la fantasía de pueblo vivo.
- Hechizos Ceniza/Juramento ahora consideran `knockback`, `SLOW` y `STAGGER`, con físicas ligeras (`ForceSystem`) para que cada impacto empuje unidades.
- El ecosistema (`FloraField`, `EnvironmentRegrowthSystem`) regenera pasto y árboles con el tiempo; las talas registradas en el JobBoard rebrotan según la influencia de los sabios.

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

## Telemetría

- El overlay de daño (`F1` opcional) ahora se acompaña de un log CSV incremental en `telemetry/damage.log`, útil para balancear builds o comparar DPS. Se recrea automáticamente al ejecutar el juego.
- Cada fila incluye timestamp, dirección (out/in), fuente, objetivo, daño, tipo y habilidad.

## Validaciones

Pruebas unitarias actuales (cálculos de daño/resistencias):

```bash
./gradlew core:test
```
