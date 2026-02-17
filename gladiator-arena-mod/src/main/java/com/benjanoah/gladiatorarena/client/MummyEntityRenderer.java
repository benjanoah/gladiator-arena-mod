package com.benjanoah.gladiatorarena.client;

import com.benjanoah.gladiatorarena.MummyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MummyEntityRenderer extends BipedEntityRenderer<MummyEntity, ZombieEntityModel<MummyEntity>> {

    // Path to the mummy texture (64x64 PNG, same layout as zombie skin)
    private static final Identifier TEXTURE =
        new Identifier("gladiator-arena-mod", "textures/entity/mummy.png");

    public MummyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
    }

    @Override
    public Identifier getTexture(MummyEntity entity) {
        return TEXTURE;
    }
}
