package me.kicksquare.blskyblockutils.spawneggs;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomSpawnEggCommand implements CommandExecutor {

    private final BLSkyblockUtils plugin;
    private final SpawnEggManager spawnEggManager;

    public CustomSpawnEggCommand(BLSkyblockUtils plugin, SpawnEggManager spawnEggManager) {
        this.plugin = plugin;
        this.spawnEggManager = spawnEggManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("blskyblockutils.getcustomspawnegg")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /getCustomSpawnEgg <spawnEggName>");
            return true;
        }

        String spawnEggName = args[0];
        if (!spawnEggManager.giveCustomSpawnEgg(player, spawnEggName)) {
            player.sendMessage("Invalid spawn egg name.");
        } else {
            player.sendMessage("You received a custom spawn egg.");
        }
        return true;
    }
}