package org.foxstudio.dinoworldboss.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.foxstudio.dinoworldboss.DinoWorldBoss;
import org.foxstudio.dinoworldboss.ModRegistry;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DinoWorldBoss.MODID)
public final class BossDeathHandler {

    private static final java.util.Set<String> BOSS_IDS = java.util.Set.of(
            "cataclysm:the_leviathan",
            "cataclysm:ancient_remnant",
            "cataclysm:ender_guardian",
            "cataclysm:ignis",
            "cataclysm:the_harbinger",
            "cataclysm:maledictus",
            "cataclysm:netherite_monstrosity",
            "cataclysm:scylla"
    );

    private BossDeathHandler() {
    }

    private static boolean isBoss(LivingEntity entity) {
        if (entity == null) return false;
        net.minecraft.resources.ResourceLocation id =
                net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id != null && BOSS_IDS.contains(id.toString());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim == null || victim.level().isClientSide()) {
            return;
        }
        if (!isBoss(victim)) {
            return;
        }
        Level level = victim.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ResourceKey<Level> dimKey = level.dimension();

        MinecraftServer server = serverLevel.getServer();
        String bossName = victim.getName().getString();
        String killerName = victim.getKillCredit() != null
                ? victim.getKillCredit().getName().getString()
                : event.getSource().getEntity() != null
                    ? event.getSource().getEntity().getName().getString()
                    : "unknown";

        Component broadcast = Component.literal(
                org.foxstudio.dinoworldboss.config.DinoMessageFormatter.format(
                        org.foxstudio.dinoworldboss.config.DinoWorldBossConfig.BOSS_KILLED_MESSAGE.get(),
                        bossName, killerName, null, null, null));
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.sendSystemMessage(broadcast);
        }

        ServerPlayer killer = null;
        if (event.getSource().getEntity() instanceof ServerPlayer spKiller) {
            killer = spKiller;
            sendTitle(killer, "§5§lBoss " + bossName, "§fĐã bị kết liễu!");
        }

        // 1) đọc return position của killer từ gate TRƯỚC khi phá gate
        DinoWorldBossGateBlockEntity.ReturnPos killerReturnPos = null;
        List<GateRegistry.GateEntry> gates = GateRegistry.getGatesForTarget(dimKey);
        if (killer != null) {
            for (GateRegistry.GateEntry entry : gates) {
                if (entry.level.getBlockEntity(entry.pos) instanceof DinoWorldBossGateBlockEntity g) {
                    DinoWorldBossGateBlockEntity.ReturnPos rp = g.getReturnPositions().get(killer.getUUID());
                    if (rp != null) {
                        killerReturnPos = rp;
                        break;
                    }
                }
            }
        }

        // 2) đóng gate: đưa player về + phá mọi gate
        for (GateRegistry.GateEntry entry : gates) {
            closeGate(entry);
        }

        // 3) spawn gate về cho killer
        if (killer != null && killerReturnPos != null) {
            spawnReturnGate(killer, killerReturnPos, bossName);
        }
    }

    private static void spawnReturnGate(ServerPlayer killer, DinoWorldBossGateBlockEntity.ReturnPos rp, String bossName) {
        ServerLevel level = (ServerLevel) killer.level();
        Vec3 look = killer.getLookAngle().normalize();
        BlockPos gatePos = killer.blockPosition()
                .offset((int) Math.round(look.x * 5), 5, (int) Math.round(look.z * 5));
        Direction facing = killer.getDirection();
        level.setBlock(gatePos, ModRegistry.DINO_WORLD_BOSS_GATE.defaultBlockState()
                .setValue(org.foxstudio.dinoworldboss.block.DinoWorldBossGateBlock.FACING, facing), 3);
        if (level.getBlockEntity(gatePos) instanceof DinoWorldBossGateBlockEntity rg) {
            rg.setupReturnGate(rp.x, rp.y, rp.z, rp.yRot, rp.xRot);
            String msg = org.foxstudio.dinoworldboss.config.DinoMessageFormatter.format(
                    org.foxstudio.dinoworldboss.config.DinoWorldBossConfig.RETURN_GATE_MESSAGE.get(),
                    bossName, null, null, null, null);
            killer.sendSystemMessage(Component.literal(msg));
        }
    }

    private static void closeGate(GateRegistry.GateEntry entry) {
        ServerLevel gateLevel = entry.level;
        BlockPos pos = entry.pos;
        if (gateLevel == null) {
            return;
        }
        if (gateLevel.getBlockEntity(pos) instanceof DinoWorldBossGateBlockEntity gate) {
            for (Map.Entry<UUID, DinoWorldBossGateBlockEntity.ReturnPos> e : gate.getReturnPositions().entrySet()) {
                ServerPlayer player = gateLevel.getServer().getPlayerList().getPlayer(e.getKey());
                if (player != null && CataclysmDimensions.ALL.contains(player.level().dimension())) {
                    DinoWorldBossGateBlockEntity.ReturnPos rp = e.getValue();
                    ServerLevel returnLevel = gateLevel.getServer().getLevel(
                            ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, rp.dimension));
                    if (returnLevel != null) {
                        player.teleportTo(returnLevel, rp.x, rp.y, rp.z, rp.yRot, rp.xRot);
                        player.sendSystemMessage(Component.literal("§5[Thế Giới Boss] §fBoss đã chết, bạn được đưa về!"));
                    }
                }
            }
            gate.getReturnPositions().clear();
            // mọi gate đều tự hủy
            gateLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        GateRegistry.unregister(gateLevel, pos);
    }

    private static void sendTitle(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 5));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
    }
}
