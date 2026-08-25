package org.foxstudio.dinoworldboss.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DinoWorldBossGateRenderer extends GeoBlockRenderer<DinoWorldBossGateBlockEntity> {

    private final GeoModel<DinoWorldBossGateBlockEntity>[] models;
    private GeoModel<DinoWorldBossGateBlockEntity> activeModel;
    private int activeIndex;

    @SuppressWarnings("unchecked")
    public DinoWorldBossGateRenderer() {
        super(new DinoWorldBossGateModel(1));
        models = new GeoModel[]{
                new DinoWorldBossGateModel(1),
                new DinoWorldBossGateModel(2),
                new DinoWorldBossGateModel(3),
                new DinoWorldBossGateModel(4)
        };
        activeModel = models[0];
        activeIndex = 0;
        this.withScale(3.0F);
    }

    @Override
    public GeoModel<DinoWorldBossGateBlockEntity> getGeoModel() {
        return activeModel;
    }

    @Override
    public long getInstanceId(DinoWorldBossGateBlockEntity animatable) {
        // mỗi model có instance id riêng -> PerIdAnimatableInstanceCache tạo manager riêng
        return animatable.getBlockPos().hashCode() + activeIndex * 397L;
    }

    @Override
    public void render(DinoWorldBossGateBlockEntity entity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        this.animatable = entity;
        int fullBright = 0xF000F0;
        for (int i = 0; i < models.length; i++) {
            activeModel = models[i];
            activeIndex = i;
            defaultRender(poseStack, entity, bufferSource, null, null, 0f, partialTick, fullBright);
        }
    }
}
