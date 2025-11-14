# Muisca

Simulador de colonia con tintes RPG inspirado en RimWorld, Dwarf Fortress y la cosmovisión de la civilización muisca. El objetivo del proyecto es entregar iteraciones jugables cortas donde las decisiones mágicas, económicas y sociales dejan huella persistente en el mundo.

## Estado del prototipo

| Versión | Fecha | Notas |
| --- | --- | --- |
| 0.0.7 | 2025-11-16 | Economía/agricultura: cultivos data-driven, plots con temporadas, comercio reputacional y bioma Valle Nublado con overlay agrícola (`F8`). |
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
pwsh -File tools/run-desktop.ps1 -RuntimeJdk 21 -GradleInfo             # opcional: ejecutar con runtime JDK 21 para comparar y reducir warnings LWJGL
pwsh -File tools/run-desktop.ps1 -ForceShapes -AutoQuitSeconds 5        # fuerza el modo shapes (fallback) útil si ves pantalla negra
pwsh -File tools/run-desktop.ps1 -UseAngle                              # lanza con ANGLE GLES20 (drivers problemáticos en Windows)
```
El helper `tools/run-desktop.ps1` instala/actualiza ambos JDKs si faltan, exporta `JAVA_HOME` al JDK 25 (por defecto, configurable con `-RuntimeJdk 21|25`) y pasa `-Dorg.gradle.java.home` con la ruta del JDK 21 antes de invocar `gradlew.bat`. Cada ejecución escribe la salida completa de Gradle (y del juego si corres `desktop:run`) en `logs/run-desktop-<timestamp>.log`. Para ver el stream en vivo desde otra terminal:
```powershell
Get-Content -Wait logs\run-desktop-YYYYMMDD-HHMMSS.log
```
Flags útiles del helper:

- `-GradleInfo` / `-GradleDebug`: propagan `--info` / `--debug` a Gradle para seguir el progreso detallado (configuración, tareas, timings).
- `-LogFile <ruta>`: define manualmente dónde guardar los logs (útil para adjuntar a bugs).
- Argumentos extra (`desktop:run --scan`) se pasan directo a Gradle. Añade `-AutoQuitSeconds <seg>` (o directamente `-Dmuisca.autoQuitSeconds=<seg>`) para que el slice se cierre automáticamente cuando corres `desktop:run` sin GUI.
- `-RuntimeJdk 21|25`: selecciona el JDK de runtime; por defecto usa 25 para un juego más actualizado.
- `-ForceShapes`: agrega `-Dmuisca.forceShapes=true` y enciende el modo “solo ShapeRenderer”. Garantiza que siempre veas al menos la grilla/overlays si el SpriteBatch falla.
- `-UseAngle`: agrega `-Dmuisca.angle=true` y busca automáticamente las DLLs de ANGLE (Chrome/Edge). Recomendado cuando ciertas GPUs muestran ventana negra con el backend GL30.

**Windows (manual):**
```cmd
set JAVA_HOME=%CD%\.toolchains\openjdk25
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat -Dorg.gradle.java.home=%CD%\.toolchains\openjdk21 desktop:run
```

Atajos actuales (v0.0.7):

- `WASD` mover al colono seleccionado (ECS).
- `Shift` aplicar sprint temporal.
- `Q`/`E` lanzar hechizos primario/secundario (Ceniza/Juramento).
- `1`/`2` solicitar recetas (tablones/cama); `B`/`N` colocan planos si hay recursos suficientes.
- `H` abre/cierra decisiones (elige con números), `F5` guarda, `F9` carga, `Tab` alterna colonos, `C` grilla, `F1` debug, `F2` overlay de rendimiento, `F3` clima (lluvia ON/OFF), `F4` fallback shapes, `F5` tiles con ShapeRenderer, `F6` cajas de actores, `F7` modo solo shapes, `F8` overlay agrícola.
- Música ambiental (`assets/audio/proto_theme.wav`) se reproduce en loop al iniciar.

### Vida de la aldea y combate v0.0.6

- Elders (`assets/data/elders.json`) se unen a la colonia y aportan doctrinas persistentes (bonos de espíritu/flora + recursos rituales). Consulta el HUD para ver el aura activa del consejo.
- Los colonos tienen rutinas sociales (`TownLifeSystem`) que los llevan de sus hogares a la plaza central en horas medias para reforzar la fantasía de pueblo vivo.
- Hechizos Ceniza/Juramento ahora consideran `knockback`, `SLOW` y `STAGGER`, con físicas ligeras (`ForceSystem`) para que cada impacto empuje unidades.
- El ecosistema (`FloraField`, `EnvironmentRegrowthSystem`) regenera pasto y árboles con el tiempo; las talas registradas en el JobBoard rebrotan según la influencia de los sabios.

### Economía y agricultura v0.0.7

- Nuevo `assets/data/crops.json` define cultivos data-driven (maíz, quinua, coca) con tiempos de crecimiento, temporadas preferidas y cantidades cosechadas.
- `FarmPlotManager` genera parcelas cerca del poblado (`F8` para ver el overlay). Colonos priorizan sembrar/cosechar antes de salir a talar.
- `AgricultureSystem` ejecuta un reloj de temporadas (templada/lluviosa/seca) que acelera o frena el crecimiento; el HUD muestra estado, progreso promedio y parcelas listas.
- `MarketPriceTracker` ajusta precios según reputación (Liga por defecto) y escasez en inventario; el HUD expone el resumen, útil para balancear economía.
- Telemetría (`telemetry/systems.csv`) ahora registra también el estado agrícola (temporada, parcelas barbecho/creciendo/listas y progreso medio) cada pocos segundos para balancear cultivos y comercio.
- El worldgen añade el bioma **Valle Nublado**, un segundo piso jugable con vegetación húmeda pensado para futuras rutas de comercio y cultivos especializados.

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
2. Eventos + audio reactivo (0.0.8): sistema de eventos sistémicos, capas de música/SFX por bioma/clima y progreso de rasgos.
3. Preparar historias de facciones (0.0.9): mapa estratégico liviano, tensiones y prototipo de diplomacia.
4. Telemetría adicional (economía/cultivos) para balancear costos y consumo durante sesiones largas.

El roadmap completo con versiones 0.0.1–0.0.11 y las métricas asociadas viven en `docs/roadmap.md`. Revisa también `docs/backlog.md` y `docs/gdd.md` para lineamientos de diseño/optimización obligatorios.

## Telemetría

- El overlay de daño (`F1` opcional) ahora se acompaña de un log CSV incremental en `telemetry/damage.log`, útil para balancear builds o comparar DPS. Se recrea automáticamente al ejecutar el juego.
- Cada fila incluye timestamp, dirección (out/in), fuente, objetivo, daño, tipo y habilidad.
- `telemetry/systems.csv` captura tanto los cambios de clima/regeneración como los snapshots agrícolas (temporada actual y parcelas por estado). Sirve para depurar por qué la colonia se queda sin comida o si los colonos ignoran alguna parcela.

### Diagnóstico de pantalla negra / safe boot

- Si la ventana se abre pero sólo ves un fondo vacío, prueba `tools\run-desktop.ps1 -ForceShapes` (o agrega `-Dmuisca.forceShapes=true` al ejecutar Gradle). Arranca directamente en modo ShapeRenderer con overlays (`F7` vuelve a intentar el SpriteBatch).
- Algunas GPUs/driver de Windows no se llevan bien con el backend GL30; ejecuta `-UseAngle` para forzar ANGLE (OpenGL ES 2.0 sobre DirectX). También puedes pasar manualmente `-Dmuisca.angle=true` desde cualquier invocación Gradle/IDE.

## Validaciones

Pruebas unitarias actuales (cálculos de daño/resistencias):

```bash
./gradlew core:test
```
## Soporte Java 25

- El objetivo del proyecto es usar **Java 25** como base en runtime. La tarea `desktop:run` está configurada con `jvmArgs --enable-native-access=ALL-UNNAMED` para adelantarnos a restricciones futuras de métodos nativos.
- Si detectas warnings de LWJGL (JNI y `sun.misc.Unsafe`), puedes comparar el comportamiento con `-RuntimeJdk 21`. El build sigue compilando con toolchain 25 y Gradle corre sobre 21 para máxima estabilidad.
