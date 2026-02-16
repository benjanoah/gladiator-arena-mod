package com.benjanoah.gladiatorarena;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ArenaCommands {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("arena")
                .then(CommandManager.literal("spawn")
                    .executes(context -> {
                        return spawnArena(context.getSource());
                    })
                )
            );
        });
    }
    
    private static int spawnArena(ServerCommandSource source) {
        ServerWorld world = source.getWorld();
        BlockPos pos = BlockPos.ofFloored(source.getPosition());
        
        GladiatorArenaMod.LOGGER.info("🔍 Arena spawn requested at: " + pos);
        
        StructureTemplateManager manager = world.getStructureTemplateManager();
        Identifier structureId = new Identifier(GladiatorArenaMod.MOD_ID, "colosseum");
        
        GladiatorArenaMod.LOGGER.info("🔍 Looking for structure: " + structureId);
        GladiatorArenaMod.LOGGER.info("🔍 MOD_ID = " + GladiatorArenaMod.MOD_ID);
        
        try {
            var optional = manager.getTemplate(structureId);
            GladiatorArenaMod.LOGGER.info("🔍 Template optional present: " + optional.isPresent());
            
            StructureTemplate template = optional.orElse(null);
            
            if (template == null) {
                GladiatorArenaMod.LOGGER.error("❌ Structure template is NULL!");
                source.sendFeedback(() -> Text.literal("❌ Structure not found!"), false);
                return 0;
            }
            
            GladiatorArenaMod.LOGGER.info("✅ Template found! Placing...");
            template.place(world, pos, pos, new StructurePlacementData(), world.getRandom(), 2);
            source.sendFeedback(() -> Text.literal("🏛️ Arena spawned!"), true);
            return 1;
            
        } catch (Exception e) {
            GladiatorArenaMod.LOGGER.error("❌ Exception: ", e);
            source.sendFeedback(() -> Text.literal("❌ Error: " + e.getMessage()), false);
            return 0;
        }
    }
}
