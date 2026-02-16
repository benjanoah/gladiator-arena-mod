package com.benjanoah.gladiatorarena;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GladiatorArenaMod implements ModInitializer {
    public static final String MOD_ID = "gladiator-arena-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Gladiator Arena Mod is loading! ⚔️🏛️");
        
        // Register items
        ModItems.registerModItems();
        
        // Register commands
        ArenaCommands.register();
        
        LOGGER.info("Gladiator Arena Mod initialized!");
    }
}