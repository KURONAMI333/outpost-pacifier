package com.kuronami.outpostpacifier;

import com.kuronami.outpostpacifier.command.PacifyCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outpost Pacifier — entry point.
 *
 * <p>Mark a zone with {@code /pacify}; natural hostile spawns stop
 * there (persisted). One spawn listener + persistent zone data + two
 * commands. No mixin, no config, no blocks/items, nothing removed —
 * only future natural hostile spawns inside chosen zones are blocked.
 */
@Mod(OutpostPacifier.MOD_ID)
public class OutpostPacifier {

    public static final String MOD_ID = "outpostpacifier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public OutpostPacifier(IEventBus modBus, ModContainer container) {
        LOGGER.info("Outpost Pacifier ready — /pacify to mark a no-spawn zone.");
        NeoForge.EVENT_BUS.register(new PacifyListener());
        NeoForge.EVENT_BUS.register(new PacifyCommand());
    }
}
