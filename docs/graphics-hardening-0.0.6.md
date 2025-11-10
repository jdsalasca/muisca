# Endurecimiento gráfico y depuración (0.0.1 → 0.0.6)

Este documento resume los cambios introducidos para hacer el render más seguro y fácilmente depoluble desde las primeras versiones hasta 0.0.6.

## Objetivos

- Mitigar pantallas negras y cuelgues por problemas de `SpriteBatch`/texturas.
- Proveer confirmación visual inmediata de que el pipeline de render está activo.
- Exponer información del entorno GL y permitir elegir emulación adecuada.

## Cambios clave

1) Fallback y overlays 100% ShapeRenderer
- F4: Fallback overlay (rectángulo y cruz verdes en el centro del mundo) para validar cámara/proyección.
- F5: Tiles shapes – rellena los tiles visibles con colores basados en `FloraField` para confirmar geometría sin depender de texturas.
- F6: Cajas de actores – dibuja cajas/cruces para colonos y enemigos y valida culling por cámara.
- F7: Modo "solo shapes" – desactiva SpriteBatch para aislar problemas y usar únicamente overlays.

2) SpriteBatch seguro
- Gating con `try/catch` alrededor de `begin()/end()`.
- En caso de excepción, se desactiva el batch (`batchAlive=false`) y queda operativo el modo "solo shapes".
- Se registran errores con `recordInitError(...)` para consulta en logs.

3) Configuración LWJGL3 robusta
- Backbuffer consistente (RGBA 8/8/8/8, depth 16, stencil 0) para minimizar incompatibilidades de driver.
- Emulación GL configurable:
  - Por defecto `GLEmulation.GL30` (OpenGL 3.2). Más estricto y estable que GL2.
  - Opción ANGLE (`-Dmuisca.angle=true`) para entornos Windows/Metal con problemas de OpenGL.

4) Logs de entorno gráfico
- En boot se imprimen: versión GL (`getGLVersion().getDebugVersionString()`), disponibilidad GL30, tamaño de backbuffer y modo continuo.

## Cómo probar

1. Pull de `feature/core` y ejecutar con JDK 21:
   - `powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\run-desktop.ps1 -RuntimeJdk 21`
2. Validar atajos:
   - F4: overlay verde visible.
   - F5: tiles coloreados con shapes.
   - F6: cajas y cruces en colonos/enemigos.
   - F7: alterna batch ON/OFF para aislar problemas.
   - C: grilla de chunks, F1: debug, F2: perf.
3. Si hay pantalla negra:
   - Probar `-GradleArgs '-Dmuisca.angle=true'` en el script de ejecución.
   - Adjuntar captura y `logs/run-desktop-*.log`.

## Interpretación rápida

- Se ve F5/F6 pero no sprites: problema con texturas/`SpriteBatch`. Jugar en modo shapes (F7) y revisar logs.
- No se ve F4/F5/F6: problema de cámara/proyección o pipeline GL. Revisar GL emulation/ANGLE.

## Impacto en versiones

- 0.0.1–0.0.4: facilitan validación del mundo y entidades aun con placeholders.
- 0.0.5–0.0.6: mejoran estabilidad del render en presencia de sistemas nuevos (clima, combate, overlays) y aceleran el diagnóstico.