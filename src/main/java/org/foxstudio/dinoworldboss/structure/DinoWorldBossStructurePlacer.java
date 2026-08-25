package org.foxstudio.dinoworldboss.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import org.foxstudio.dinoworldboss.ModRegistry;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;
import org.foxstudio.dinoworldboss.dimension.CataclysmDimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class DinoWorldBossStructurePlacer {

    private static final ResourceLocation STRUCTURE =
            new ResourceLocation("minecraft", "dinoworldboss");
    private static final Logger LOG = LoggerFactory.getLogger("dinoworldboss");

    private static final Set<String> REPLACEABLES = Set.of(
            "minecraft:grass", "minecraft:tall_grass", "minecraft:fern",
            "minecraft:large_fern", "minecraft:dead_bush", "minecraft:vine",
            "minecraft:dandelion", "minecraft:poppy", "minecraft:blue_orchid",
            "minecraft:allium", "minecraft:azure_bluet", "minecraft:red_tulip",
            "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip",
            "minecraft:oxeye_daisy", "minecraft:cornflower", "minecraft:lily_of_the_valley",
            "minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush", "minecraft:peony");

    private DinoWorldBossStructurePlacer() {
    }

    public static boolean place(ServerLevel level, BlockPos origin, ResourceLocation targetDim) {
        // đọc NBT structure trực tiếp (MCEdit v3: palette + blocks)
        CompoundTag tag;
        try {
            var resource = level.getServer().getResourceManager().getResource(
                    new ResourceLocation(STRUCTURE.getNamespace(), "structures/" + STRUCTURE.getPath() + ".nbt"));
            if (resource.isEmpty()) {
                LOG.error("[dinoworldboss] không tìm thấy structure {}", STRUCTURE);
                return false;
            }
            try (var in = resource.get().open()) {
                tag = NbtIo.readCompressed(in);
            }
        } catch (Exception e) {
            LOG.error("[dinoworldboss] lỗi đọc structure {}: {}", STRUCTURE, e.getMessage());
            return false;
        }

        ListTag sizeTag = tag.getList("size", Tag.TAG_INT);
        int sx = sizeTag.getInt(0);
        int sy = sizeTag.getInt(1);
        int sz = sizeTag.getInt(2);

        // decode palette
        ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
        BlockState[] states = new BlockState[palette.size()];
        for (int i = 0; i < palette.size(); i++) {
            states[i] = decodeState(palette.getCompound(i));
        }

        // decode blocks
        List<BlockPos> posList = new ArrayList<>();
        List<BlockState> stateList = new ArrayList<>();
        ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++) {
            CompoundTag b = blocks.getCompound(i);
            ListTag p = b.getList("pos", Tag.TAG_INT);
            int state = b.getInt("state");
            if (state >= 0 && state < states.length && !states[state].isAir()) {
                posList.add(new BlockPos(p.getInt(0), p.getInt(1), p.getInt(2)));
                stateList.add(states[state]);
            }
        }

        if (org.foxstudio.dinoworldboss.config.DinoWorldBossConfig.DEBUG_LOG.get()) {
            LOG.info("[dinoworldboss] place: structure {} size {}x{}x{}, blocks={}", STRUCTURE, sx, sy, sz, posList.size());
        }
        if (posList.isEmpty()) {
            LOG.error("[dinoworldboss] place FAILED: structure rỗng tại {}", origin);
            return false;
        }
        // chọn random dimension
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = null;
        if (targetDim == null) {
            dim = CataclysmDimensions.ALL.get(level.getRandom().nextInt(CataclysmDimensions.ALL.size()));
            targetDim = dim.location();
        }

        boolean foundGate = false;
        for (int i = 0; i < posList.size(); i++) {
            BlockPos rel = posList.get(i);
            BlockPos wp = origin.offset(rel);
            BlockState existing = level.getBlockState(wp);
            if (existing.isAir()) {
                level.setBlock(wp, stateList.get(i), 2);
            } else {
                String id = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(existing.getBlock()).toString();
                if (REPLACEABLES.contains(id)) {
                    level.setBlock(wp, stateList.get(i), 2);
                }
            }
            if (stateList.get(i).getBlock() == ModRegistry.DINO_WORLD_BOSS_GATE) {
                var be = level.getBlockEntity(wp);
                if (be instanceof DinoWorldBossGateBlockEntity gate) {
                    gate.setTargetDimension(targetDim);
                    gate.setItemPlaced(false);
                    foundGate = true;
                }
            }
        }

        // tìm block đáy thấp nhất toàn structure (world Y) — chỉ dùng mức này để blend
        int bottom = Integer.MAX_VALUE;
        for (BlockPos p : posList) {
            int wy = origin.getY() + p.getY();
            if (wy < bottom) bottom = wy;
        }

        // BLEND VÀO ĐỊA HÌNH — từ block đáy thấp nhất, đổ xuống quanh structure theo dốc tự nhiên.
        // Không dùng lowY từng cột (tránh fill lên trên), không theo khung.
        Random random = new Random(level.getSeed());
        int spread = Math.max(10, Math.max(sx, sz) / 2); // bán kính lan
        int x0 = origin.getX() - spread;
        int x1 = origin.getX() + sx + spread;
        int z0 = origin.getZ() - spread;
        int z1 = origin.getZ() + sz + spread;

        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (ground >= bottom) continue; // đã ngang/vượt đáy — không cần đổ

                // khoảng cách tới block đáy structure gần nhất (theo footprint, bo góc tự nhiên)
                double dx = Math.max(0, Math.max(origin.getX() - x, x - (origin.getX() + sx - 1)));
                double dz = Math.max(0, Math.max(origin.getZ() - z, z - (origin.getZ() + sz - 1)));
                double dist = Math.sqrt(dx * dx + dz * dz);
                double taper = 1.0 - dist / spread;
                if (taper <= 0) continue;

                // đổ từ (bottom) xuống — càng xa càng nông, không bao giờ lên trên bottom
                int fillTop = ground + (int) Math.round((bottom - ground) * taper);
                if (fillTop > bottom) fillTop = bottom;
                if (fillTop <= ground) continue;

                for (int y = fillTop; y > ground; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState ex = level.getBlockState(p);
                    if (!isFillable(ex)) break;
                    int r = random.nextInt(10);
                    boolean topBlock = (y == ground + 1);
                    level.setBlock(p, topBlock
                            ? Blocks.GRASS_BLOCK.defaultBlockState()
                            : r < 4 ? Blocks.DIRT.defaultBlockState()
                            : r < 7 ? Blocks.STONE.defaultBlockState()
                            : Blocks.DIRT.defaultBlockState(), 2);
                }
            }
        }

        return foundGate;
    }

    private static boolean isFillable(BlockState st) {
        if (st.isAir()) return true;
        net.minecraft.world.level.material.FluidState f = st.getFluidState();
        if (!f.isEmpty()) return true; // nước/dung nham -> fill qua
        String id = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(st.getBlock()).toString();
        return REPLACEABLES.contains(id); // cỏ/hoa -> fill qua
    }

    private static BlockState decodeState(CompoundTag entry) {
        net.minecraft.world.level.block.Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS
                .getValue(new ResourceLocation(entry.getString("Name")));
        if (block == null) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState st = block.defaultBlockState();
        if (entry.contains("Properties", Tag.TAG_COMPOUND)) {
            CompoundTag props = entry.getCompound("Properties");
            for (String key : props.getAllKeys()) {
                Property<?> prop = block.getStateDefinition().getProperty(key);
                if (prop != null) {
                    st = applyProperty(st, prop, props.getString(key));
                }
            }
        }
        return st;
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState st, Property<T> prop, String value) {
        return prop.getValue(value).map(v -> st.setValue(prop, v)).orElse(st);
    }
}
