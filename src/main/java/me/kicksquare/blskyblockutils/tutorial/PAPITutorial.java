package me.kicksquare.blskyblockutils.tutorial;

import me.angeschossen.lands.api.LandsIntegration;
import me.clip.placeholderapi.PlaceholderAPI;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.entity.Player;

import java.util.Objects;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class PAPITutorial {
    public static String parseTutorialPlaceholder(Player player, String params, BLSkyblockUtils plugin) {
        if (params.equalsIgnoreCase("tutorial_status1")) {
            // if the user is in the right world for the current quest, we return nothing
            // if the user is in the wrong world, we return `&e&n/[worldcommand]&7 to continue tutorial!` where [worldcommand] is the command to get to the right world
            String currentWorldName = player.getWorld().getName();
            String currentQuestName = getPlayerCurrentQuest(player); //tutorial1, tutorial2, etc

            String questNumber = currentQuestName.replace("tutorial", "");

            String questWorld = "spawn";
            for (String world : plugin.getMainConfig().getStringList("quest-worlds")) {
                String[] split = world.split(": ");
                if (split[0].equals(questNumber)) {
                    questWorld = split[1];
                    break;
                }
            }

            if (questWorld.equalsIgnoreCase("ANY")) {
                return "";
            }

            if (!currentWorldName.equalsIgnoreCase(questWorld)) {
        
                String command = null;
                for (String worldCommand : plugin.getMainConfig().getStringList("world-commands")) {
                    String[] split = worldCommand.split(": ");
                    if (split[0].equalsIgnoreCase(questWorld)) {
                        command = split[1];
                        break;
                    }
                }

                if (Objects.equals(command, "/rtp")) {
                    if (LandsIntegration.of(plugin).getLandPlayer(player.getUniqueId()).ownsLand()) {
                        command = "/land spawn";
                    }
                }

                if (command == null) {
                    throw new RuntimeException("Could not find world command for world " + questWorld);
                }

                return colorize("&e&n" + command + "&7 to continue tutorial!");
            } else {
                return "";
            }
        }

        return null;
    }

    private static String getPlayerCurrentQuest(Player p) {
        // %quests_started_listid% returns a string list if there's more than 1 (this should never happen)
        String result = PlaceholderAPI.setPlaceholders(p, "%quests_started_listid%");
        if (result.contains(",")) {
            throw new RuntimeException("Player has more than 1 quest started!");
        }

        return result;
    }
}
