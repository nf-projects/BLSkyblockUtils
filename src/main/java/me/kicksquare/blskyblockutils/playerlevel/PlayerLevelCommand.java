package me.kicksquare.blskyblockutils.playerlevel;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class PlayerLevelCommand implements CommandExecutor {
    public PlayerLevelCommand(BLSkyblockUtils blSkyblockUtils) {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 0) {
            sender.sendMessage("Usage: /playerlevel");
            return true;
        }

        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player p = (Player) sender;

        p.sendMessage(colorize("&3&m----------------------------------------"));
        p.sendMessage(colorize("#2596be&lPlayer Level"));
        p.sendMessage(colorize(""));
        p.sendMessage(colorize("&bLevel: &3" + PlayerLevelCalculator.calculatePlayerLevel(p).level));
        p.sendMessage(colorize("&bLevels from Skills: &3" + PlayerLevelCalculator.calculatePlayerLevel(p).levelsFromSkills));
        p.sendMessage(colorize("&bLevels from Island Level: &3" + PlayerLevelCalculator.calculatePlayerLevel(p).levelsFromIslandLevel));
        p.sendMessage(colorize("&bLevels from Playtime: &3" + PlayerLevelCalculator.calculatePlayerLevel(p).levelsFromPlaytime));
        p.sendMessage(colorize("&bLevels from Mobs Killed: &3" + PlayerLevelCalculator.calculatePlayerLevel(p).levelsFromMobsKilled));
        p.sendMessage(colorize("&bLevels from Quests Completed: &3" + PlayerLevelCalculator.calculatePlayerLevel(p).levelsFromQuestsCompleted));
        p.sendMessage(colorize("&3&m----------------------------------------"));

        return true;
    }
}
