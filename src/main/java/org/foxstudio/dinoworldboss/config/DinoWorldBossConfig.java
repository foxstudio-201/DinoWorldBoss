package org.foxstudio.dinoworldboss.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DinoWorldBossConfig {

    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.BooleanValue SPAWN_ENABLED;
    public static final ForgeConfigSpec.IntValue SPAWN_INTERVAL_MINUTES;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOG;
    public static final ForgeConfigSpec.ConfigValue<String> SPAWN_MESSAGE;
    public static final ForgeConfigSpec.ConfigValue<String> BOSS_KILLED_MESSAGE;
    public static final ForgeConfigSpec.ConfigValue<String> RETURN_GATE_MESSAGE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("general");
        SPAWN_ENABLED = b
                .comment("Tự động spawn gate world boss định kỳ.")
                .define("spawn.enabled", true);
        SPAWN_INTERVAL_MINUTES = b
                .comment("Số ngày trong game giữa mỗi lần spawn gate tự động.",
                        "1 ngày = 24000 ticks = 20 phút real time.")
                .defineInRange("spawn.interval_days", 730, 1, 100000);
        DEBUG_LOG = b
                .comment("Bật log debug [dinoworldboss] để kiểm tra lỗi đặt structure.")
                .define("debug_log", false);
        b.pop();

        b.push("messages");
        SPAWN_MESSAGE = b
                .comment("Thông báo khi gate world boss spawn.",
                        "Placeholder: {boss} {x} {y} {z}")
                .define("spawn_message", "§5§l[Sytem Dino Boss] §fBoss thế giới §e{boss}§f vừa xuất hiện, cổng đã mở tại §a{x} {y} {z}");
        BOSS_KILLED_MESSAGE = b
                .comment("Thông báo khi boss bị tiêu diệt (broadcast toàn server).",
                        "Placeholder: {boss} {killer}")
                .define("boss_killed_message", "§5§l[Sytem Dino Boss] §fBoss §e{boss}§f đã bị kết liễu bởi §c{killer}§f, cổng đã đóng lại!");
        RETURN_GATE_MESSAGE = b
                .comment("Thông báo khi gate về xuất hiện gần người giết boss.",
                        "Placeholder: {boss}")
                .define("return_gate_message", "§5§l[Sytem Dino Boss] §fBoss {boss} đã chết, cổng về đã xuất hiện gần bạn!");
        b.pop();

        SERVER_SPEC = b.build();
    }

    private DinoWorldBossConfig() {
    }
}
