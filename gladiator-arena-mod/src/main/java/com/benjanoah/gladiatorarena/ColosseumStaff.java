package com.benjanoah.gladiatorarena;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ColosseumStaff extends Item {
    
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TICKS = 1200; // 60 seconds (20 ticks per second)
    private static final int EFFECT_DURATION_TICKS = 400; // 20 seconds
    
    public ColosseumStaff(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        
        if (!world.isClient()) {
            UUID playerUuid = player.getUuid();
            long currentTime = world.getTime();
            
            // Check cooldown
            if (cooldowns.containsKey(playerUuid)) {
                long lastUse = cooldowns.get(playerUuid);
                long ticksPassed = currentTime - lastUse;
                
                if (ticksPassed < COOLDOWN_TICKS) {
                    // Still on cooldown
                    long ticksRemaining = COOLDOWN_TICKS - ticksPassed;
                    int secondsRemaining = (int) (ticksRemaining / 20);
                    player.sendMessage(Text.literal("§c⏱ Staff recharging... " + secondsRemaining + "s remaining"), true);
                    
                    // Fail sound
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.5f, 1.0f);
                    
                    return TypedActionResult.fail(stack);
                }
            }
            
            // Activate staff powers!
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, EFFECT_DURATION_TICKS, 1)); // Strength 2
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, EFFECT_DURATION_TICKS, 1)); // Speed 2
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, EFFECT_DURATION_TICKS, 1)); // Jump Boost 2
            
            // Set cooldown
            cooldowns.put(playerUuid, currentTime);
            
            // Success messages
            player.sendMessage(Text.literal("§5✨ COLOSSEUM'S MIGHT ACTIVATED! ⚔️"), false);
            player.sendMessage(Text.literal("§6💪 Strength 2 | 🏃 Speed 2 | 🦘 Jump Boost 2 §7(20s)"), false);
            
            // Epic sounds
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 1.2f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.5f);
            
            GladiatorArenaMod.LOGGER.info("⚡ {} activated Staff of Colosseum's Might", player.getName().getString());
        }
        
        return TypedActionResult.success(stack);
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Always show enchantment glint
    }
}
