package com.benjanoah.gladiatorarena;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    // The Mummy boss entity!
    public static final EntityType<MummyEntity> MUMMY = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(GladiatorArenaMod.MOD_ID, "mummy"),
        FabricEntityTypeBuilder.<MummyEntity>create(SpawnGroup.MONSTER, MummyEntity::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.95f)) // Same size as a zombie
            .build()
    );

    public static void registerModEntities() {
        GladiatorArenaMod.LOGGER.info("Registering entities for Gladiator Arena Mod 💀");
    }
}
