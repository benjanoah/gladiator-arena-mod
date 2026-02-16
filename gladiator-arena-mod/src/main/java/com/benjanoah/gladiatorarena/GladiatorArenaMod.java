package com.benjanoah.gladiatorarena;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GladiatorArenaMod implements ModInitializer {
    public static final String MOD_ID = "gladiator-arena-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<UUID, ArenaSession> activeSessions = new HashMap<>();
    private static int tickCounter = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("Gladiator Arena Mod is loading! ⚔️🏛️");
        
        // Register items
        ModItems.registerModItems();
        
        // Register commands
        ArenaCommands.register();
        
        // Register tick handler for arena sessions
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Tick all active arena sessions (use copy to avoid ConcurrentModificationException)
            new java.util.ArrayList<>(activeSessions.values()).forEach(ArenaSession::tick);
        });
        
        LOGGER.info("Gladiator Arena Mod initialized!");
    }
    
    public static void addArenaSession(UUID playerUuid, ArenaSession session) {
        activeSessions.put(playerUuid, session);
        LOGGER.info("Added arena session for player {}", playerUuid);
    }
    
    public static void removeArenaSession(UUID playerUuid) {
        activeSessions.remove(playerUuid);
        LOGGER.info("Removed arena session for player {}", playerUuid);
    }
    
    public static ArenaSession getArenaSession(UUID playerUuid) {
        return activeSessions.get(playerUuid);
    }
}