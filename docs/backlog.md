# Backlog Prioritario

| ID | Release | Épica | Objetivo | Estado |
| --- | --- | --- | --- | --- |
| E-001 | 0.0.2 | Mundo procedural + audio | Generar chunks 96×96, biomas y música base. | ✅ |
| E-002 | 0.0.3 | Colonos & scheduler | Colonos como entidades ECS, necesidades y cola global de trabajos. | 🔜 |
| E-003 | 0.0.4 | Construcción & crafting | Estaciones, recetas JSON, inventario compartido y UI. | 🔜 |
| E-004 | 0.0.5 | Decisiones & reputación | Flags persistentes, nodos YAML/JSON con condiciones y efectos. | 🔜 |
| E-005 | 0.0.6 | Combate & magia | Stats, talentos, escuelas Ceniza/Juramento, enemigos IA. | 🔜 |
| E-006 | 0.0.7 | Economía & granja | Cultivos, estaciones, comercio dinámico y bioma adicional. | 🔜 |
| E-007 | 0.0.8 | Eventos & audio reactivo | Eventos sistémicos, clima y capas musicales/SFX por bioma. | 🔜 |
| E-008 | 0.0.9 | Facciones & guerras | Tensiones, mapa estratégico, asaltos/tributos/treguas. | 🔜 |
| E-009 | 0.0.10 | UX & accesibilidad | Overlays, remapeo completo, perfiles de entrada, daltonismo. | 🔜 |
| E-010 | 0.0.11 | Guardado & optimización | Chunk streaming, pooling IA, validadores en CI y soporte modding data. | 🔜 |

## Historias en foco (para 0.0.3)
1. **Integrar Ashley/Artemis** en `core/ecs` con sistema de movimiento + necesidades (tick fijo).
2. **Cola de trabajos**: definir prioridades (recolectar, descansar, socializar) y prototipo de scheduler determinista.
3. **Recolección básica**: instanciar nodos de recursos en el mundo y permitir que un colono los marque y “consuma”.
4. **Módulo de música**: abstraer `Music` en un `AudioDirector` que permita cambiar de pista según bioma y evento (preparando 0.0.8).
5. **Perf harness**: script en `tools/` que corra el juego en headless y registre FPS/allocs para cada commit del sprint.
