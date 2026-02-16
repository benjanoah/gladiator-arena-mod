package com.benjanoah.gladiatorarena;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    
    public static final Item COLOSSEUM_SCROLL = registerItem("colosseum_scroll",
            new ColosseumScroll(new FabricItemSettings().maxCount(16)));
    
    public static final Item COLOSSEUM_STAFF = registerItem("colosseum_staff",
            new ColosseumStaff(new FabricItemSettings().maxCount(1).maxDamage(0)));
    
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(GladiatorArenaMod.MOD_ID, name), item);
    }
    
    public static void registerModItems() {
        GladiatorArenaMod.LOGGER.info("Registering items for " + GladiatorArenaMod.MOD_ID);
        
        // Add to creative inventory (Tools tab)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(COLOSSEUM_SCROLL);
            entries.add(COLOSSEUM_STAFF);
        });
    }
}
