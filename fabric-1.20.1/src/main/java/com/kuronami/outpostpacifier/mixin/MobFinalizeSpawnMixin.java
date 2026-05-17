package com.kuronami.outpostpacifier.mixin;

import com.kuronami.outpostpacifier.PacifyData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric 1.20.1: cancels <em>natural</em> hostile spawns inside a
 * pacified zone. Fabric has no first-class FinalizeSpawn event, so we
 * mixin the exact vanilla method NeoForge's {@code FinalizeSpawnEvent}
 * fires from — {@link Mob#finalizeSpawn} — and reproduce the same
 * scope: only {@link Monster} mobs from {@code NATURAL} or
 * {@code CHUNK_GENERATION} (the "the area keeps repopulating itself"
 * sources). Everything else (STRUCTURE, PATROL, REINFORCEMENT, JOCKEY,
 * spawners, eggs, commands, breeding) is left alone, and mobs that
 * already exist are never removed — purely preventative.
 *
 * <p>1.20.1 signature (Mojang mappings, pre-1.20.5 — note the trailing
 * {@code @Nullable CompoundTag} that 1.21.1 no longer has):
 * {@code finalizeSpawn(ServerLevelAccessor, DifficultyInstance,
 * MobSpawnType, SpawnGroupData, CompoundTag)}. Cancelling = returning
 * {@code null} via {@code cir.setReturnValue(null)} at HEAD so the mob
 * is discarded before any spawn bookkeeping runs.
 */
@Mixin(Mob.class)
public abstract class MobFinalizeSpawnMixin {

    @Inject(method = "finalizeSpawn", at = @At("HEAD"), cancellable = true)
    private void outpostpacifier$cancelInPacifiedZone(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData,
            @Nullable CompoundTag dataTag,
            CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob self = (Mob) (Object) this;
        if (!(self instanceof Monster)) {
            return;
        }
        switch (spawnType) {
            case NATURAL, CHUNK_GENERATION -> {
                // the only "the area keeps repopulating" sources we block
            }
            default -> {
                return; // out of scope — leave alone
            }
        }
        ServerLevel serverLevel = level.getLevel();
        MinecraftServer server = serverLevel.getServer();
        if (server == null) {
            return;
        }
        ResourceLocation rl = serverLevel.dimension().location();
        String dim = "minecraft".equals(rl.getNamespace()) ? rl.getPath() : rl.toString();
        BlockPos pos = self.blockPosition();
        if (PacifyData.get(server).isPacified(dim, pos.getX(), pos.getZ())) {
            cir.setReturnValue(null);
        }
    }
}
