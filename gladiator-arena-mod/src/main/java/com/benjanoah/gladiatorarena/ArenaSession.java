package com.benjanoah.gladiatorarena;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.EnchantmentHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArenaSession {
    private final ServerWorld world;
    private final BlockPos arenaPos;
    private final ServerPlayerEntity player;
    private int currentWave = 1;
    private final Set<UUID> waveHusks = new HashSet<>();
    
    public ArenaSession(ServerWorld world, BlockPos arenaPos, ServerPlayerEntity player) {
        this.world = world;
        this.arenaPos = arenaPos;
        this.player = player;
    }
    
    public void spawnWave1() {
        currentWave = 1;
        waveHusks.clear();
        player.sendMessage(Text.literal("§c⚔️ WAVE 1 - 5 HUSKS!"), false);
        spawnHusks(5);
    }
    
    public void checkWaveComplete() {
        // Remove dead husks from tracking
        waveHusks.removeIf(uuid -> world.getEntity(uuid) == null || !world.getEntity(uuid).isAlive());
        
        if (waveHusks.isEmpty()) {
            if (currentWave == 1) {
                startWave2();
            } else if (currentWave == 2) {
                spawnRewardChest();
            }
        }
    }
    
    private void startWave2() {
        currentWave = 2;
        waveHusks.clear();
        
        // Teleport player back to center
        BlockPos center = arenaPos.add(10, 2, 10);
        player.teleport(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
        
        // Messages and sound
        player.sendMessage(Text.literal("§6✨ WAVE 1 COMPLETE!"), false);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        // Delayed wave 2 start (3 seconds)
        world.getServer().execute(() -> {
            try {
                Thread.sleep(3000);
                world.getServer().execute(() -> {
                    player.sendMessage(Text.literal("§c🌊 WAVE 2 INCOMING - 7 HUSKS!"), false);
                    world.playSound(null, player.getBlockPos(), SoundEvents.EVENT_RAID_HORN, SoundCategory.HOSTILE, 1.0f, 0.8f);
                    spawnHusks(7);
                });
            } catch (InterruptedException e) {
                GladiatorArenaMod.LOGGER.error("Wave delay interrupted", e);
            }
        });
    }
    
    private void spawnHusks(int count) {
        for (int i = 0; i < count; i++) {
            int offsetX = world.getRandom().nextInt(16) - 8;
            int offsetZ = world.getRandom().nextInt(16) - 8;
            int offsetY = 2;
            
            BlockPos huskPos = arenaPos.add(10 + offsetX, offsetY, 10 + offsetZ);
            
            HuskEntity husk = EntityType.HUSK.create(world);
            if (husk != null) {
                husk.refreshPositionAndAngles(huskPos, 0.0f, 0.0f);
                husk.setPersistent();
                world.spawnEntity(husk);
                waveHusks.add(husk.getUuid());
                
                GladiatorArenaMod.LOGGER.info("⚔️ Spawned husk {} at {}", husk.getUuid(), huskPos);
            }
        }
    }
    
    private void spawnRewardChest() {
        player.sendMessage(Text.literal("§6✨ ARENA COMPLETE! 🎁"), false);
        player.sendMessage(Text.literal("§e💎 Victory rewards await!"), false);
        
        world.playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        // Spawn chest at center
        BlockPos chestPos = arenaPos.add(10, 2, 10);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());
        
        // Fill chest with loot
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            // Diamonds (2-4)
            chest.setStack(0, new ItemStack(Items.DIAMOND, 2 + world.getRandom().nextInt(3)));
            
            // Iron (5-10)
            chest.setStack(1, new ItemStack(Items.IRON_INGOT, 5 + world.getRandom().nextInt(6)));
            
            // Gold (3-7)
            chest.setStack(2, new ItemStack(Items.GOLD_INGOT, 3 + world.getRandom().nextInt(5)));
            
            // Magic Staff (enchanted stick placeholder)
            ItemStack magicStaff = new ItemStack(Items.STICK);
            magicStaff.setCustomName(Text.literal("§5✨ Magic Staff §7(Coming Soon!)"));
            EnchantmentHelper.set(java.util.Map.of(Enchantments.KNOCKBACK, 2), magicStaff);
            chest.setStack(3, magicStaff);
            
            // Bonus: Golden Apple
            chest.setStack(4, new ItemStack(Items.GOLDEN_APPLE, 2));
            
            GladiatorArenaMod.LOGGER.info("🎁 Spawned reward chest at {}", chestPos);
        }
        
        // Remove this session from active sessions
        GladiatorArenaMod.removeArenaSession(player.getUuid());
    }
    
    public UUID getPlayerUuid() {
        return player.getUuid();
    }
}
