package org.foxstudio.dinoworldboss.config;

public final class DinoMessageFormatter {

    private DinoMessageFormatter() {
    }

    public static String format(String template, String boss, String killer,
                                String x, String y, String z) {
        if (template == null) {
            return "";
        }
        return template
                .replace("{boss}", boss == null ? "" : boss)
                .replace("{killer}", killer == null ? "" : killer)
                .replace("{x}", x == null ? "" : x)
                .replace("{y}", y == null ? "" : y)
                .replace("{z}", z == null ? "" : z);
    }
}
