package me.kicksquare.blskyblockutils.capitols;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.util.TimeFormatterUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderCalculator {
    public static String parseWarPlaceholder(Player player, String params) {
        // %blskyblockutils_war_[status|beaconcontrol_[capitol]_[beacon num]|nation_blue|nation_red|beacon[1-4]_status|all_beacons|blue_points|red_points|blue_kills|red_kills|time_elapsed|time_remaining|point_goal]%
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

            case "war_beacon1_status":
                return war.beaconOneStatus.toString();

            case "war_beacon2_status":
                return war.beaconTwoStatus.toString();

            case "war_beacon3_status":
                return war.beaconThreeStatus.toString();

            case "war_beacon4_status":
                return war.beaconFourStatus.toString();

            case "war_all_beacons":
                // returns it like this: 1 2 3 4
                // but each number is colored based on the beacon status
                // for example, if red controls 2 and 3, but blue controls 1 and 4, it returns
                // &9&l1 &C&l2 3 &9&l4
                // &f&l is neutral, &9&l is blue, &c&l is red

                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= 4; i++) {
                    BeaconStatus status = war.getBeaconStatus(i);
                    sb.append(status.equals(BeaconStatus.NEUTRAL) ? "&f&l" : status.equals(BeaconStatus.BLUE) ? "&9&l" : "&c&l");
                    sb.append(i);
                    sb.append(" ");
                }

                return sb.toString().trim();

            case "war_blue_points":
                return war.getNationBlueCurrentPoints() + "";

            case "war_red_points":
                return war.getNationRedCurrentPoints() + "";

            case "war_blue_kills":
                return war.nationBlueKills + "";

            case "war_red_kills":
                return war.nationRedKills + "";

            case "war_time_elapsed":
                return TimeFormatterUtil.formatSeconds(war.getSecondsElapsed());

            case "war_time_remaining":
                return TimeFormatterUtil.formatSeconds(war.getSecondsRemaining());

            case "war_point_goal":
                return war.pointsGoal + "";
            default:
                // for beaconcontrol_[capitol]_[beacon num]
                if (params.startsWith("war_beaconcontrol_")) {
                    String[] split = params.split("_");
                    if (split.length != 4) {
                        Bukkit.getLogger().warning("Invalid placeholder: " + params);
                        return null;
                    }

                    int beaconNum;
                    try {
                        beaconNum = Integer.parseInt(split[3]);
                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().warning("Invalid placeholder: " + params);
                        return null;
                    }

                    Capitol capitol = Capitol.valueOf(split[2].toUpperCase());
                    if (capitol == null) {
                        Bukkit.getLogger().warning("Invalid placeholder: " + params);
                        return null;
                    }

                    BeaconStatus status = war.getBeaconStatus(beaconNum);
                    return status.equals(BeaconStatus.NEUTRAL) ? "&f&lNEUTRAL" : status.equals(BeaconStatus.BLUE) ? "&9&l" + war.nationBlue.getName() : "&c&l" + war.nationRed.getName();
                }

                return null;
        }
    }
}
