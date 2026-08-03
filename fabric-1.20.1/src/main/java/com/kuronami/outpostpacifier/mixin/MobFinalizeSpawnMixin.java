package com.kuronami.outpostpacifier.mixin;

import com.kuronami.outpostpacifier.PacifyData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 1.20.1: cancels <em>natural</em> hostile spawns inside a
 * pacified zone. Fabric has no first-class FinalizeSpawn event, and
 * unlike NeoForge, cancelling {@link net.minecraft.world.entity.Mob#finalizeSpawn}
 * on plain Fabric vanilla does <em>not</em> stop the mob from being added —
 * both {@code NaturalSpawner.spawnCategoryForPosition} (NATURAL) and
 * {@code NaturalSpawner.spawnMobsForChunkGeneration} (CHUNK_GENERATION)
 * call {@code serverLevelAccessor.addFreshEntityWithPassengers(mob)}
 * unconditionally right after {@code finalizeSpawn}, ignoring its return
 * value entirely (confirmed against the Vineflower-decompiled Fabric
 * 1.20.1 vanilla sources — {@code NaturalSpawner.java:188-191} and
 * {@code :414-421}).
 *
 * <p>Both spawn paths instead gate the mob on
 * {@link PathfinderMob#checkSpawnRules(LevelAccessor, MobSpawnType)}
 * <em>before</em> {@code finalizeSpawn}/{@code addFreshEntityWithPassengers}
 * ever run ({@code NaturalSpawner.java:187} via {@code isValidPositionForMob},
 * and {@code :416}). That method is never overridden below
 * {@link PathfinderMob} (only {@link net.minecraft.world.entity.Mob} and
 * {@link PathfinderMob} declare it, and every {@link Monster} subclass
 * resolves to {@link PathfinderMob}'s override — verified by grepping the
 * decompiled sources for {@code "boolean checkSpawnRules"}), so mixing into
 * it here is a single injection point that covers both spawn types for
 * every {@link Monster}, with no need to reach into the two different
 * {@code NaturalSpawner} call sites individually. By the time it runs the
 * candidate mob already has its spawn position ({@code moveTo}) and its
 * {@link net.minecraft.world.level.Level} (set at
 * {@code EntityType#create}), so {@code self.blockPosition()} and
 * {@code self.level()} are both valid here.
 *
 * <p>Scope matches the NeoForge/Forge cells exactly: only {@link Monster}
 * mobs from {@code NATURAL} or {@code CHUNK_GENERATION} (the "the area
 * keeps repopulating itself" sources). Everything else (SPAWNER — used by
 * mob spawner blocks, STRUCTURE, PATROL, REINFORCEMENT, JOCKEY, eggs,
 * commands, breeding) passes a different {@link MobSpawnType} and is left
 * alone by the {@code switch} below regardless of caller. Mobs that already
 * exist are never removed — purely preventative. {@code require = 0}: if a
 * future Minecraft/mapping update moves or removes this method, the mixin
 * silently no-ops instead of crashing the client (same hardening as
 * mod-006-ping-to-map's PingManagerMixin).
 *
 * <p><strong>Known gap:</strong> a third-party mod's {@link Monster}
 * subclass that overrides {@code checkSpawnRules} without calling
 * {@code super.checkSpawnRules(...)} bypasses this mixin entirely (Java
 * virtual dispatch resolves to that override, not {@link PathfinderMob}'s).
 * No vanilla {@link Monster} does this — see the class javadoc above — but
 * it's a real gap for modded servers that this mixin cannot close from the
 * Fabric side. NeoForge/Forge don't have this gap: {@code
 * FinalizeSpawnEvent} fires from the loader's own ASM patch regardless of
 * whether a mod overrides {@code finalizeSpawn}.
 */
@Mixin(PathfinderMob.class)
public abstract class MobFinalizeSpawnMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true, require = 0)
    private void outpostpacifier$cancelInPacifiedZone(
            LevelAccessor level,
            MobSpawnType spawnType,
            CallbackInfoReturnable<Boolean> cir) {
        PathfinderMob self = (PathfinderMob) (Object) this;
        if (!(self instanceof Monster)) {
            return;
        }
        switch (spawnType) {
            case NATURAL, CHUNK_GENERATION -> {
                // the only "the area keeps repopulating" sources we block
            }
            default -> {
                return; // out of scope — leave alone (e.g. SPAWNER, STRUCTURE, PATROL)
            }
        }
        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        if (server == null) {
            return;
        }
        ResourceLocation rl = serverLevel.dimension().location();
        String dim = "minecraft".equals(rl.getNamespace()) ? rl.getPath() : rl.toString();
        BlockPos pos = self.blockPosition();
        if (PacifyData.get(server).isPacified(dim, pos.getX(), pos.getZ())) {
            cir.setReturnValue(false);
        }
    }
}
