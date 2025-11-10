# Backlog Inicial (Épicas y primeras historias)

| ID | Épica | Objetivo | Dependencias | Estado |
| --- | --- | --- | --- | --- |
| E-001 | Mundo procedural | Generar chunks, biomas y recursos básicos. | Motor base | 🔜 |
| E-002 | Colonos & scheduler | Necesidades (hambre/sueño/ánimo), prioridades y cola global. | E-001 | 🔜 |
| E-003 | Construcción & crafting | Estaciones, recetas data-driven, inventario compartido. | E-002 | 🔜 |
| E-004 | Decisiones & reputación | Flags persistentes, nodos JSON/YAML, consecuencias en comercio/acceso. | E-003 | 🔜 |
| E-005 | Combate & magia | Stats, talentos, 2 escuelas (Ceniza/Juramento), enemigos IA. | E-002 | 🔜 |
| E-006 | Economía & granja | Cultivos, estaciones, comercio dinámico. | E-003 | 🔜 |
| E-007 | Eventos & monturas | Migrantes, clima hostil, caballo/lobo para viaje y carga. | E-002 | 🔜 |
| E-008 | Facciones & guerras | Tensiones territoriales, asaltos, tributos. | E-004 | 🔜 |
| E-009 | UX & accesibilidad | Overlays, escalado, remapeo, reportes de reputación. | Todos | 🔜 |
| E-010 | Guardado & optimización | Snapshots ECS, chunk streaming, pooling. | E-001 | 🔜 |

## Historias sugeridas (próximo sprint)
1. **Chunk renderer**: cargar malla 64×64 y permitir scroll con cámara.
2. **Scheduler mínimo**: cola de trabajos (comer/dormir) con prioridades configurables por colono.
3. **Recetas JSON**: parsear 3 recetas (madera→tablón→cama) y mostrarlas en UI básica.
4. **Flag bridge_decision**: decisión binaria que cambia precios de comerciante itinerante tras guardar/cargar.
5. **Prueba unitaria**: validar que el costo de recetas escala con la rareza (placeholder).
