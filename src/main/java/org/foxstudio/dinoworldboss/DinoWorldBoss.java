package org.foxstudio.dinoworldboss;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.foxstudio.dinoworldboss.config.DinoWorldBossConfig;

@Mod(DinoWorldBoss.MODID)
public class DinoWorldBoss {

    public static final String MODID = "dinoworldboss";

    public DinoWorldBoss() {
        ModRegistry.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, DinoWorldBossConfig.SERVER_SPEC);
    }
}