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

    public double nationBlueCurrentPoints = 0;
    public double nationRedCurrentPoints = 0;

    public int nationBlueKills = 0;
    public int nationRedKills = 0;

    public final int maxDurationHours;
    public final long startTime;

    public final int pointsGoal; // +1 points per second for each controlled beacon

    // status fields
    public BeaconStatus beaconOneStatus = BeaconStatus.NEUTRAL;
    public BeaconStatus beaconTwoStatus = BeaconStatus.NEUTRAL;
    public BeaconStatus beaconThreeStatus = BeaconStatus.NEUTRAL;
    public BeaconStatus beaconFourStatus = BeaconStatus.NEUTRAL;

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

    public BeaconStatus getBeaconStatus(int beaconNumber) {
        switch (beaconNumber) {
            case 1:
                return beaconOneStatus;
            case 2:
                return beaconTwoStatus;
            case 3:
                return beaconThreeStatus;
            case 4:
                return beaconFourStatus;
            default:
                throw new IllegalArgumentException("Invalid beacon number: " + beaconNumber);
        }
    }

    public void setBeaconStatus(int beaconNumber, BeaconStatus status) {
        switch (beaconNumber) {
            case 1:
                beaconOneStatus = status;
                break;
            case 2:
                beaconTwoStatus = status;
                break;
            case 3:
                beaconThreeStatus = status;
                break;
            case 4:
                beaconFourStatus = status;
                break;
            default:
                throw new IllegalArgumentException("Invalid beacon number: " + beaconNumber);
        }
    }

    public void addPointsToBlue(double points) {
        nationBlueCurrentPoints += points;
    }

    public void addPointsToRed(double points) {
        nationRedCurrentPoints += points;
    }

    public void addKillToBlue() {
        nationBlueKills++;
        addPointsToBlue(50);
    }

    public void addKillToRed() {
        nationRedKills++;
        addPointsToRed(50);
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

    public boolean isAtWar(Player player) {
        LandsIntegration api = LandsIntegration.of(BLSkyblockUtils.getPlugin());
        LandPlayer landPlayer = api.getLandPlayer(player.getUniqueId());

        Collection<? extends Land> lands = landPlayer.getLands();
        if (!lands.isEmpty()) {
            for (Land land : lands) {

                // Check if the land is part of a nation
                Nation nation = land.getNation();
                if (nation != null) {
                    if (nation.equals(nationBlue) || nation.equals(nationRed)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void broadcastToAllParticipants(String message) {
        for (Player player : getOnlinePlayersInWar(false)) {
            player.sendMessage(colorize(message));
        }
    }

    // rounded to 3 decimal places
    public double getNationBlueCurrentPoints() {
        return Math.round(nationBlueCurrentPoints * 1000.0) / 1000.0;
    }

    // rounded to 3 decimal places
    public double getNationRedCurrentPoints() {
        return Math.round(nationRedCurrentPoints * 1000.0) / 1000.0;
    }

    public int getSecondsRemaining() {
        return (int) ((startTime + (maxDurationHours * 60 * 60 * 1000)) - System.currentTimeMillis()) / 1000;
    }

    public int getSecondsElapsed() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }
}
