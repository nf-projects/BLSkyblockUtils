package me.kicksquare.blskyblockutils;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.nation.Nation;
import me.kicksquare.blskyblockutils.capitols.Capitol;
import me.kicksquare.blskyblockutils.capitols.War;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class AdminCommands implements CommandExecutor {
    private final BLSkyblockUtils plugin;

    public AdminCommands(BLSkyblockUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player player = (Player) commandSender;
        if (!player.hasPermission("blskyblockutils.admin")) {
            player.sendMessage("You do not have permission to use this command!");
            return true;
        }

        // /blskyblockutils getActiveWar
        // /blskyblockutils endWar
        // /blskyblockutils startwar <nation blue> <nation red> <capitol region name> <maxDurationHours> <pointsGoal>

        if (args.length == 0) {
            player.sendMessage("Usage: ");
            player.sendMessage("/blskyblockutils getActiveWar");
            player.sendMessage("/blskyblockutils endWar");
            player.sendMessage("/blskyblockutils startwar <nation blue> <nation red> <capitol region name> <maxDurationHours> <pointsGoal>");
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("getActiveWar")) {
                War war = plugin.getCurrentWar();

                if (war == null) {
                    player.sendMessage("There is no active war!");
                    return true;
                }

                player.sendMessage(colorize("&Active war: " + war.nationBlue.getName() + " vs " + war.nationRed.getName()));
                player.sendMessage(colorize("&aBlue Points: " + war.nationBlueCurrentPoints));
                player.sendMessage(colorize("&aRed Points: " + war.nationRedCurrentPoints));
                player.sendMessage(colorize("&aFinal Beacon status: " + war.beaconStatus.toString()));
                player.sendMessage(colorize("&aDuration: " + (war.maxDurationHours - (System.currentTimeMillis() - war.startTime) / 1000 / 60 / 60) + " hours"));
                player.sendMessage(colorize("&aTime remaining: " + (war.maxDurationHours - (System.currentTimeMillis() - war.startTime) / 1000 / 60 / 60) + " hours"));
                return true;
            }

            if (args[0].equalsIgnoreCase("endWar")) {
                if (plugin.getCurrentWar() == null) {
                    player.sendMessage("There is no active war!");
                    return true;
                }

                War war = plugin.endCurrentWar();

                player.sendMessage(colorize("&aEnded war: " + war.nationBlue.getName() + " vs " + war.nationRed.getName()));
                player.sendMessage(colorize("&aBlue Points: " + war.nationBlueCurrentPoints));
                player.sendMessage(colorize("&aRed Points: " + war.nationRedCurrentPoints));
                player.sendMessage(colorize("&aFinal Beacon status: " + war.beaconStatus.toString()));
                player.sendMessage(colorize("&aDuration: " + (war.maxDurationHours - (System.currentTimeMillis() - war.startTime) / 1000 / 60 / 60) + " hours"));
                player.sendMessage(colorize("&aTime remaining: " + (war.maxDurationHours - (System.currentTimeMillis() - war.startTime) / 1000 / 60 / 60) + " hours"));
                return true;
            }
        }
        
        if (args.length == 6) {
            if (args[0].equalsIgnoreCase("startwar")) {
                String nationBlueName = args[1];
                String nationRedName = args[2];
                String capitolRegionName = args[3];
                int maxDurationHours = Integer.parseInt(args[4]);
                int pointsGoal = Integer.parseInt(args[5]);
                
                // try to parse the 2 nation names as Nation objects
                // if either of them are null, then the nation name was invalid
                LandsIntegration api = LandsIntegration.of(BLSkyblockUtils.getPlugin());
                Nation nationBlue = api.getNationByName(nationBlueName);
                Nation nationRed = api.getNationByName(nationRedName);

                if (nationBlue == null) {
                    player.sendMessage("Nation " + nationBlueName + " does not exist!");
                    return true;
                }

                if (nationRed == null) {
                    player.sendMessage("Nation " + nationRedName + " does not exist!");
                    return true;
                }

                // parse the Capitol enum from the capitol region name
                Capitol capitol;
                try {
                    capitol = Capitol.valueOf(capitolRegionName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Capitol " + capitolRegionName + " does not exist!");
                    return true;
                }

                plugin.setWar(new War(nationBlue, nationRed, capitol, maxDurationHours, pointsGoal));

                player.sendMessage("Started war: " + nationBlue.getName() + " vs " + nationRed.getName());
            }
        }
        
        
        return true;
    }
}
