package me.kicksquare.blskyblockutils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.nation.Nation;
import me.kicksquare.blskyblockutils.capitols.Capitol;
import me.kicksquare.blskyblockutils.capitols.War;
import me.kicksquare.blskyblockutils.capitols.WarUtil;
import me.kicksquare.blskyblockutils.util.TimeFormatterUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import static me.kicksquare.blskyblockutils.capitols.WarUtil.replaceBlueAndRedWithWhite;
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
        // /blskyblockutils resetallbeacons
        // /blskyblockutils listCapitols
        // /blskyblockutils setCapitolController <capitol name> <controller nation name OR None>

        if (args.length == 0) {
            player.sendMessage("Usage: ");
            player.sendMessage("/blskyblockutils getActiveWar");
            player.sendMessage("/blskyblockutils endWar");
            player.sendMessage("/blskyblockutils startwar <nation blue> <nation red> <capitol region name> <maxDurationHours> <pointsGoal>");
            player.sendMessage("/blskyblockutils resetallbeacons");
            player.sendMessage("/blskyblockutils listCapitols");
            player.sendMessage("/blskyblockutils setCapitolController <capitol name> <controller nation name OR None>");
            player.sendMessage("/blskyblockutils getItemNbt");
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("getActiveWar")) {
                War war = plugin.getCurrentWar();

                if (war == null) {
                    player.sendMessage("There is no active war!");
                    return true;
                }

                player.sendMessage(colorize("&Active war: " + war.nationBlue.getName() + " vs " + war.nationRed.getName()));
                player.sendMessage(colorize("&aBlue Points: " + war.getNationBlueCurrentPoints()));
                player.sendMessage(colorize("&aRed Points: " + war.getNationRedCurrentPoints()));
                player.sendMessage(colorize("&aBeacon 1 status: " + war.getBeaconStatus(1).toString()));
                player.sendMessage(colorize("&aBeacon 2 status: " + war.getBeaconStatus(2).toString()));
                player.sendMessage(colorize("&aBeacon 3 status: " + war.getBeaconStatus(3).toString()));
                player.sendMessage(colorize("&aBeacon 4 status: " + war.getBeaconStatus(4).toString()));
                player.sendMessage(colorize("&aTime Elapsed: " + TimeFormatterUtil.formatSeconds(war.getSecondsElapsed())));
                player.sendMessage(colorize("&aTime Remaining: " + TimeFormatterUtil.formatSeconds(war.getSecondsRemaining())));

                return true;
            }

            if (args[0].equalsIgnoreCase("endWar")) {
                if (plugin.getCurrentWar() == null) {
                    player.sendMessage("There is no active war!");
                    return true;
                }

                WarUtil.endWar();
                return true;
            }

            if (args[0].equalsIgnoreCase("resetallbeacons")) {
                World world = Bukkit.getWorld("world");
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(world));

                for (String beaconRegionName : Capitol.getAllBeaconRegionNames()) {
                    ProtectedRegion beaconRegion = regions.getRegion(beaconRegionName);

                    if (beaconRegion == null) {
                        player.sendMessage("Region " + beaconRegionName + " does not exist! Continuing...");
                        continue;
                    }

                    replaceBlueAndRedWithWhite(beaconRegion);
                }

                player.sendMessage("All beacons have been reset to white/neutral!");

                return true;
            }

            if (args[0].equalsIgnoreCase("listCapitols")) {
                player.sendMessage("Capitols: ");
                for (Capitol capitol : Capitol.getAllCapitols()) {
                    Nation capitolController = LandsIntegration.of(BLSkyblockUtils.getPlugin()).getNationByName(plugin.getCapitalControllerManager().getCapitolController(capitol));
                    String name = "no one";
                    if (capitolController != null) {
                        name = capitolController.getName();
                    }
                    player.sendMessage(capitol + " - controlled by " + name);
                }

                return true;
            }

            if (args[0].equalsIgnoreCase("getItemNbt")) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage("You must be holding an item!");
                    return true;
                }

                // send info about:
                // nbt
                // custom model data
                // if applicable, dye color (R G B values)

                NBTItem nbtItem = new NBTItem(item);
                player.sendMessage("NBT: " + nbtItem);

                if (item.getItemMeta().hasCustomModelData()) {
                    player.sendMessage("Custom Model Data: " + item.getItemMeta().getCustomModelData());
                }

                if (item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_LEGGINGS) {
                    LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
                    player.sendMessage("Dye Color: " + leatherArmorMeta.getColor().getRed() + " " + leatherArmorMeta.getColor().getGreen() + " " + leatherArmorMeta.getColor().getBlue());
                }
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setCapitolController")) {
                String capitolName = args[1];
                String nationName = args[2];

                Capitol capitol = Capitol.valueOf(capitolName);
                if (capitol == null) {
                    player.sendMessage("Capitol " + capitolName + " does not exist!");
                    return true;
                }

                if (nationName.equalsIgnoreCase("none")) {
                    plugin.getCapitalControllerManager().setCapitolController(capitol, null);
                    player.sendMessage("Capitol " + capitolName + " is now controlled by no one!");
                    return true;
                }

                Nation nation = LandsIntegration.of(BLSkyblockUtils.getPlugin()).getNationByName(nationName);
                if (nation == null) {
                    player.sendMessage("Nation " + nationName + " does not exist!");
                    return true;
                }

                plugin.getCapitalControllerManager().setCapitolController(capitol, nation.getName());
                player.sendMessage("Capitol " + capitolName + " is now controlled by " + nationName + "!");
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
