package org.foxstudio.dinoworldboss.scheduler;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.foxstudio.dinoworldboss.DinoWorldBoss;
import org.foxstudio.dinoworldboss.dimension.CataclysmDimensions;
import org.foxstudio.dinoworldboss.structure.DinoWorldBossStructurePlacer;

import java.util.Random;

@Mod.EventBusSubscriber(modid = DinoWorldBoss.MODID)
public final class DinoWorldBossScheduler {

    private static final Random RANDOM = new Random();
    private static long lastSpawnDay = -1;
    private static boolean enabled = true;

    private DinoWorldBossScheduler() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!enabled) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server == null || server.overworld() == null) {
            return;
        }
        // dùng in-game day (24000 tick/ngày)
        long day = server.overworld().getDayTime() / 24000L;
        if (lastSpawnDay < 0) {
            lastSpawnDay = day;
            return;
        }
        int intervalDays = org.foxstudio.dinoworldboss.config.DinoWorldBossConfig.SPAWN_INTERVAL_MINUTES.get();
        if (day - lastSpawnDay >= intervalDays) {
            lastSpawnDay = day;
            trySpawn(server);
        }
    }

    public static void trySpawn(MinecraftServer server) {
        ResourceKey<Level> dimKey = CataclysmDimensions.ALL.get(RANDOM.nextInt(CataclysmDimensions.ALL.size()));
        spawnForDimension(server, dimKey);
    }

    /** @return BlockPos nơi đặt thành công, hoặc null nếu thất bại */
    public static net.minecraft.core.BlockPos spawnForDimension(MinecraftServer server, ResourceKey<Level> dimKey) {
        ServerLevel overworld = server.overworld();
        int x = (RANDOM.nextInt(200) - 100) * 16;
        int z = (RANDOM.nextInt(200) - 100) * 16;
        net.minecraft.core.BlockPos origin = new net.minecraft.core.BlockPos(x, 64, z);

        var chunk = overworld.getChunk(x >> 4, z >> 4);
        if (!chunk.getStatus().isOrAfter(net.minecraft.world.level.chunk.ChunkStatus.FULL)) {
            return null;
        }

        int y = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new net.minecraft.core.BlockPos(x, 0, z)).getY();
        if (y < 64) {
            y = 64;
        }
        origin = new net.minecraft.core.BlockPos(x, y, z);

        boolean placed = DinoWorldBossStructurePlacer.place(overworld, origin, dimKey.location());

        if (placed) {
            String bossName = CataclysmDimensions.bossNameOf(dimKey);
            String msg = org.foxstudio.dinoworldboss.config.DinoMessageFormatter.format(
                    org.foxstudio.dinoworldboss.config.DinoWorldBossConfig.SPAWN_MESSAGE.get(),
                    bossName, null,
                    String.valueOf(origin.getX()), String.valueOf(origin.getY()), String.valueOf(origin.getZ()));
            Component component = net.minecraft.network.chat.Component.literal(msg);
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(component);
            }
            return origin;
        }
        return null;
    }

    public static void setEnabled(boolean e) {
        enabled = e;
    }

    public static void resetTimer() {
        lastSpawnDay = -1;
    }
}