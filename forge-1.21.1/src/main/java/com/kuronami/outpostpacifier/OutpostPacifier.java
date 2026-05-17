package com.kuronami.outpostpacifier;

import com.kuronami.outpostpacifier.command.PacifyCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outpost Pacifier — entry point (Forge 1.21.1).
 *
 * <p>Mark a zone with {@code /pacify}; natural hostile spawns stop
 * there (persisted). One spawn listener + persistent zone data + two
 * commands. No mixin, no config, no blocks/items, nothing removed —
 * only future natural hostile spawns inside chosen zones are blocked.
 *
 * <p>Forge 52.x (1.21.1) uses a {@code FMLJavaModLoadingContext}
 * {@code @Mod} constructor; only the game event bus is needed here.
 */
@Mod(OutpostPacifier.MOD_ID)
public class OutpostPacifier {

    public static final String MOD_ID = "outpostpacifier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public OutpostPacifier(FMLJavaModLoadingContext context) {
        LOGGER.info("Outpost Pacifier ready — /pacify to mark a no-spawn zone.");
        MinecraftForge.EVENT_BUS.register(new PacifyListener());
        MinecraftForge.EVENT_BUS.register(new PacifyCommand());
    }
}
