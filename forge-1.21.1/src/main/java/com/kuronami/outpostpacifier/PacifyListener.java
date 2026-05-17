package com.kuronami.outpostpacifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Cancels <em>natural</em> hostile spawns inside a pacified zone
 * (Forge 1.21.1).
 *
 * <p>Blocks new {@link Monster} spawns ONLY from the two "the area keeps
 * repopulating itself" sources: {@code NATURAL} and
 * {@code CHUNK_GENERATION}. It deliberately does NOT touch
 * {@code STRUCTURE} (one-time worldgen placement), {@code PATROL}
 * (roaming pillager patrols / the raid system), {@code REINFORCEMENT},
 * {@code JOCKEY}, spawners, spawn eggs, commands or breeding, and never
 * removes mobs that already exist — purely preventative, matching the
 * stated scope ("stop natural respawns in this spot") exactly.
 *
 * <p>Forge 52 fires {@link MobSpawnEvent.FinalizeSpawn}; cancelling it
 * via {@code setSpawnCancelled(true)} prevents the mob being added to
 * the world (the Forge analogue of NeoForge's identically-named call).
 */
public class PacifyListener {

    @SubscribeEvent
    public void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
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
