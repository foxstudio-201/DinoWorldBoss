package org.foxstudio.dinoworldboss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;
import org.foxstudio.dinoworldboss.dimension.CataclysmDimensions;
import org.foxstudio.dinoworldboss.dimension.DimensionTeleporter;

public class DinoWorldBossGateBlock extends Block implements EntityBlock {

    public DinoWorldBossGateBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DinoWorldBossGateBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof DinoWorldBossGateBlockEntity gate)) {
            return InteractionResult.SUCCESS;
        }
        ResourceLocation target = gate.getTargetDimension();
        if (target == null) {
            serverPlayer.sendSystemMessage(Component.literal("Cánh cổng chưa được kích hoạt."));
            return InteractionResult.SUCCESS;
        }
        DimensionTeleporter.teleport(serverPlayer, target);
        return InteractionResult.SUCCESS;
    }
}
