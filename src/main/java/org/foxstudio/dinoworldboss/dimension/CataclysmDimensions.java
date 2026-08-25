package org.foxstudio.dinoworldboss.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CataclysmDimensions {

    public static final String CD_MOD = "cataclysm_dimension";

    public static final ResourceKey<Level> FORGE_OF_AEONS = key("cataclysm_forge_of_aeons");
    public static final ResourceKey<Level> ABYSSAL_DEPTHS = key("cataclysm_abyssal_depths");
    public static final ResourceKey<Level> PHARAOHS_BANE = key("cataclysm_pharaohs_bane");
    public static final ResourceKey<Level> ETERNAL_FROSTHOLD = key("cataclysm_eternal_frosthold");
    public static final ResourceKey<Level> SANCTUM_FALLEN = key("cataclysm_sanctum_fallen");
    public static final ResourceKey<Level> SOULS_ANVIL = key("cataclysm_souls_anvil");
    public static final ResourceKey<Level> INFERNOS_MAW = key("cataclysm_infernos_maw");
    public static final ResourceKey<Level> BASTION_LOST = key("cataclysm_bastion_lost");

    public static final List<ResourceKey<Level>> ALL = List.of(
            FORGE_OF_AEONS, ABYSSAL_DEPTHS, PHARAOHS_BANE, ETERNAL_FROSTHOLD,
            SANCTUM_FALLEN, SOULS_ANVIL, INFERNOS_MAW, BASTION_LOST);

    public static final List<String> NAMES = new ArrayList<>();

    static {
        for (ResourceKey<Level> k : ALL) {
            NAMES.add(k.location().getPath().replace("cataclysm_", ""));
        }
    }

    private static ResourceKey<Level> key(String path) {
        return ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(CD_MOD, path));
    }

    public static ResourceKey<Level> fromName(String name) {
        if (name == null) {
            return null;
        }
        String n = name.toLowerCase(Locale.ROOT);
        for (int i = 0; i < NAMES.size(); i++) {
            if (NAMES.get(i).equals(n)) {
                return ALL.get(i);
            }
        }
        // allow full path "cataclysm_xxx" or "cataclysm_dimension:cataclysm_xxx"
        for (ResourceKey<Level> k : ALL) {
            if (k.location().toString().equals(name) || k.location().getPath().equals(name)) {
                return k;
            }
        }
        return null;
    }

    public static String nameOf(ResourceKey<Level> key) {
        if (key == null) {
            return "unknown";
        }
        return key.location().getPath().replace("cataclysm_", "");
    }

    public static String bossNameOf(ResourceKey<Level> key) {
        if (key == null) {
            return "unknown";
        }
        return switch (nameOf(key)) {
            case "forge_of_aeons" -> "The Ancient Factory";
            case "abyssal_depths" -> "Leviathan";
            case "pharaohs_bane" -> "Cursed Pyramid Guardian";
            case "eternal_frosthold" -> "Frosted Prison Warden";
            case "sanctum_fallen" -> "Scylla";
            case "souls_anvil" -> "Ancient Remnant";
            case "infernos_maw" -> "Ignis";
            case "bastion_lost" -> "The Harbinger";
            default -> nameOf(key);
        };
    }
}
