package org.foxstudio.dinoworldboss.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.foxstudio.dinoworldboss.DinoWorldBoss;
import org.foxstudio.dinoworldboss.dimension.CataclysmDimensions;
import org.foxstudio.dinoworldboss.scheduler.DinoWorldBossScheduler;
import org.foxstudio.dinoworldboss.structure.DinoWorldBossStructurePlacer;

@Mod.EventBusSubscriber(modid = DinoWorldBoss.MODID)
public final class DinoWorldBossCommands {

    private DinoWorldBossCommands() {
    }

    public static void register() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();

        d.register(Commands.literal("dinoworldboss")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(ctx -> {
                            DinoWorldBossScheduler.trySpawn(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal("Đã thử spawn cổng ngẫu nhiên"), true);
                            return 1;
                        })
                        .then(Commands.argument("dimension", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "dimension");
                                    ResourceKey<Level> dim = CataclysmDimensions.fromName(name);
                                    if (dim == null) {
                                        ctx.getSource().sendFailure(Component.literal("Dimension không hợp lệ: " + name
                                                + ". Chọn: " + String.join(", ", CataclysmDimensions.NAMES)));
                                        return 0;
                                    }
                                    DinoWorldBossScheduler.spawnForDimension(ctx.getSource().getServer(), dim);
                                    return 1;
                                }))
                        .then(Commands.literal("at")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("dimension", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                                    String name = StringArgumentType.getString(ctx, "dimension");
                                                    ResourceKey<Level> dim = CataclysmDimensions.fromName(name);
                                                    if (dim == null) {
                                                        ctx.getSource().sendFailure(Component.literal("Dimension không hợp lệ: " + name));
                                                        return 0;
                                                    }
                                                    boolean placed = DinoWorldBossStructurePlacer.place(
                                                            ctx.getSource().getServer().overworld(), pos, dim.location());
                                                    ctx.getSource().sendSuccess(() -> Component.literal(placed
                                                            ? "Đã đặt cổng " + dim.location() : "Đặt cổng thất bại"), true);
                                                    return placed ? 1 : 0;
                                                })))))
                .then(Commands.literal("toggle")
                        .executes(ctx -> {
                            DinoWorldBossScheduler.setEnabled(false);
                            ctx.getSource().sendSuccess(() -> Component.literal("Đã tắt auto-spawn cổng"), true);
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("Dimension: "
                                    + String.join(", ", CataclysmDimensions.NAMES)), true);
                            return 1;
                        })));
    }
}
