package org.foxstudio.dinoworldboss;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DinoWorldBoss.MODID);

    public static final RegistryObject<CreativeModeTab> DINO_WORLD_BOSS_TAB = TABS.register(
            "dinoworldboss",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dinoworldboss"))
                    .icon(() -> new ItemStack(ModBlocks.DINO_WORLD_BOSS_GATE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.DINO_WORLD_BOSS_GATE_ITEM.get());
                    })
                    .build());

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        TABS.register(bus);
    }
}
