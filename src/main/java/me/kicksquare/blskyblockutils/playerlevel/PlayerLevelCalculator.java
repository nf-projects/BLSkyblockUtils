package me.kicksquare.blskyblockutils.playerlevel;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlayerLevelCalculator {
    public static String calculatePlayerLevel(Player player) {
        int level = 1;

        String combinedSkillLevel = PlaceholderAPI.setPlaceholders(player, "%aureliumskills_power%");

        // skill levels, playtime, mobs killed, quests completed


        return String.valueOf(level);
    }
}
