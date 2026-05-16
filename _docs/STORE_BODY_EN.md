# Outpost Pacifier

> Found a great base spot right next to a pillager outpost (or any structure) that won't stop spawning mobs? Mark a zone and it goes quiet — permanently.

A recurring r/feedthebeast request (32↑): "a mod that lets me completely clear out pillager outposts so pillagers stop spawning — found a great base spot but there's an outpost next to it." Vanilla has no toggle; datapack workarounds are global and clunky. Outpost Pacifier lets you silence a specific area.

- 🛡️ Mark a no-natural-spawn zone around where you stand
- 📏 Adjustable radius (8–128)
- 💾 Zones persist across restarts
- ↩️ Fully reversible

## What it does / Usage

Op-only (permission level 2):

- `/pacify` — add a zone at your position (default radius)
- `/pacify <8-128>` — add a zone with an explicit radius
- `/pacify list` — list active zones
- `/unpacify` — remove a zone

Natural hostile spawns inside a pacified zone are cancelled; everywhere else is untouched. Spawners and mob-farm spawns are not affected — this targets the *natural* spawning that makes a nearby outpost/structure annoying.

## Supported loaders / versions

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | planned | planned |

Forge / Fabric / 1.20.1 ports planned; this release is NeoForge 1.21.1.

## Dependencies

None.

## Compatibility & scope

Server-side. Hooks vanilla natural-spawn finalization to cancel hostiles inside marked zones; persistent zone data via vanilla world storage. No custom blocks/items, no config.

## Known limitations

Zones are axis-aligned spheres around the command position. It suppresses *natural* hostile spawning only — it does not remove already-spawned mobs (clear them once) and does not affect spawners or trial chambers.

## Install

1. Install NeoForge for Minecraft 1.21.1.
2. Drop `outpostpacifier-0.1.0.jar` into `mods/`. Server-side.

- Minecraft 1.21.1 · NeoForge · JDK 21

## Languages

Output localized in 9 languages (machine-baseline; native-speaker PRs welcome).

## License

MIT — modpack inclusion welcome, no credit required.

Author: KURONAMI
