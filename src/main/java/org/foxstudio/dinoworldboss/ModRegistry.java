package org.foxstudio.dinoworldboss;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.foxstudio.dinoworldboss.block.DinoWorldBossGateBlock;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;

public final class ModRegistry {

    public static Block DINO_WORLD_BOSS_GATE;
    public static Item DINO_WORLD_BOSS_GATE_ITEM;
    public static BlockEntityType<DinoWorldBossGateBlockEntity> GATE_BE;
    public static CreativeModeTab DINO_WORLD_BOSS_TAB;

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ModRegistry::onRegister);
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            DINO_WORLD_BOSS_GATE = new DinoWorldBossGateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)
                    .noOcclusion()
                    .lightLevel(state -> 12));
            event.register(Registries.BLOCK, helper ->
                    helper.register("dinoworldboss_gate", DINO_WORLD_BOSS_GATE));
        }

        if (event.getRegistryKey().equals(Registries.ITEM)) {
            DINO_WORLD_BOSS_GATE_ITEM = new BlockItem(ModRegistry.DINO_WORLD_BOSS_GATE, new Item.Properties());
            event.register(Registries.ITEM, helper ->
                    helper.register("dinoworldboss_gate", DINO_WORLD_BOSS_GATE_ITEM));
        }

        if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            GATE_BE = BlockEntityType.Builder.of(DinoWorldBossGateBlockEntity::new,
                    ModRegistry.DINO_WORLD_BOSS_GATE).build(null);
            event.register(Registries.BLOCK_ENTITY_TYPE, helper ->
                    helper.register("dinoworldboss_gate", GATE_BE));
        }

        if (event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
            DINO_WORLD_BOSS_TAB = CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dinoworldboss"))
                    .icon(() -> new ItemStack(ModRegistry.DINO_WORLD_BOSS_GATE_ITEM))
                    .displayItems((params, output) -> {
                        output.accept(ModRegistry.DINO_WORLD_BOSS_GATE_ITEM);
                    })
                    .build();
            event.register(Registries.CREATIVE_MODE_TAB, helper ->
                    helper.register("dinoworldboss", DINO_WORLD_BOSS_TAB));
        }
    }
}