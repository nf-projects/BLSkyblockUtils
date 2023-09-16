package me.kicksquare.blskyblockutils.util;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdminUtilCommands implements CommandExecutor {
    private final BLSkyblockUtils plugin;
    public AdminUtilCommands(BLSkyblockUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player player = (Player) commandSender;
        if(!player.hasPermission("blskyblockutils.admin")) {
            player.sendMessage("You do not have permission to use this command!");
            return true;
        }

        if(args.length == 0) {
            player.sendMessage("Usage: /blskyblockutils <getitemdata>");
            return true;
        }

        if(args[0].equalsIgnoreCase("getitemdata")) {
            if(args.length != 1) {
                player.sendMessage("Usage: /blskyblockutils getitemdata");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if(item == null) {
                player.sendMessage("You must be holding an item to use this command!");
                return true;
            }

            player.sendMessage("Custom Model Data: " + item.getItemMeta().getCustomModelData());
            return true;
        }

        return true;
    }
}
