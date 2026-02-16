package com.benjanoah.gladiatorarena;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
import net.minecraft.util.math.Vec3d;

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
                player.sendMessage(Text.literal("§6⚠ Je moet op sandstone klikken!"), true);
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
                    player.sendMessage(Text.literal("§c❌ Structure niet gevonden!"), false);
                }
                return ActionResult.FAIL;
            }
            
            // Spawn the arena above the clicked block
            BlockPos spawnPos = pos.up();
            template.place(world, spawnPos, spawnPos, new StructurePlacementData(), world.getRandom(), 2);
            
            // Spawn 5 husks randomly in the arena
            spawnHusks(world, spawnPos, 5);
            
            // Play sound effect
            world.playSound(null, spawnPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            
            // Send success message
            if (player != null) {
                player.sendMessage(Text.literal("§6✨ Colosseum opgeroepen! ⚔️🏛️"), false);
                player.sendMessage(Text.literal("§c⚔️ DE GLADIATOREN ONTWAKEN!"), false);
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
    
    private void spawnHusks(ServerWorld world, BlockPos arenaPos, int count) {
        for (int i = 0; i < count; i++) {
            // Random offset within 10x10 arena
            int offsetX = world.getRandom().nextInt(8) - 4; // -4 to +4
            int offsetZ = world.getRandom().nextInt(8) - 4; // -4 to +4
            int offsetY = 2;  // Spawn on the floor (2 blocks up from spawnPos)
            
            BlockPos huskPos = arenaPos.add(offsetX, offsetY, offsetZ);
            
            // Create and spawn husk
            HuskEntity husk = EntityType.HUSK.create(world);
            if (husk != null) {
                husk.refreshPositionAndAngles(huskPos, 0.0f, 0.0f);
                husk.setPersistent();
                world.spawnEntity(husk);
                
                GladiatorArenaMod.LOGGER.info("⚔️ Spawned husk at {}", huskPos);
            }
        }
        
        GladiatorArenaMod.LOGGER.info("✅ Spawned {} husks in the arena", count);
    }
}
