package org.foxstudio.dinoworldboss.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.foxstudio.dinoworldboss.ModBlocks;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;

import java.util.Optional;

public final class DinoWorldBossStructurePlacer {

    private static final ResourceLocation STRUCTURE =
            new ResourceLocation("minecraft", "dinoworldboss");

    private DinoWorldBossStructurePlacer() {
    }

    public static boolean place(ServerLevel level, BlockPos origin, ResourceLocation targetDim) {
        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> opt = manager.get(STRUCTURE);
        if (opt.isEmpty()) {
            return false;
        }
        StructureTemplate template = opt.get();
        net.minecraft.core.Vec3i size = template.getSize();
        // structure is 28x26x23, origin at ground corner
        BlockPos placePos = origin;
        StructurePlaceSettings settings = new StructurePlaceSettings();
        template.placeInWorld(level, placePos, placePos, settings, level.getRandom(), 2);

        // find the gate block within the placed structure and set its target dimension
        boolean found = false;
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos p = placePos.offset(x, y, z);
                    if (level.getBlockState(p).getBlock() == ModBlocks.DINO_WORLD_BOSS_GATE.get()) {
                        BlockEntity be = level.getBlockEntity(p);
                        if (be instanceof DinoWorldBossGateBlockEntity gate) {
                            gate.setTargetDimension(targetDim);
                            found = true;
                        }
                    }
                }
            }
        }
        return found;
    }
}
