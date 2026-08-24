package org.foxstudio.dinoworldboss.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.foxstudio.dinoworldboss.ModBlockEntities;

public class DinoWorldBossGateBlockEntity extends BlockEntity {

    private ResourceLocation targetDimension;

    public DinoWorldBossGateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GATE.get(), pos, state);
    }

    public ResourceLocation getTargetDimension() {
        return targetDimension;
    }

    public void setTargetDimension(ResourceLocation targetDimension) {
        this.targetDimension = targetDimension;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TargetDimension")) {
            targetDimension = ResourceLocation.tryParse(tag.getString("TargetDimension"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (targetDimension != null) {
            tag.putString("TargetDimension", targetDimension.toString());
        }
    }
}
