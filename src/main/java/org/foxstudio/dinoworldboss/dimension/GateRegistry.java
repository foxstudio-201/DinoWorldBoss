package org.foxstudio.dinoworldboss.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class GateRegistry {

    private static final ConcurrentHashMap<BlockPos, GateEntry> GATES = new ConcurrentHashMap<>();

    public static void register(ServerLevel gateLevel, BlockPos gatePos, boolean itemPlaced) {
        GATES.put(gatePos, new GateEntry(gateLevel, gatePos, itemPlaced));
    }

    public static void unregister(ServerLevel gateLevel, BlockPos gatePos) {
        GATES.remove(gatePos);
    }

    public static void setTargetDimension(ServerLevel gateLevel, BlockPos pos, ResourceKey<Level> target) {
        GateEntry e = GATES.get(pos);
        if (e != null) {
            e.targetDimension = target;
        }
    }

    public static List<GateEntry> getGatesForTarget(ResourceKey<Level> targetDim) {
        List<GateEntry> result = new ArrayList<>();
        for (GateEntry e : GATES.values()) {
            if (e.targetDimension != null && e.targetDimension.equals(targetDim)) {
                result.add(e);
            }
        }
        return result;
    }

    public static GateEntry get(BlockPos pos) {
        return GATES.get(pos);
    }

    public static class GateEntry {
        public final ServerLevel level;
        public final BlockPos pos;
        public final boolean itemPlaced;
        public ResourceKey<Level> targetDimension;

        public GateEntry(ServerLevel level, BlockPos pos, boolean itemPlaced) {
            this.level = level;
            this.pos = pos;
            this.itemPlaced = itemPlaced;
        }
    }
}
