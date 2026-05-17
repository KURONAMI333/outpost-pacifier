package com.kuronami.outpostpacifier;

import com.kuronami.outpostpacifier.command.PacifyCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outpost Pacifier — entry point (Fabric 1.21.1).
 *
 * <p>Mark a zone with {@code /pacify}; natural hostile spawns stop
 * there (persisted). Two pieces: the {@code /pacify} command tree
 * (registered via {@code CommandRegistrationCallback}) and persistent
 * zone data. The actual spawn-cancel runs in
 * {@code com.kuronami.outpostpacifier.mixin.MobFinalizeSpawnMixin}
 * because Fabric has no first-class FinalizeSpawn event — the Mixin
 * reads the same {@link PacifyData} this entry point's command writes.
 */
public class OutpostPacifierFabric implements ModInitializer {

    public static final String MOD_ID = "outpostpacifier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Outpost Pacifier ready — /pacify to mark a no-spawn zone.");
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) ->
                PacifyCommand.register(dispatcher));
    }
}
