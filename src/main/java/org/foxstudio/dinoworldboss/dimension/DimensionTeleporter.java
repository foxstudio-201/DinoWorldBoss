package org.foxstudio.dinoworldboss.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public final class DimensionTeleporter {

    public static final int SLOW_FALL_SECONDS = 60;
    public static final int SPAWN_HEIGHT_OFFSET = 40;

    private DimensionTeleporter() {
    }

    public static void teleport(ServerPlayer player, ResourceLocation dimensionId) {
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        MinecraftServer server = player.server;
        ServerLevel target = server.getLevel(dimKey);
        if (target == null) {
            player.sendSystemMessage(Component.literal("Không tìm thấy dimension: " + dimensionId));
            return;
        }

        Vec3 pos = findSpawnPos(target);
        player.teleportTo(target, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());

        // slow fall 1 phút
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALL_SECONDS * 20, 1, false, false));
        player.sendSystemMessage(Component.literal("Chào mừng đến " + CataclysmDimensions.nameOf(dimKey)));
    }

    private static Vec3 findSpawnPos(ServerLevel level) {
        BlockPos origin = level.getSharedSpawnPos();
        BlockPos top = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, origin);
        double x = origin.getX() + 0.5D;
        double z = origin.getZ() + 0.5D;
        double y = Math.max(top.getY() + SPAWN_HEIGHT_OFFSET, 320);
        return new Vec3(x, y, z);
    }
}
