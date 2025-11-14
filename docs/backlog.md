# Backlog Prioritario

| ID | Release | Épica | Objetivo | Estado |
| --- | --- | --- | --- | --- |
| E-001 | 0.0.2 | Mundo procedural + audio | Generar chunks 96×96, biomas y música base. | ✅ |
| E-002 | 0.0.3 | Colonos & scheduler | Colonos como entidades ECS, necesidades y cola global de trabajos. | ✅ |
| E-003 | 0.0.4 | Construcción & crafting | Estaciones, recetas JSON, inventario compartido y UI. | ✅ |
| E-004 | 0.0.5 | Decisiones & reputación | Flags persistentes, nodos YAML/JSON con condiciones y efectos. | ✅ |
| E-005 | 0.0.6 | Combate & magia | Stats, talentos, escuelas Ceniza/Juramento, enemigos IA. | ✅ |
| E-006 | 0.0.7 | Economía & granja | Cultivos, estaciones, comercio dinámico y bioma adicional. | ✅ |
| E-007 | 0.0.8 | Eventos & audio reactivo | Eventos sistémicos, clima y capas musicales/SFX por bioma. | 🔜 |
| E-008 | 0.0.9 | Facciones & guerras | Tensiones, mapa estratégico, asaltos/tributos/treguas. | 🔜 |
| E-009 | 0.0.10 | UX & accesibilidad | Overlays, remapeo completo, perfiles de entrada, daltonismo. | 🔜 |
| E-010 | 0.0.11 | Guardado & optimización | Chunk streaming, pooling IA, validadores en CI y soporte modding data. | 🔜 |

## Historias en foco (0.0.7 – Economía & Agricultura)
1. **Cultivos data-driven**: biblioteca JSON de cultivos, `FarmPlotManager` con rotaciones, guardado de parcelas y reloj ligero de temporadas.
2. **Comercio dinámico**: `MarketPriceTracker` ligado a reputación, HUD con precios contextuales y hooks hacia futuros contratos/trueques.
3. **Overlay + telemetría agrícola**: `F8` muestra estado por parcela y `SystemTelemetry` registra `farm_status` (temporada, barbecho/creciendo/listos, progreso medio) para balance remoto.
4. **Safe boot Windows**: scripts `run-desktop.ps1` con `-ForceShapes`/`-UseAngle` más fallback automático del SpriteBatch para evitar pantallas negras en GPUs problemáticas.

## Parche 0.0.6.1 y soporte Java 25
- Overlay día/noche y clima (lluvia `F3`) con HUD actualizado.
- Movimiento con colisiones por eje y passability de agua (`TileType.isPassable/isWater`).
- Generador de mundo con FBM suavizado y sesgo radial para spawn central.
- Script `run-desktop.ps1`: `-GradleInfo` (evita ambigüedad con PowerShell) y `-RuntimeJdk 21|25` para seleccionar runtime.
- Desktop `run` agrega `jvmArgs --enable-native-access=ALL-UNNAMED` para preparar compatibilidad con Java 25.

## Parche 0.0.6.2 (gameplay clima/día-noche)
- Penalización de regeneración (stamina/focus) en lluvia y de noche: `CombatResourceSystem.setRegenScale` aplicado cada frame.
- Regrowth de flora acelerado cuando llueve: `EnvironmentRegrowthSystem.setWeatherRegrowMultiplier` multiplica `JobBoard.update` y `randomRegrow`.
- Verificación en JDK 25 (runtime por defecto) y opción `-RuntimeJdk 21|25` para comparar warnings.
