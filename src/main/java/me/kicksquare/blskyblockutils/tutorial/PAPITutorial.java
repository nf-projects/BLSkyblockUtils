package me.kicksquare.blskyblockutils.tutorial;

import org.bukkit.entity.Player;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class PAPITutorial {
    public static String parseTutorialPlaceholder(Player player, String params) {
        if (params.equalsIgnoreCase("tutorial_status1")) {
            if (player.getWorld().getName().equals("spawn")) {
                return "";
            } else {
                return colorize("&e&n/spawn&7 to continue tutorial!");
            }
        }

        return null;
    }
}
