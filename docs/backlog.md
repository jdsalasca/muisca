# Backlog Prioritario

| ID | Release | Épica | Objetivo | Estado |
| --- | --- | --- | --- | --- |
| E-001 | 0.0.2 | Mundo procedural + audio | Generar chunks 96×96, biomas y música base. | ✅ |
| E-002 | 0.0.3 | Colonos & scheduler | Colonos como entidades ECS, necesidades y cola global de trabajos. | ✅ |
| E-003 | 0.0.4 | Construcción & crafting | Estaciones, recetas JSON, inventario compartido y UI. | ✅ |
| E-004 | 0.0.5 | Decisiones & reputación | Flags persistentes, nodos YAML/JSON con condiciones y efectos. | ✅ |
| E-005 | 0.0.6 | Combate & magia | Stats, talentos, escuelas Ceniza/Juramento, enemigos IA. | ✅ |
| E-006 | 0.0.7 | Economía & granja | Cultivos, estaciones, comercio dinámico y bioma adicional. | 🔜 |
| E-007 | 0.0.8 | Eventos & audio reactivo | Eventos sistémicos, clima y capas musicales/SFX por bioma. | 🔜 |
| E-008 | 0.0.9 | Facciones & guerras | Tensiones, mapa estratégico, asaltos/tributos/treguas. | 🔜 |
| E-009 | 0.0.10 | UX & accesibilidad | Overlays, remapeo completo, perfiles de entrada, daltonismo. | 🔜 |
| E-010 | 0.0.11 | Guardado & optimización | Chunk streaming, pooling IA, validadores en CI y soporte modding data. | 🔜 |

## Historias en foco (para 0.0.6)
1. **Estadísticas y talentos**: definir componentes ECS para VIT/STM/ATK/DEF y árbol inicial.
2. **Combate básico**: enemigos IA (3 tipos) con habilidades y mini-jefe que consume stamina/magia.
3. **Magia Ceniza/Juramento**: proyectiles + escudos con estados (quemado/vínculo) y costos de recursos.
4. **Telemetría de daño**: registrar DPS recibido/emitido para balance; overlay debug.
5. **Pruebas**: casos unitarios para cálculos de daño/resistencias.

## Parche 0.0.6.1 y soporte Java 25
- Overlay día/noche y clima (lluvia `F3`) con HUD actualizado.
- Movimiento con colisiones por eje y passability de agua (`TileType.isPassable/isWater`).
- Generador de mundo con FBM suavizado y sesgo radial para spawn central.
- Script `run-desktop.ps1`: `-GradleInfo` (evita ambigüedad con PowerShell) y `-RuntimeJdk 21|25` para seleccionar runtime.
- Desktop `run` agrega `jvmArgs --enable-native-access=ALL-UNNAMED` para preparar compatibilidad con Java 25.
