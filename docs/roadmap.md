# Roadmap Operativo (Resumen)

## Hito actual: v0.0.1
- ✅ Estructura Gradle/libGDX, launcher desktop y slice visual básico con movimiento.
- 🚧 Migrar placeholder tiles → assets reales y comenzar ECS (Ashley).

## Próximos hitos

### v0.1 — Vertical Slice Base (2–3 semanas)
- Chunk streaming mínimo (un bioma), ciclo día/noche y recolección básica.
- 1–2 colonos con prioridades simples (moverse, comer, dormir).
- Construcción de muro/taller/cama + 3 recetas (tablón, cama, antorcha).
- Guardado/carga simple (snapshot de estado del chunk) + overlay de áreas de trabajo.

### v0.2 — Decisiones y Comercio
- Sistema de flags/reputación persistentes.
- Cadena de decisión (reparar puente vs peaje) que altera rutas, precios y acceso.
- Una facción visible reaccionando al jugador; precios dinámicos.

### v0.3 — RPG y Combate
- Stats básicos, árbol de talentos inicial (6 nodos) y arma cuerpo a cuerpo.
- 2 escuelas de magia (Ceniza + Juramento) con estados ardor/vínculo.
- 3 enemigos con IA gdx-ai y mini-jefe que exige gestión de stamina/hechizos.

### v0.4 — Economía y Agricultura
- Cultivos (3 plantas), estaciones ligeras, recetas nuevas (pociones/comida).
- Segundo bioma jugable (paramo solar) con tileset y bestiario propios.
- Comercio con escasez/reputación afectando precios.

### v0.5 — Eventos, Colonos y Monturas (Lite)
- Eventos: migrante, ataque menor, tormenta azufrada.
- Rasgos adicionales y prioridades extendidas; caballo como montura básica.
- Viajes más rápidos con monturas (≥30% mejora).

### v0.6 — Facciones y Guerras (Proto)
- 2–3 facciones con tensiones y territorios; mapa estratégico.
- Asaltos, tributos y treguas dinamizados por reputación y pagos.

### Alpha 0.8 / Beta 0.9 / 1.0
- Alpha: 4 biomas, 30+ recetas, 10 cadenas narrativas, UI pulida.
- Beta: contenido completo, audio final, telemetría ligera, feature freeze.
- 1.0: campaña sandbox estable, logros básicos, localización EN/ES.

## Entregables transversales
- **Docs**: actualizar GDD/roadmap/backlog tras cada sprint.
- **Testing**: empezar suite de reglas (crafting, decisiones) en v0.2+.
- **Tooling**: scripts para validar JSON, exportar Tiled y empacar atlas.
