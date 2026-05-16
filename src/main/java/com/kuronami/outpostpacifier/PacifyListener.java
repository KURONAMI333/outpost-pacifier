package com.kuronami.outpostpacifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Cancels <em>natural</em> hostile spawns inside a pacified zone.
 *
 * <p>Only blocks new spawns of {@link Monster} via natural-style spawn
 * types (so the pillager outpost / cave hostiles stop coming back). It
 * never touches player-made spawners, spawn eggs, commands, breeding,
 * etc., and never removes mobs that already exist — purely preventative,
 * exactly the scope the request asks for.
 */
public class PacifyListener {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        switch (event.getSpawnType()) {
            case NATURAL, CHUNK_GENERATION, STRUCTURE, PATROL, REINFORCEMENT, JOCKEY -> {
                // these are the "keeps respawning" sources we block
            }
            default -> {
                return; // spawner / egg / command / breeding etc. left alone
            }
        }
        ServerLevel level = event.getLevel().getLevel();
        ResourceLocation rl = level.dimension().location();
        String dim = "minecraft".equals(rl.getNamespace()) ? rl.getPath() : rl.toString();

        if (PacifyData.get(level.getServer())
                .isPacified(dim, (int) Math.floor(event.getX()), (int) Math.floor(event.getZ()))) {
            event.setSpawnCancelled(true);
        }
    }
}
