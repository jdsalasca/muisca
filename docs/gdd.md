# Muisca — Diseño de Alto Nivel

## 1. Visión
*Muisca* es un simulador de colonia 2D inspirado en RimWorld, Dwarf Fortress y la cosmovisión muisca. El jugador funda un asentamiento en un mundo procedural donde cada pacto, hechizo o comercio deja huella en facciones y biomas vivos. Mezcla gestión (necesidades y prioridades), RPG ligero (stats, talentos, magia) y narrativa ramificada.

## 2. Fantasía del jugador
- **Cacique de destinos**: convertir una aldea en bastión o mercado sagrado.
- **Aventurero místico**: explorar ruinas, cazar bestias, dominar 5 escuelas de magia (Ceniza, Juramento, Umbra, Tempestad y Raíz).
- **Maestre de colonos**: equilibrar trabajo, ánimo y defensa frente a eventos dinámicos.
- **Agente moral**: negociar pactos, imponer tributos o liberar territorios.

## 3. Bucles
- **Meta loop**: Expandir colonia + alianzas → condiciones de victoria (maravilla, hegemonía, gloria arcana, paz).
- **Core loop**: Explorar → Recolectar → Construir/talar/minar → Craftear → Decidir → Eventos/combate → Guardar.
- **Micro loop RPG**: Encuentro → Elección → Recompensa/Consecuencia → Ajuste de reputación/flags.

## 4. Sistemas clave
- **Mundo procedural**: chunks 64×64, 3 Z-levels, biomas (bosque templado, páramo solar, estepa cenicienta, pantano azufrado, bosque de huesos, costa). Recursos, ruinas, santuarios.
- **Colonos**: necesidades (hambre, sueño, ánimo), prioridades configurables, rasgos (diligente, melancólico, pirotécnico, empático), scheduler global.
- **Economía & crafting**: recetas data-driven (JSON), estaciones ligeras, cultivo básico.
- **Decisiones & reputación**: flags persistentes por facción; nodos JSON/YAML con condiciones (inventario, reputación, clima, hora).
- **RPG/magia**: stats (VIT, STM, ATK, DEF, CRIT, RES F/A/V), talentos iniciales, armas/armaduras con rarezas. Escuelas temperamentales con estados (ardor, vínculo, terror, descarga, enredado).
- **Monturas & movimiento**: caballos/lobos en fase 2; caravanas.
- **Facciones y guerras**: Consejo de la Brasa, Liga del Juramento, Vigías del Velo + facciones menores. Eventos bélicos, tensiones y mini mapa estratégico.

## 5. Modo de juego
Sandbox guiado con objetivos sugeridos (reparar puente, fundar taller, pactar). Dificultades: relajado, clásico, implacable (permadeath opcional).

## 6. Arte y audio
Pixel art 16×16 (personajes) y 32×32 (tiles detallados). Animaciones 6–8 fps, atlases compartidos. Paletas por bioma (32–64 colores). Audio ambiental por clima/bioma + SFX para trabajos y magia.

## 7. UX
WASD o click-to-move, barra rápida, rueda de inventario, pausa táctica. Overlays de pathfinding, áreas laborales, mapa de calor de peligros, panel de reputación. Accesibilidad: escalado UI, paletas altas, remapeo total.

## 8. Tecnología
- Java 25, Gradle 8.10, libGDX 1.12+, Ashley/Artemis-ODB, gdx-ai, Box2D opcional.
- Arquitectura ECS con sistemas dedicados (render, física, IA, tareas, crafting, diálogo, eventos, guardado).
- Datos en `assets/data/` (JSON/YAML) + mapas Tiled (`.tmx`, `.tsx`).
- Guardado incremental por chunk + snapshots ECS. Optimización: chunk streaming, object pooling, batched A*.

## 9. Roadmap playable milestones
- **v0.1** Base colonia 2D (chunk único, ciclo día/noche, construcción básica).
- **v0.2** Flags y reputación (cadena de decisiones que altera comercio/accesos).
- **v0.3** Combate + magia (stats, talentos iniciales, 2 escuelas).
- **v0.4** Economía/granja + segundo bioma.
- **v0.5** Eventos, rasgos extra y monturas básicas.
- **v0.6** Facciones activas con tensiones y asaltos.
- **Alpha v0.8** 4 biomas, 30+ recetas, 10 cadenas narrativas, UI pulida.
- **Beta v0.9** Contenido completo, optimización, audio final, QA.
- **1.0** Campaña sandbox estable, logros y localización EN/ES.

## 10. Sprints propuestos
1. Proyecto base libGDX + ECS scaffolding.
2. Chunks, colisiones, día/noche.
3. Colonos (movimiento, necesidades, scheduler).
4. Inventario + crafting.
5. Decisiones (flags/reputación) + 1 cadena.
6. Combate + magia inicial.
7. Bioma 2 + cultivos.
8. Eventos + monturas.
9. Facciones y guerras (proto).
10+. Pulido Alpha/Beta.

## 11. Métricas
- Retención 30 min v0.3 ≥ 60%.
- Conversión tutorial v0.2 ≥ 85%.
- Ciclos de juego por sesión v0.5 ≥ 3.
- Bugs críticos por build < 3.

## 12. Riesgos
- **Alcance**: modularizar features (DF-lite primero).
- **Rendimiento**: throttle IA/path, regiones sucias, pooling.
- **Arte**: tilesets modulares, atlas compartidos.
- **Narrativa**: pipelines data-driven + validadores automáticos.
