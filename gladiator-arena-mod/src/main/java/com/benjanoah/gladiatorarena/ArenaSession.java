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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArenaSession {
    private final ServerWorld world;
    private final BlockPos arenaPos;
    private final ServerPlayerEntity player;
    private int currentWave = 1;
    private final Set<UUID> waveHusks = new HashSet<>();

    // Wave countdown timers (-1 = not active)
    private int wave2Countdown = -1;
    private int wave3Countdown = -1;

    // Track the mummy boss UUID
    private UUID mummyUuid = null;

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

    public void tick() {
        // Wave 2 countdown
        if (wave2Countdown > 0) {
            wave2Countdown--;
            if (wave2Countdown == 0) {
                player.sendMessage(Text.literal("§c🌊 WAVE 2 INCOMING - 7 HUSKS!"), false);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 1.0f, 0.8f);
                spawnHusks(7);
                wave2Countdown = -1;
            }
        }

        // Wave 3 countdown
        if (wave3Countdown > 0) {
            wave3Countdown--;
            if (wave3Countdown == 0) {
                player.sendMessage(Text.literal("§4💀 WAVE 3 - THE ANCIENT MUMMY AWAKENS!"), false);
                player.sendMessage(Text.literal("§6🏺 Beware the Mummy Curse!"), false);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 1.5f, 0.5f);
                spawnMummy();
                wave3Countdown = -1;
            }
        }

        // Check wave completion
        checkWaveComplete();
    }

    private void checkWaveComplete() {
        // Don't check during countdowns
        if (wave2Countdown > 0 || wave3Countdown > 0) {
            return;
        }

        if (currentWave == 1 || currentWave == 2) {
            // Remove dead husks from tracking
            waveHusks.removeIf(uuid -> world.getEntity(uuid) == null || !world.getEntity(uuid).isAlive());

            if (waveHusks.isEmpty() && currentWave > 0) {
                if (currentWave == 1) {
                    startWave2();
                } else if (currentWave == 2) {
                    startWave3();
                }
            }
        } else if (currentWave == 3 && mummyUuid != null) {
            // Check if the mummy boss is dead
            var mummy = world.getEntity(mummyUuid);
            if (mummy == null || !mummy.isAlive()) {
                mummyUuid = null;
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

        player.sendMessage(Text.literal("§6✨ WAVE 1 COMPLETE!"), false);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // 3 second countdown before wave 2
        wave2Countdown = 60;
    }

    private void startWave3() {
        currentWave = 3;
        waveHusks.clear();

        // Teleport player back to center
        BlockPos center = arenaPos.add(10, 2, 10);
        player.teleport(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);

        player.sendMessage(Text.literal("§6✨ WAVE 2 COMPLETE!"), false);
        player.sendMessage(Text.literal("§c💀 Something ancient stirs..."), false);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // 3 second countdown before the mummy appears
        wave3Countdown = 60;
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

    private void spawnMummy() {
        BlockPos center = arenaPos.add(10, 2, 10);

        MummyEntity mummy = ModEntities.MUMMY.create(world);
        if (mummy != null) {
            mummy.refreshPositionAndAngles(center, 0.0f, 0.0f);
            mummy.setPersistent();
            world.spawnEntity(mummy);
            mummyUuid = mummy.getUuid();

            GladiatorArenaMod.LOGGER.info("💀 Spawned Ancient Mummy at {}", center);
        }
    }

    private void spawnRewardChest() {
        player.sendMessage(Text.literal("§6✨ ARENA COMPLETE! 🎁"), false);
        player.sendMessage(Text.literal("§e💎 The Mummy has been defeated! Claim your reward!"), false);

        world.playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Spawn chest at center
        BlockPos chestPos = arenaPos.add(10, 2, 10);
        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());

        // Fill chest with loot
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setStack(0, new ItemStack(Items.DIAMOND, 2 + world.getRandom().nextInt(3)));
            chest.setStack(1, new ItemStack(Items.IRON_INGOT, 5 + world.getRandom().nextInt(6)));
            chest.setStack(2, new ItemStack(Items.GOLD_INGOT, 3 + world.getRandom().nextInt(5)));
            chest.setStack(3, new ItemStack(ModItems.COLOSSEUM_STAFF));
            chest.setStack(4, new ItemStack(Items.GOLDEN_APPLE, 2));

            GladiatorArenaMod.LOGGER.info("🎁 Spawned reward chest at {}", chestPos);
        }

        // End this arena session
        GladiatorArenaMod.removeArenaSession(player.getUuid());
    }

    public UUID getPlayerUuid() {
        return player.getUuid();
    }
}
