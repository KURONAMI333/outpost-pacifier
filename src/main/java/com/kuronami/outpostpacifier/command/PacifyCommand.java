package com.kuronami.outpostpacifier.command;

import com.kuronami.outpostpacifier.PacifyData;
import com.mojang.brigadier.Command;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /pacify [radius]} marks the square zone around you (default
 * radius 48) as no-natural-hostile-spawn. {@code /pacify list} shows
 * your zones, {@code /unpacify} clears the one you're standing in. All
 * op-only; nothing is destroyed, only future natural spawns blocked.
 */
public class PacifyCommand {

    private static final int DEFAULT_RADIUS = 48;

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        var d = event.getDispatcher();
        d.register(Commands.literal("pacify")
            .requires(s -> s.hasPermission(2))
            .executes(ctx -> add(ctx, DEFAULT_RADIUS))
            .then(Commands.literal("list").executes(this::list))
            .then(Commands.argument("radius", IntegerArgumentType.integer(8, 128))
                .executes(ctx -> add(ctx, IntegerArgumentType.getInteger(ctx, "radius")))));
        d.register(Commands.literal("unpacify")
            .requires(s -> s.hasPermission(2))
            .executes(this::unpacify));
    }

    private int add(CommandContext<CommandSourceStack> ctx, int radius) {
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

    private int unpacify(CommandContext<CommandSourceStack> ctx) {
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

    private int list(CommandContext<CommandSourceStack> ctx) {
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
