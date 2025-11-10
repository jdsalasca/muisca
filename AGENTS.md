# Repository Guidelines

## Project Structure & Module Organization
- `core/`: libGDX gameplay logic (ECS components/systems, world generation, colony simulation).
- `desktop/`: LWJGL3 launcher (`DesktopLauncher`) that boots the game on Java 25.
- `assets/`: sprites, tilesets, maps, audio, and data stubs (keep `.gitkeep` files unless real assets exist).
- `docs/`: GDD, roadmap, backlog, and supporting design notes; update alongside code.
- `tools/`: helper scripts (e.g., `fetch-openjdk21.sh`) used to install matching JDK toolchains.

## Build, Test, and Development Commands
- `JAVA_HOME=$PWD/.toolchains/linux-openjdk21 ./gradlew desktop:classes` – compiles core + desktop modules.
- `JAVA_HOME=$PWD/.toolchains/linux-openjdk21 ./gradlew desktop:run` – launches the playable slice (requires WSL GUI or native desktop).
- `./tools/fetch-openjdk25.sh` / `./tools/fetch-openjdk21.sh` – install vendor JDKs under `.toolchains/`; re-run if toolchain goes missing.
- No automated test suite exists yet; when adding tests, expose them via Gradle tasks (e.g., `core:test`).

## Coding Style & Naming Conventions
- Java 25 syntax, 4‑space indentation. Follow libGDX + Ashley idioms (systems under `com.muisca.ecs.systems`, components under `com.muisca.ecs.components`).
- Keep gameplay logic in ECS systems, never inside rendering classes.
- Use descriptive class names (`SettlementScreen`, `JobBoard`) and camelCase for methods/fields.
- Commit to data-driven design: JSON/YAML lives in `assets/data/`; avoid hard-coded magic.

## Testing Guidelines
- Future tests should use JUnit via Gradle (`./gradlew test`). Place them alongside the modules they exercise (e.g., `core/src/test/java`).
- Name tests after the feature under validation (`JobBoardTest`, `DecisionFlagValidatorTest`).
- Target: prevent regressions on decisions, crafting, economy once those systems arrive.

## Commit & Pull Request Guidelines
- Follow existing history: `chore: ...`, `feat: ...`, `docs: ...`. Scope prefix + concise summary.
- Each PR should include: description of changes, build/test evidence (`desktop:classes` or `desktop:run`), and updated docs/roadmap when features shift.
- Avoid mixing unrelated work; keep commits small and logically grouped. Mention follow-up tasks or TODOs in the PR body.

## Agent-Specific Notes
- Always document new gameplay capabilities in `README.md`, `CHANGELOG.md`, and `docs/roadmap.md`.
- When modifying toolchains or build scripts, verify both Linux (WSL) and Windows notes in README remain accurate.
