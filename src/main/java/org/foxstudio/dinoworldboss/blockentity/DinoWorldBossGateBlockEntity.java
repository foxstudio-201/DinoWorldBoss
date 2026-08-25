package org.foxstudio.dinoworldboss.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.foxstudio.dinoworldboss.ModRegistry;
import org.foxstudio.dinoworldboss.dimension.CataclysmDimensions;
import org.foxstudio.dinoworldboss.dimension.DimensionTeleporter;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DinoWorldBossGateBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    public static final int UP = 15;
    public static final int DOWN = 15;
    public static final int SIDE = 15;
    public static final int FORWARD = 1;
    public static final int BACK = 1;

    private static final int COUNTDOWN_SECONDS = 5;
    private static final int COUNTDOWN_TICKS = COUNTDOWN_SECONDS * 20;

    private final AnimatableInstanceCache cache = new PerIdAnimatableInstanceCache(this);
    private ResourceLocation targetDimension;
    private boolean itemPlaced;
    private boolean returnGate;      // gate về vị trí chính xác (boss đã chết)
    private double returnX, returnY, returnZ;
    private float returnYRot, returnXRot;
    private int returnTicks;         // TTL của gate về
    private int cooldown;
    private UUID teleportingPlayer;
    private int countdown;
    private final Map<UUID, ReturnPos> returnPositions = new HashMap<>();

    public DinoWorldBossGateBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.GATE_BE, pos, state);
    }

    public ResourceLocation getTargetDimension() {
        return targetDimension;
    }

    public void setTargetDimension(ResourceLocation targetDimension) {
        this.targetDimension = targetDimension;
        setChanged();
    }

    public boolean isItemPlaced() {
        return itemPlaced;
    }

    public void setItemPlaced(boolean itemPlaced) {
        this.itemPlaced = itemPlaced;
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel sl) {
            org.foxstudio.dinoworldboss.dimension.GateRegistry.register(sl, worldPosition, itemPlaced);
            if (targetDimension != null) {
                org.foxstudio.dinoworldboss.dimension.GateRegistry.setTargetDimension(
                        sl, worldPosition,
                        net.minecraft.resources.ResourceKey.create(
                                net.minecraft.core.registries.Registries.DIMENSION, targetDimension));
            }
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel sl) {
            org.foxstudio.dinoworldboss.dimension.GateRegistry.unregister(sl, worldPosition);
        }
        super.setRemoved();
    }

    public boolean isReturnGate() {
        return returnGate;
    }

    public void setupReturnGate(double x, double y, double z, float yRot, float xRot) {
        this.returnGate = true;
        this.returnX = x;
        this.returnY = y;
        this.returnZ = z;
        this.returnYRot = yRot;
        this.returnXRot = xRot;
        this.returnTicks = 20 * 120; // 2 phút
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DinoWorldBossGateBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        if (be.targetDimension == null && !be.returnGate) {
            return;
        }
        if (be.cooldown > 0) {
            be.cooldown--;
        }
        Direction facing = state.getValue(
                org.foxstudio.dinoworldboss.block.DinoWorldBossGateBlock.FACING);
        if (be.teleportingPlayer != null) {
            // player đang countdown — nếu ra khỏi vùng thì hủy
            Player tp = level.getPlayerByUUID(be.teleportingPlayer);
            if (tp instanceof ServerPlayer tsp && be.isInsideHitbox(tsp, facing)) {
                be.tickCountdown(level);
            } else {
                // ra khỏi vùng -> hủy countdown, không dịch chuyển
                be.teleportingPlayer = null;
                be.countdown = 0;
            }
            return;
        }
        if (be.cooldown > 0) {
            return;
        }
        if (be.returnGate) {
            // gate về: tự hủy khi không còn player nào trong cataclysm dimension
            boolean anyPlayerInBossDim = false;
            for (Player p : level.players()) {
                if (p instanceof ServerPlayer sp) {
                    var dim = sp.level().dimension();
                    if (org.foxstudio.dinoworldboss.dimension.CataclysmDimensions.ALL.contains(dim)) {
                        anyPlayerInBossDim = true;
                        break;
                    }
                }
            }
            if (!anyPlayerInBossDim) {
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                return;
            }
            for (Player p : level.players()) {
                if (!(p instanceof ServerPlayer sp)) {
                    continue;
                }
                if (be.isInsideHitbox(sp, facing)) {
                    be.startCountdown(sp);
                    return;
                }
            }
            return;
        }
        for (Player p : level.players()) {
            if (!(p instanceof ServerPlayer sp)) {
                continue;
            }
            if (be.isInsideHitbox(sp, facing)) {
                be.startCountdown(sp);
                return;
            }
        }
    }

    private void startCountdown(ServerPlayer player) {
        teleportingPlayer = player.getUUID();
        countdown = COUNTDOWN_TICKS;
        sendTitle(player, "§5§lCỔNG DỊCH CHUYỂN", "§fDịch chuyển sau §e" + COUNTDOWN_SECONDS);
    }

    private void tickCountdown(Level level) {
        Player p = level.getPlayerByUUID(teleportingPlayer);
        if (!(p instanceof ServerPlayer sp)) {
            teleportingPlayer = null;
            countdown = 0;
            return;
        }
        countdown--;
        if (countdown % 20 == 0) {
            int secondsLeft = Math.max(0, (countdown + 19) / 20);
            sendTitle(sp, "§5§lCỔNG DỊCH CHUYỂN", "§fDịch chuyển sau §e" + secondsLeft);
        }
        if (countdown <= 0) {
            teleportingPlayer = null;
            countdown = 0;
            cooldown = 40;
            if (returnGate) {
                // gate về: teleport về vị trí đã lưu + tự hủy
                org.foxstudio.dinoworldboss.dimension.DimensionTeleporter.teleportToPosition(
                        sp, returnX, returnY, returnZ, returnYRot, returnXRot);
                level.setBlock(worldPosition, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            } else {
                // lưu vị trí về
                returnPositions.put(sp.getUUID(), new ReturnPos(level.dimension().location(),
                        sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot()));
                DimensionTeleporter.teleport(sp, targetDimension);
            }
        }
    }

    private void sendTitle(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 20, 5));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
    }

    private boolean isInsideHitbox(ServerPlayer player, Direction facing) {
        double dx = player.getX() - worldPosition.getX();
        double dy = player.getY() - worldPosition.getY();
        double dz = player.getZ() - worldPosition.getZ();
        if (dy < -DOWN || dy > UP) {
            return false;
        }
        double forward;
        double side;
        switch (facing) {
            case SOUTH -> { forward = dz; side = dx; }
            case WEST -> { forward = -dx; side = dz; }
            case EAST -> { forward = dx; side = -dz; }
            default -> { forward = -dz; side = dx; }
        }
        if (forward < -BACK || forward > FORWARD) {
            return false;
        }
        return Math.abs(side) <= SIDE;
    }

    public Map<UUID, ReturnPos> getReturnPositions() {
        return returnPositions;
    }

    public static class ReturnPos {
        public final ResourceLocation dimension;
        public final double x, y, z;
        public final float yRot, xRot;

        public ReturnPos(ResourceLocation dimension, double x, double y, double z, float yRot, float xRot) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "gate1", 0, this::idleAnim));
        registrar.add(new AnimationController<>(this, "gate2", 0, this::idleAnim));
        registrar.add(new AnimationController<>(this, "gate3", 0, this::idleAnim));
        registrar.add(new AnimationController<>(this, "gate4", 0, this::idleAnim));
    }

    private <E extends GeoBlockEntity> PlayState idleAnim(AnimationState<E> state) {
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        return level == null ? 0 : level.getGameTime();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TargetDimension")) {
            targetDimension = ResourceLocation.tryParse(tag.getString("TargetDimension"));
        }
        itemPlaced = tag.getBoolean("ItemPlaced");
        returnGate = tag.getBoolean("ReturnGate");
        if (returnGate) {
            returnX = tag.getDouble("ReturnX");
            returnY = tag.getDouble("ReturnY");
            returnZ = tag.getDouble("ReturnZ");
            returnYRot = tag.getFloat("ReturnYRot");
            returnXRot = tag.getFloat("ReturnXRot");
            returnTicks = tag.getInt("ReturnTicks");
        }
        returnPositions.clear();
        if (tag.contains("ReturnPositions")) {
            ListTag list = tag.getList("ReturnPositions", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag c = (CompoundTag) t;
                UUID uuid = UUID.fromString(c.getString("Uuid"));
                ReturnPos rp = new ReturnPos(
                        ResourceLocation.tryParse(c.getString("Dim")),
                        c.getDouble("X"), c.getDouble("Y"), c.getDouble("Z"),
                        c.getFloat("YRot"), c.getFloat("XRot"));
                returnPositions.put(uuid, rp);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (targetDimension != null) {
            tag.putString("TargetDimension", targetDimension.toString());
        }
        tag.putBoolean("ItemPlaced", itemPlaced);
        tag.putBoolean("ReturnGate", returnGate);
        if (returnGate) {
            tag.putDouble("ReturnX", returnX);
            tag.putDouble("ReturnY", returnY);
            tag.putDouble("ReturnZ", returnZ);
            tag.putFloat("ReturnYRot", returnYRot);
            tag.putFloat("ReturnXRot", returnXRot);
            tag.putInt("ReturnTicks", returnTicks);
        }
        ListTag list = new ListTag();
        for (Map.Entry<UUID, ReturnPos> e : returnPositions.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putString("Uuid", e.getKey().toString());
            ReturnPos rp = e.getValue();
            c.putString("Dim", rp.dimension.toString());
            c.putDouble("X", rp.x);
            c.putDouble("Y", rp.y);
            c.putDouble("Z", rp.z);
            c.putFloat("YRot", rp.yRot);
            c.putFloat("XRot", rp.xRot);
            list.add(c);
        }
        tag.put("ReturnPositions", list);
    }
}
