package org.foxstudio.dinoworldboss.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.foxstudio.dinoworldboss.DinoWorldBoss;
import org.foxstudio.dinoworldboss.ModRegistry;

@Mod.EventBusSubscriber(modid = DinoWorldBoss.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.GATE_BE, context -> new DinoWorldBossGateRenderer());
    }
}
