package me.kicksquare.blskyblockutils.capitols;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.util.TimeFormatterUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderCalculator {
    public static String parseWarPlaceholder(Player player, String params) {
        // %blskyblockutils_war_[status|nation_blue|nation_red|beacon_status|blue_points|red_points|time_elapsed|time_remaining|point_goal]%
        // so params can be "war_status", "war_nation_blue", etc.

        // if there is no war, all of this is null automatically
        War war = BLSkyblockUtils.getPlugin().getCurrentWar();
        if (war == null) return "NO_WAR";

        if(!war.isAtWar(player) && (!player.hasPermission("blskyblockutils.managewars"))) return "NO_WAR";

        switch (params) {
            case "war_status":
                // returns "true" if the player is at war, "NO_WAR" if not

                return (war.isAtWar(player) || player.hasPermission("blskyblockutils.managewars")) ? "true" : "NO_WAR";

            case "war_nation_blue":
                return war.nationBlue.getName();

            case "war_nation_red":
                return war.nationRed.getName();

            case "war_beacon_status":
                return war.beaconStatus.toString();

            case "war_blue_points":
                return war.getNationBlueCurrentPoints() + "";

            case "war_red_points":
                return war.getNationRedCurrentPoints() + "";

            case "war_time_elapsed":
                return TimeFormatterUtil.formatSeconds(war.getSecondsElapsed());

            case "war_time_remaining":
                return TimeFormatterUtil.formatSeconds(war.getSecondsRemaining());

            case "war_point_goal":
                return war.pointsGoal + "";
            default:
                return null;
        }
    }
}
