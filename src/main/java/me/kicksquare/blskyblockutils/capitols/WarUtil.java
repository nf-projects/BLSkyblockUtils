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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;

public class WarUtil {
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
        long timeRemaining = ((long) war.maxDurationHours * 60 * 60 * 1000) - timeElapsed;
        if (timeRemaining <= 0) {
            endWar();

            return null;
        } else if (timeRemaining <= 20 * 60 * 1000 && !war.oneHourWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 20 minutes!");
            war.oneHourWarningSent = true;
        } else if (timeRemaining <= 30 * 60 * 1000 && !war.thirtyMinuteWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 30 minutes!");
            war.thirtyMinuteWarningSent = true;
        } else if (timeRemaining <= 5 * 60 * 1000 && !war.fiveMinuteWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 5 minutes!");
            war.fiveMinuteWarningSent = true;
        } else if (timeRemaining <= 60 * 1000 && !war.oneMinuteWarningSent) {
            war.broadcastToAllParticipants("&c&lThe war will end in 1 minute!");
            war.oneMinuteWarningSent = true;
        }

        return war;
    }

    public static void endWar() {
        War war = BLSkyblockUtils.getPlugin().getCurrentWar();

        war.broadcastToAllParticipants("&c&lThe war has ended! &7The beacon was controlled by " + (war.nationBlueCurrentPoints > war.nationRedCurrentPoints ? war.nationBlue.getName() : war.nationRed.getName()) + "!");
        war.broadcastToAllParticipants("&9&lBLUE TEAM POINTS: &7" + war.nationBlueCurrentPoints + " points");
        war.broadcastToAllParticipants("&c&lRED TEAM POINTS: &7" + war.nationRedCurrentPoints + " points");
        war.broadcastToAllParticipants("&a&lWINNER: &7" + (war.nationBlueCurrentPoints > war.nationRedCurrentPoints ? war.nationBlue.getName() : war.nationRed.getName()));

        // replace all red/blue blocks with white
        World world = Bukkit.getWorld("world");
        // go through every beacon region
        for (int beaconNumber = 1; beaconNumber <= 4; beaconNumber++) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(world));
            ProtectedRegion beaconRegion = regions.getRegion(war.capitol.getBeaconRegionName(beaconNumber));

            replaceBlueAndRedWithWhite(beaconRegion);
        }

        BLSkyblockUtils.getPlugin().endCurrentWar();
    }

    private static War handleBeaconLogic(War war, List<Player> playersInWarzone) {
        // there are 4 beacons in total
        // iterate through all 4
        for (int beaconNumber = 1; beaconNumber <= 4; beaconNumber++) {
            int nationBluePlayersInBeaconRegion = 0;
            int nationRedPlayersInBeaconRegion = 0;

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("world")));
            ProtectedRegion beaconRegion = regions.getRegion(war.capitol.getBeaconRegionName(beaconNumber));
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

            BeaconStatus currentStatus = war.getBeaconStatus(beaconNumber);

            if (nationBluePlayersInBeaconRegion == nationRedPlayersInBeaconRegion) {
                // same amount of players - no change

            } else if (nationBluePlayersInBeaconRegion > nationRedPlayersInBeaconRegion) {
                // more blue than red
                if (currentStatus != BeaconStatus.BLUE) {
                    // change to blue
                    war.setBeaconStatus(beaconNumber, BeaconStatus.BLUE);

                    war.broadcastToAllParticipants("&e&lBEACON " + beaconNumber +
                            " CAPTURED! &7The beacon has been captured by &b" + war.nationBlue.getName() + "&7!");

                    replaceRedAndWhiteWithBlue(beaconRegion);
                }

            } else if (nationBluePlayersInBeaconRegion < nationRedPlayersInBeaconRegion) {
                // more red than blue
                if (currentStatus != BeaconStatus.RED) {
                    // change to red
                    war.setBeaconStatus(beaconNumber, BeaconStatus.RED);

                    war.broadcastToAllParticipants("&e&lBEACON " + beaconNumber +
                            " CAPTURED! &7The beacon has been captured by &c" + war.nationRed.getName() + "&7!");

                    replaceBlueAndWhiteWithRed(beaconRegion);
                }
            }

            // give out 1 points (per second) to the nation that owns the beacon
            switch (war.getBeaconStatus(beaconNumber)) {
                case BLUE:
                    war.addPointsToBlue(1);
                    break;
                case RED:
                    war.addPointsToRed(1);
                    break;
                case NEUTRAL:
                    break;
            }
        }

        return war;
    }

    private static void replaceBlockInRegion(ProtectedRegion region, Material replace1, Material replace2, Material with) {
        World world = Bukkit.getWorld("world");
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == replace1 || block.getType() == replace2) {
                        // All of this additional code is to preserve the block's direction (e.g. glazed terracotta)

                        BlockData blockData = block.getBlockData();
                        if (blockData instanceof Directional) {
                            // Set the new block type
                            block.setType(with, false); // False to not apply physics immediately
                            // Cast the block data to Directional and apply the saved direction
                            Directional directional = (Directional) block.getBlockData();
                            directional.setFacing(((Directional) blockData).getFacing());
                            // Set the block data with the new direction
                            block.setBlockData(directional, true); // True to apply physics after the change
                        } else {
                            // If it's not a directional block (like wool), just set the type
                            block.setType(with);
                        }
                    }
                }
            }
        }
    }

    private static void replaceRedAndWhiteWithBlue(ProtectedRegion beaconRegion) {
        // replace all red wool & white wool with blue wool
        replaceBlockInRegion(beaconRegion, Material.RED_WOOL, Material.WHITE_WOOL, Material.BLUE_WOOL);

        // update the beacon color - replace white stained glass or red stained glass with blue stained glass
        replaceBlockInRegion(beaconRegion, Material.WHITE_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.BLUE_STAINED_GLASS);

        // replace white/red concrete with blue concrete
        replaceBlockInRegion(beaconRegion, Material.WHITE_CONCRETE, Material.RED_CONCRETE, Material.BLUE_CONCRETE);

        // replace white/red glazed terracotta with blue glazed terracotta
        replaceBlockInRegion(beaconRegion, Material.WHITE_GLAZED_TERRACOTTA, Material.RED_GLAZED_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA);
    }

    private static void replaceBlueAndWhiteWithRed(ProtectedRegion beaconRegion) {
        // replace all blue wool & white wool with red wool
        replaceBlockInRegion(beaconRegion, Material.BLUE_WOOL, Material.WHITE_WOOL, Material.RED_WOOL);

        // update the beacon color - replace white stained glass or blue stained glass with red stained glass
        replaceBlockInRegion(beaconRegion, Material.WHITE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.RED_STAINED_GLASS);

        // replace white/blue concrete with red concrete
        replaceBlockInRegion(beaconRegion, Material.WHITE_CONCRETE, Material.BLUE_CONCRETE, Material.RED_CONCRETE);

        // replace white/blue glazed terracotta with red glazed terracotta
        replaceBlockInRegion(beaconRegion, Material.WHITE_GLAZED_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA, Material.RED_GLAZED_TERRACOTTA);
    }

    public static void replaceBlueAndRedWithWhite(ProtectedRegion beaconRegion) {
        // replace all blue wool & red wool with white wool
        replaceBlockInRegion(beaconRegion, Material.BLUE_WOOL, Material.RED_WOOL, Material.WHITE_WOOL);

        // update the beacon color - replace blue stained glass or red stained glass with white stained glass
        replaceBlockInRegion(beaconRegion, Material.BLUE_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.WHITE_STAINED_GLASS);

        // replace blue/red concrete with white concrete
        replaceBlockInRegion(beaconRegion, Material.BLUE_CONCRETE, Material.RED_CONCRETE, Material.WHITE_CONCRETE);

        // replace blue/red glazed terracotta with white glazed terracotta
        replaceBlockInRegion(beaconRegion, Material.BLUE_GLAZED_TERRACOTTA, Material.RED_GLAZED_TERRACOTTA, Material.WHITE_GLAZED_TERRACOTTA);
    }
}
