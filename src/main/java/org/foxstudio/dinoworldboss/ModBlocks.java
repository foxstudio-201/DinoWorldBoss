package org.foxstudio.dinoworldboss;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.foxstudio.dinoworldboss.block.DinoWorldBossGateBlock;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DinoWorldBoss.MODID);

    public static final RegistryObject<Block> DINO_WORLD_BOSS_GATE = BLOCKS.register(
            "dinoworldboss_gate",
            () -> new DinoWorldBossGateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(2.0F, 6.0F)
                    .noOcclusion()
                    .lightLevel(state -> 12)));

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, DinoWorldBoss.MODID);

    public static final RegistryObject<Item> DINO_WORLD_BOSS_GATE_ITEM = ITEMS.register(
            "dinoworldboss_gate",
            () -> new BlockItem(DINO_WORLD_BOSS_GATE.get(), new Item.Properties()));

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
