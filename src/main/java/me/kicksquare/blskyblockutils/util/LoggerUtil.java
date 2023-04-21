package me.kicksquare.blskyblockutils.util;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;

public class LoggerUtil {

    private static final BLSkyblockUtils plugin = BLSkyblockUtils.getPlugin();

    public static void warning(String message) {
        plugin.getLogger().warning(message);
    }

    public static void info(String message) {
        plugin.getLogger().info(message);
    }

    public static void debug(String message) {
        if (plugin.getMainConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public static void severe(String message) {
        plugin.getLogger().severe(message);
    }

}
