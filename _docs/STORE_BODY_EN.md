Mark a zone and natural mob spawning inside it stops — permanently and reversibly. For when you've found a base spot next to a pillager outpost (or any structure) that won't stop spawning hostiles.

Vanilla has no per-area spawn toggle, and datapack workarounds are global and clunky. Outpost Pacifier silences a specific area you choose and leaves everywhere else alone.

Op-only (permission level 2):

- `/pacify` — add a zone at your position (default radius)
- `/pacify <8-128>` — add a zone with an explicit radius
- `/pacify list` — list active zones
- `/unpacify` — remove every zone overlapping your position

Natural hostile spawns inside a pacified zone are cancelled; everywhere else is untouched. Zones are axis-aligned square columns (any Y) and persist across restarts. It cancels only natural / world-gen spawns — it does not touch patrols, raids, structure-placed mobs, reinforcements, spawners, trial chambers, spawn eggs, or already-spawned mobs (clear those once). Up to 256 zones are kept.

It hooks vanilla spawn finalization only — no mixin, no custom blocks or items, no config.

Bugs and questions: comment on the CurseForge page, or DM @kuronami333 on X.

All Rights Reserved. Modpack inclusion is allowed without permission or credit. Source: https://github.com/KURONAMI333/outpost-pacifier
