package me.kicksquare.blskyblockutils.capitols;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.nation.Nation;
import me.angeschossen.lands.api.player.LandPlayer;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class War {
    public final Nation nationBlue;
    public final Nation nationRed;

    public final Capitol capitol;

    public double nationBlueCurrentPoints;
    public double nationRedCurrentPoints;

    public final int maxDurationHours;
    public final long startTime;

    public final int pointsGoal; // +0.01 points per second for team controlling the beacon, +0.25 points per kill, -0.25 points per death

    // status fields
    public BeaconStatus beaconStatus = BeaconStatus.NEUTRAL;

    public boolean oneHourWarningSent = false;
    public boolean thirtyMinuteWarningSent = false;
    public boolean fiveMinuteWarningSent = false;
    public boolean oneMinuteWarningSent = false;

    public War(Nation nationBlue, Nation nationRed, Capitol capitol, int maxDurationHours, int pointsGoal) {
        this.nationBlue = nationBlue;
        this.nationRed = nationRed;
        this.capitol = capitol;
        this.maxDurationHours = maxDurationHours;
        this.pointsGoal = pointsGoal;
        this.startTime = System.currentTimeMillis();
    }

    public void addPointsToBlue(double points) {
        nationBlueCurrentPoints += points;
    }

    public void addPointsToRed(double points) {
        nationRedCurrentPoints += points;
    }

    // returns all Bukkit Players who are 1) part of the war and 2) in the capitol region
    public List<Player> getOnlinePlayersInWar(boolean mustBeInRegion) {
        List<Player> players = new ArrayList<>();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        LandsIntegration api = LandsIntegration.of(BLSkyblockUtils.getPlugin());
        RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("world")));

        ProtectedRegion region = regions.getRegion(capitol.getRegionName());
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Location loc = player.getLocation();

            // Convert Bukkit location to WorldGuard location
            com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(loc);

            // Check if the player is in the region
            if (mustBeInRegion
                && !region.contains(wgLoc.getBlockX(), wgLoc.getBlockY(), wgLoc.getBlockZ())
            ) {
                continue;
            }

            // Check if the player is in a land or nation
            LandPlayer landPlayer = api.getLandPlayer(player.getUniqueId());

            Collection<? extends Land> lands = landPlayer.getLands();
            if (!lands.isEmpty()) {
                for (Land land : lands) {

                    // Check if the land is part of a nation
                    Nation nation = land.getNation();
                    if (nation != null) {
                        if (nation.equals(nationBlue) || nation.equals(nationRed)) {
                            players.add(player);
                        }
                    }
                }
            }

        }

        return players;
    }

    public void broadcastToAllParticipants(String message) {
        for (Player player : getOnlinePlayersInWar(false)) {
            player.sendMessage(colorize(message));
        }
    }
}
