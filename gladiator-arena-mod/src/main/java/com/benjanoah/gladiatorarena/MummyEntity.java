package com.benjanoah.gladiatorarena;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class MummyEntity extends ZombieEntity {

    private final ServerBossBar bossBar;

    public MummyEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
            Text.literal("💀 Ancient Mummy"),
            BossBar.Color.YELLOW,
            BossBar.Style.NOTCHED_10
        );
    }

    // Stats: 100 hearts, hits hard, but slow (like a mummy!)
    public static DefaultAttributeContainer.Builder createMummyAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    // Update boss bar health every tick
    @Override
    public void tick() {
        super.tick();
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
    }

    // Show boss bar when a player starts tracking this entity
    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    // Hide boss bar when a player stops tracking this entity
    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    // Mummy curse: apply Slowness II on hit!
    @Override
    public void onAttacking(Entity target) {
        super.onAttacking(target);
        if (target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
        }
    }

    // Drop the Staff of Colosseum's Might when the mummy dies!
    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        this.dropStack(new ItemStack(ModItems.COLOSSEUM_STAFF));
    }

    // Mummies branden NIET in de zon (ze zijn al duizenden jaren dood 🏺)
    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    // Clean up boss bar when the mummy is removed from the world
    @Override
    public void remove(RemovalReason reason) {
        if (!this.getWorld().isClient()) {
            this.bossBar.clearPlayers();
        }
        super.remove(reason);
    }
}
