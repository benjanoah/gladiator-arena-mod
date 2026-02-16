package com.benjanoah.gladiatorarena;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;

public class ColosseumScroll extends Item {
    
    public ColosseumScroll(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Check if we're on the server side
        if (context.getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }
        
        ServerWorld world = (ServerWorld) context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        
        // Check if the block clicked is sandstone
        if (!world.getBlockState(pos).isOf(Blocks.SANDSTONE)) {
            if (player != null) {
                player.sendMessage(Text.literal("§6⚠ You must click on sandstone!"), true);
            }
            return ActionResult.FAIL;
        }
        
        // Load and place the structure
        StructureTemplateManager manager = world.getStructureTemplateManager();
        Identifier structureId = new Identifier(GladiatorArenaMod.MOD_ID, "colosseum");
        
        try {
            StructureTemplate template = manager.getTemplate(structureId).orElse(null);
            
            if (template == null) {
                GladiatorArenaMod.LOGGER.error("❌ Colosseum structure not found!");
                if (player != null) {
                    player.sendMessage(Text.literal("§c❌ Structure not found!"), false);
                }
                return ActionResult.FAIL;
            }
            
            // Spawn the arena above the clicked block
            BlockPos spawnPos = pos.up();
            template.place(world, spawnPos, spawnPos, new StructurePlacementData(), world.getRandom(), 2);
            
            // Teleport player to the center of the arena
            if (player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos arenaCenter = spawnPos.add(10, 2, 10); // Center of 21x21 arena, 2 blocks up
                serverPlayer.teleport(arenaCenter.getX() + 0.5, arenaCenter.getY(), arenaCenter.getZ() + 0.5);
                GladiatorArenaMod.LOGGER.info("📍 Teleported {} to arena center at {}", serverPlayer.getName().getString(), arenaCenter);
                
                // Create arena session and start wave 1
                ArenaSession session = new ArenaSession(world, spawnPos, serverPlayer);
                GladiatorArenaMod.addArenaSession(serverPlayer.getUuid(), session);
                session.spawnWave1();
            }
            
            // Play sound effect
            world.playSound(null, spawnPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            
            // Send success message
            if (player != null) {
                player.sendMessage(Text.literal("§6✨ Colosseum summoned! ⚔️🏛️"), false);
            }
            
            // Consume the scroll (remove 1 from stack)
            if (player != null && !player.isCreative()) {
                stack.decrement(1);
            }
            
            GladiatorArenaMod.LOGGER.info("✅ Colosseum spawned at {} by {}", spawnPos, player != null ? player.getName().getString() : "unknown");
            
            return ActionResult.SUCCESS;
            
        } catch (Exception e) {
            GladiatorArenaMod.LOGGER.error("❌ Error spawning colosseum: ", e);
            if (player != null) {
                player.sendMessage(Text.literal("§c❌ Error: " + e.getMessage()), false);
            }
            return ActionResult.FAIL;
        }
    }
}
