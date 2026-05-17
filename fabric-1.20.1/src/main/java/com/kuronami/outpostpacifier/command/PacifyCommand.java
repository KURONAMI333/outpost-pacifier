package com.kuronami.outpostpacifier.command;

import com.kuronami.outpostpacifier.PacifyData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /pacify [radius]} marks the square zone around you (default
 * radius 48) as no-natural-hostile-spawn. {@code /pacify list} shows
 * your zones, {@code /unpacify} clears the one you're standing in. All
 * op-only; nothing is destroyed, only future natural spawns blocked.
 *
 * <p>Fabric variant: registered via {@code CommandRegistrationCallback}
 * in {@code OutpostPacifierFabric}; the Brigadier tree is identical to
 * the Forge/NeoForge builds.
 */
public final class PacifyCommand {

    private static final int DEFAULT_RADIUS = 48;

    private PacifyCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("pacify")
            .requires(s -> s.hasPermission(2))
            .executes(ctx -> add(ctx, DEFAULT_RADIUS))
            .then(Commands.literal("list").executes(PacifyCommand::list))
            .then(Commands.argument("radius", IntegerArgumentType.integer(8, 128))
                .executes(ctx -> add(ctx, IntegerArgumentType.getInteger(ctx, "radius")))));
        d.register(Commands.literal("unpacify")
            .requires(s -> s.hasPermission(2))
            .executes(PacifyCommand::unpacify));
    }

    private static int add(CommandContext<CommandSourceStack> ctx, int radius) {
        CommandSourceStack src = ctx.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.translatable("pacify.playeronly"));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        String dim = shortDim(level);
        BlockPos p = player.blockPosition();
        PacifyData.get(level.getServer())
            .add(new PacifyData.Zone(dim, p.getX(), p.getY(), p.getZ(), radius));
        src.sendSuccess(() -> Component.translatable("pacify.added",
            radius, p.getX(), p.getZ()).withStyle(ChatFormatting.GREEN), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int unpacify(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.translatable("pacify.playeronly"));
            return 0;
        }
        ServerLevel level = player.serverLevel();
        BlockPos p = player.blockPosition();
        int removed = PacifyData.get(level.getServer())
            .removeAt(shortDim(level), p.getX(), p.getZ());
        if (removed == 0) {
            src.sendSuccess(() -> Component.translatable("pacify.removed.none")
                .withStyle(ChatFormatting.GRAY), false);
        } else {
            src.sendSuccess(() -> Component.translatable("pacify.removed", removed)
                .withStyle(ChatFormatting.YELLOW), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        List<PacifyData.Zone> zones = PacifyData.get(src.getServer()).zones();
        if (zones.isEmpty()) {
            src.sendSuccess(() -> Component.translatable("pacify.list.none")
                .withStyle(ChatFormatting.GRAY), false);
            return Command.SINGLE_SUCCESS;
        }
        src.sendSuccess(() -> Component.translatable("pacify.list.title", zones.size())
            .withStyle(ChatFormatting.GOLD), false);
        for (PacifyData.Zone z : zones) {
            src.sendSuccess(() -> Component.translatable("pacify.list.entry",
                z.dim(), z.x(), z.z(), z.radius()).withStyle(ChatFormatting.GRAY),
                false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static String shortDim(ServerLevel level) {
        ResourceLocation rl = level.dimension().location();
        return "minecraft".equals(rl.getNamespace()) ? rl.getPath() : rl.toString();
    }
}
