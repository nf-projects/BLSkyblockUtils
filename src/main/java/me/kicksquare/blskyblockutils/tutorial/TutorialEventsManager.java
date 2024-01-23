package me.kicksquare.blskyblockutils.tutorial;

import com.leonardobishop.quests.bukkit.api.event.PlayerFinishQuestEvent;
import com.leonardobishop.quests.bukkit.api.event.PlayerStartQuestEvent;
import com.live.bemmamin.gps.logic.Point;
import me.clip.placeholderapi.PlaceholderAPI;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class TutorialEventsManager implements Listener {
    private BLSkyblockUtils plugin;

    public TutorialEventsManager(BLSkyblockUtils plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                handleQuestIntegration(p, false);
            }
        }, 0, 20); // every second
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().hasPlayedBefore()) {
            e.getPlayer().performCommand("quests:q start tutorial1");
        }
    }

    @EventHandler
    public void onQuestComplete(PlayerFinishQuestEvent e) {
        handleQuestIntegration(e.getPlayer(), true);
    }

    @EventHandler
    public void onQuestStart(PlayerStartQuestEvent e) {
        handleQuestIntegration(e.getPlayer(), true);
    }

    private void handleQuestIntegration(Player p, boolean overrideCurrentGPS) {
        if (!p.getWorld().getName().equals("spawn")) {
            return;
        }

        String currentQuest = getPlayerCurrentQuest(p); // point name = quest name

        // if the player is more than 10 blocks away from the GPS point, start a gps
        // first, get the GPS point
        List<Point> allPoints = plugin.getGpsAPI().getAllPoints();
        // find the point with the same name as the quest
        Point point = null;
        for (Point pnt : allPoints) {
            if (pnt.getName().equals(currentQuest)) {
                point = pnt;
                break;
            }
        }

        if (point == null) {
            return;
        }

        Location pointLocation = point.getLocation();
        Location playerLocation = p.getLocation();

        if (pointLocation.distance(playerLocation) < 10) {
            return;
        }

        if (plugin.getGpsAPI().gpsIsActive(p) && !overrideCurrentGPS) {
            // already active
            return;
        }

        plugin.getGpsAPI().startGPS(p, currentQuest);
    }

    private String getPlayerCurrentQuest(Player p) {
        // %quests_started_listid% returns a string list if there's more than 1 (this should never happen)
        String result = PlaceholderAPI.setPlaceholders(p, "%quests_started_listid%");
        if (result.contains(",")) {
            throw new RuntimeException("Player has more than 1 quest started!");
        }

        return result;
    }
}
