package org.foxstudio.dinoworldboss;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.foxstudio.dinoworldboss.blockentity.DinoWorldBossGateBlockEntity;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DinoWorldBoss.MODID);

    public static final RegistryObject<BlockEntityType<DinoWorldBossGateBlockEntity>> GATE =
            BLOCK_ENTITIES.register("dinoworldboss_gate",
                    () -> BlockEntityType.Builder.of(DinoWorldBossGateBlockEntity::new,
                            ModBlocks.DINO_WORLD_BOSS_GATE.get()).build(null));

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCK_ENTITIES.register(bus);
    }
}
