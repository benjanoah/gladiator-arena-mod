package com.benjanoah.gladiatorarena.client;

import com.benjanoah.gladiatorarena.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class GladiatorArenaModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register the mummy's custom renderer (uses mummy.png texture)
        EntityRendererRegistry.register(ModEntities.MUMMY, MummyEntityRenderer::new);
    }
}
