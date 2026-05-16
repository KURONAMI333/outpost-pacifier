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
 * <p>Blocks new {@link Monster} spawns ONLY from the two "the area keeps
 * repopulating itself" sources: {@code NATURAL} and
 * {@code CHUNK_GENERATION}. It deliberately does NOT touch
 * {@code STRUCTURE} (one-time worldgen placement), {@code PATROL}
 * (roaming pillager patrols / the raid system), {@code REINFORCEMENT},
 * {@code JOCKEY}, spawners, spawn eggs, commands or breeding, and never
 * removes mobs that already exist — purely preventative, matching the
 * stated scope ("stop natural respawns in this spot") exactly.
 */
public class PacifyListener {

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }
        switch (event.getSpawnType()) {
            case NATURAL, CHUNK_GENERATION -> {
                // the only "the area keeps repopulating" sources we block
            }
            default -> {
                // STRUCTURE / PATROL / REINFORCEMENT / JOCKEY / spawner /
                // egg / command / breeding — all left alone (out of scope)
                return;
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
