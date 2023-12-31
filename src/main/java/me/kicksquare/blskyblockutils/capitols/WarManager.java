package me.kicksquare.blskyblockutils.capitols;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;

public class WarManager {
    public static War tickWar(War war, BLSkyblockUtils plugin) {
        if (war == null) {
            return null;
        }

        List<Player> playersInWarzone = war.getOnlinePlayersInWar(true);
        for (Player p : playersInWarzone) {
            // give players in the warzone a glowing effect
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 2, 10));
        }

        // handle beacon/king of the hill logic
        war = handleBeaconLogic(war, playersInWarzone);

        // max duration countdown broadcasts
        long timeElapsed = System.currentTimeMillis() - war.startTime;
        long timeRemaining = (war.maxDurationHours * 60 * 60 * 1000) - timeElapsed;
        if (timeRemaining <= 0) {
            // war is over
            war.broadcastToAllParticipants("&c&lThe war has ended! &7The beacon was controlled by " + (war.nationBlueCurrentPoints > war.nationRedCurrentPoints ? war.nationBlue.getName() : war.nationRed.getName()) + "!");
            war.broadcastToAllParticipants("&9&lBLUE TEAM: &7" + war.nationBlueCurrentPoints + " points");
            war.broadcastToAllParticipants("&c&lRED TEAM: &7" + war.nationRedCurrentPoints + " points");
            war.broadcastToAllParticipants("&a&lWINNER: &7" + (war.nationBlueCurrentPoints > war.nationRedCurrentPoints ? war.nationBlue.getName() : war.nationRed.getName()));

            return null;
        } else if (timeRemaining <= 20 * 60 * 1000 && !war.oneHourWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 20 minutes!");
            war.oneHourWarningSent = true;
        } else if (timeRemaining <= 30 * 60 * 1000 && !war.thirtyMinuteWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 13 minutes!");
            war.tenMinuteWarningSent = true;
        } else if (timeRemaining <= 5 * 60 * 1000 && !war.fiveMinuteWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 5 minutes!");
            war.fiveMinuteWarningSent = true;
        } else if (timeRemaining <= 1 * 60 * 1000 && !war.oneMinuteWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 1 minute!");
            war.oneMinuteWarningSent = true;
        }

        return war;
    }

    private static War handleBeaconLogic(War war, List<Player> playersInWarzone) {
        int nationBluePlayersInBeaconRegion = 0;
        int nationRedPlayersInBeaconRegion = 0;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("world")));
        ProtectedRegion beaconRegion = regions.getRegion(war.capitol.getBeaconRegionName());
        LandsIntegration api = LandsIntegration.of(BLSkyblockUtils.getPlugin());

        for (Player p : playersInWarzone) {
            // check if this player is in the beacon region

            Location playerLoc = p.getLocation();

            // Convert Bukkit location to WorldGuard location
            com.sk89q.worldedit.util.Location wgPlayerLoc = BukkitAdapter.adapt(playerLoc);

            // Check if the player is in the region
            if (beaconRegion.contains(wgPlayerLoc.getBlockX(), wgPlayerLoc.getBlockY(), wgPlayerLoc.getBlockZ())) {
                // check if the player is in nation A or B
                LandPlayer landPlayer = api.getLandPlayer(p.getUniqueId());

                Collection<? extends Land> lands = landPlayer.getLands();
                if (!lands.isEmpty()) {
                    for (Land land : lands) {

                        // Check if the land is part of a nation
                        Nation nation = land.getNation();
                        if (nation != null) {
                            // Check if the nation is nation A or B
                            if (nation.getName().equals(war.nationBlue.getName())) {
                                nationBluePlayersInBeaconRegion++;
                            } else if (nation.getName().equals(war.nationRed.getName())) {
                                nationRedPlayersInBeaconRegion++;
                            }
                        }
                    }
                }
            }
        }

        BeaconStatus currentStatus = war.beaconStatus;

        if (nationBluePlayersInBeaconRegion == nationRedPlayersInBeaconRegion) {
            // same amount of players - no change

        } else if (nationBluePlayersInBeaconRegion > nationRedPlayersInBeaconRegion) {
            // more blue than red
            if (currentStatus != BeaconStatus.BLUE) {
                // change to blue
                war.beaconStatus = BeaconStatus.BLUE;

                war.broadcastToAllParticipants("&e&lBEACON CAPTURED! &7The beacon has been captured by &b" + war.nationBlue.getName() + "&7!");

                // replace all red wool & white wool with blue wool
                replaceBlockInRegion(beaconRegion, Material.RED_WOOL, Material.WHITE_WOOL, Material.BLUE_WOOL);

                // update the beacon color - replace white stained glass or red stained glass with blue stained glass
                replaceBlockInRegion(beaconRegion, Material.WHITE_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.BLUE_STAINED_GLASS);
            }

        } else if (nationBluePlayersInBeaconRegion < nationRedPlayersInBeaconRegion) {
            // more red than blue
            if (currentStatus != BeaconStatus.RED) {
                // change to red
                war.beaconStatus = BeaconStatus.RED;

                war.broadcastToAllParticipants("&e&lBEACON CAPTURED! &7The beacon has been captured by &c" + war.nationRed.getName() + "&7!");

                // replace all blue wool & white wool with red wool
                replaceBlockInRegion(beaconRegion, Material.BLUE_WOOL, Material.WHITE_WOOL, Material.RED_WOOL);

                // update the beacon color - replace white stained glass or blue stained glass with red stained glass
                replaceBlockInRegion(beaconRegion, Material.WHITE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.RED_STAINED_GLASS);
            }
        }

        // give out 0.01 points (per second) to the nation that owns the beacon
        switch (war.beaconStatus) {
            case BLUE:
                war.addPointsToBlue(0.01);
                break;
            case RED:
                war.addPointsToRed(0.01);
                break;
            case NEUTRAL:
                break;
        }

        return war;
    }

    private static void replaceBlockInRegion(ProtectedRegion region, Material replace1, Material replace2, Material with) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    Block block = Bukkit.getWorld("world").getBlockAt(x, y, z);
                    if (block.getType() == replace1 || block.getType() == replace2) {
                        block.setType(with);
                    }
                }
            }
        }
    }

    // todo event listener: kills, deaths, etc.
}
