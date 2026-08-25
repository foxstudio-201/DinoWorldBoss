package org.foxstudio.dinoworldboss.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.foxstudio.dinoworldboss.DinoWorldBoss;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class DinoWorldBossGateModel extends GeoModel<DinoWorldBossGateBlockEntity> {

    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation(DinoWorldBoss.MODID, "textures/block/portals_the_origin_gate_portal_anim1_small_void.png"),
            new ResourceLocation(DinoWorldBoss.MODID, "textures/block/portals_the_origin_gate_portal_anim1_middle_void.png"),
            new ResourceLocation(DinoWorldBoss.MODID, "textures/block/portals_the_origin_gate_portal_anim1_big_voidpng.png"),
            new ResourceLocation(DinoWorldBoss.MODID, "textures/block/portals_the_origin_gate_portal_void.png")
    };

    private final ResourceLocation model;
    private final ResourceLocation animation;
    private final int index;

    public DinoWorldBossGateModel(int gateIndex) {
        this.index = gateIndex - 1;
        this.model = new ResourceLocation(DinoWorldBoss.MODID, "geo/block/gate_" + gateIndex + ".json");
        this.animation = new ResourceLocation(DinoWorldBoss.MODID, "animations/block/gate_" + gateIndex + ".json");
    }

    @Override
    public ResourceLocation getModelResource(DinoWorldBossGateBlockEntity animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(DinoWorldBossGateBlockEntity animatable) {
        return TEXTURES[index];
    }

    @Override
    public ResourceLocation getAnimationResource(DinoWorldBossGateBlockEntity animatable) {
        return animation;
    }
}
