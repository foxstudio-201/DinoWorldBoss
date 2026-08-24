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
    private static int ticksSinceLastSpawn = 0;
    private static final int SPAWN_INTERVAL_TICKS = 20 * 60 * 15; // 15 phút
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
        ticksSinceLastSpawn++;
        if (ticksSinceLastSpawn >= SPAWN_INTERVAL_TICKS) {
            ticksSinceLastSpawn = 0;
            trySpawn(event.getServer());
        }
    }

    public static void trySpawn(MinecraftServer server) {
        // chọn random dimension
        ResourceKey<Level> dimKey = CataclysmDimensions.ALL.get(RANDOM.nextInt(CataclysmDimensions.ALL.size()));
        spawnForDimension(server, dimKey);
    }

    public static void spawnForDimension(MinecraftServer server, ResourceKey<Level> dimKey) {
        ServerLevel overworld = server.overworld();
        // random position
        int x = (RANDOM.nextInt(200) - 100) * 16; // -1600 to 1600 by 16
        int z = (RANDOM.nextInt(200) - 100) * 16;
        int y = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new net.minecraft.core.BlockPos(x, 0, z)).getY();

        net.minecraft.core.BlockPos origin = new net.minecraft.core.BlockPos(x, y, z);
        if (origin.getY() < 64) {
            origin = new net.minecraft.core.BlockPos(x, 64, z);
        }

        boolean placed = DinoWorldBossStructurePlacer.place(overworld, origin,
                dimKey.location());

        if (placed) {
            String name = CataclysmDimensions.nameOf(dimKey);
            Component msg = Component.literal("§5§l[Thế Giới Boss] §fCánh cổng §e" + name
                    + " §fđã mở tại §a" + origin.getX() + " " + origin.getY() + " " + origin.getZ()
                    + " §ftrong §aOverworld");
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(msg);
            }
        }
    }

    public static void setEnabled(boolean e) {
        enabled = e;
    }

    public static void resetTimer() {
        ticksSinceLastSpawn = 0;
    }
}