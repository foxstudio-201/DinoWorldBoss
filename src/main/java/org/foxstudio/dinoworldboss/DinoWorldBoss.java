package org.foxstudio.dinoworldboss;

import net.minecraftforge.fml.common.Mod;

@Mod(DinoWorldBoss.MODID)
public class DinoWorldBoss {

    public static final String MODID = "dinoworldboss";

    public DinoWorldBoss() {
        ModBlocks.register();
        ModCreativeTab.register();
        ModBlockEntities.register();
        org.foxstudio.dinoworldboss.command.DinoWorldBossCommands.register();
    }
}