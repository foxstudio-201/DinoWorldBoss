package org.foxstudio.dinoworldboss.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;
import org.foxstudio.dinoworldboss.dimension.CataclysmDimensions;
import org.foxstudio.dinoworldboss.dimension.GateRegistry;

public class DinoWorldBossGateBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DinoWorldBossGateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DinoWorldBossGateBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DinoWorldBossGateBlockEntity gate) {
            gate.setItemPlaced(true);
            if (gate.getTargetDimension() == null) {
                gate.setTargetDimension(randomDimension(level.getRandom()).location());
            }
            if (placer instanceof Player p) {
                p.sendSystemMessage(Component.literal("§5[Thế Giới Boss] §fCổng dẫn đến §e"
                        + gate.getTargetDimension().getPath().replace("cataclysm_", "")
                        + "§f. Đi vào để sang!"));
            }
        }
    }

    private static net.minecraft.resources.ResourceKey<Level> lastDimension;

    private static net.minecraft.resources.ResourceKey<Level> randomDimension(net.minecraft.util.RandomSource random) {
        int size = CataclysmDimensions.ALL.size();
        net.minecraft.resources.ResourceKey<Level> dim;
        do {
            dim = CataclysmDimensions.ALL.get(random.nextInt(size));
        } while (size > 1 && dim.equals(lastDimension));
        lastDimension = dim;
        return dim;
    }

    // không phá được
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public float getExplosionResistance() {
        return 6_000_000.0F;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : (lvl, pos, st, be) -> DinoWorldBossGateBlockEntity.tick(lvl, pos, st, (DinoWorldBossGateBlockEntity) be);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
}
