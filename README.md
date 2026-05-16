# Outpost Pacifier

> Found the perfect base spot, but a pillager outpost (or a cave) keeps respawning hostiles next to it? Stand there, `/pacify`, done.

## What it does

- `/pacify [radius]` — marks the square zone around you (default radius 48, any Y) as no-natural-hostile-spawn. Persists across restarts.
- `/unpacify` — clears the zone(s) covering where you stand.
- `/pacify list` — lists your zones.

Inside a zone, **natural** hostile spawns (the outpost respawn, cave mobs, patrols, etc.) are blocked. It does **not**:
- remove mobs that already exist (kill them once yourself),
- touch player spawners, spawn eggs, `/summon`, breeding, conversions — only natural-style spawns,
- change anything else.

All commands are op-only.

## Why

Recurring r/feedthebeast request (e.g. "a mod that lets you clear out pillager outposts so pillagers stop spawning — found a great base spot but there's an outpost next to it", 30+ upvotes). Vanilla outposts respawn pillagers forever with no toggle; datapack solutions are global/clunky. This is a precise, per-zone, reversible switch.

## Install

Drop `outpostpacifier-<version>.jar` into `mods/`. Server-side. No dependencies.

- Minecraft 1.21.1 · NeoForge · JDK 21

## Scope

A `FinalizeSpawnEvent` listener + persistent zone data + commands. No mixin, no config, no blocks/items. Zones are simple XZ squares (any Y) — covers the base footprint and the caves beneath it. 9 languages (machine-baseline; native PRs welcome).

## License

MIT — modpack inclusion welcome, no credit required.

Author: KURONAMI
