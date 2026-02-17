package com.benjanoah.gladiatorarena;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GladiatorArenaMod implements ModInitializer {
    public static final String MOD_ID = "gladiator-arena-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<UUID, ArenaSession> activeSessions = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Gladiator Arena Mod is loading! ⚔️🏛️");
        
        // Register items
        ModItems.registerModItems();
        
        // Register entities
        ModEntities.registerModEntities();
        
        // Register entity attributes (health, damage, speed)
        FabricDefaultAttributeRegistry.register(ModEntities.MUMMY, MummyEntity.createMummyAttributes());
        
        // Register commands
        ArenaCommands.register();
        
        // Tick all active arena sessions every server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            new java.util.ArrayList<>(activeSessions.values()).forEach(ArenaSession::tick);
        });
        
        LOGGER.info("Gladiator Arena Mod initialized! 💀🏺");
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
