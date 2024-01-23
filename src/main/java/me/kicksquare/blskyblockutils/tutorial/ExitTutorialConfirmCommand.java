package me.kicksquare.blskyblockutils.tutorial;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class ExitTutorialConfirmCommand implements CommandExecutor {
    private BLSkyblockUtils plugin;

    public ExitTutorialConfirmCommand(BLSkyblockUtils blSkyblockUtils) {
        this.plugin = blSkyblockUtils;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player p = (Player) sender;
        String currentQuest = getPlayerCurrentQuest(p);

        if (currentQuest.equals("")) {
            p.sendMessage(colorize("&cYou are not currently in the tutorial!"));
            return true;
        }

        p.performCommand("quests:q cancel " + currentQuest);
        p.sendMessage(colorize("&aTutorial cancelled! &7Use /tutorial to continue."));

        return true;
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
