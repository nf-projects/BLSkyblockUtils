package me.kicksquare.blskyblockutils.spawneggs;

import org.bukkit.Bukkit;

public class BossReward {
    public final String bossName;
    public final String[] rewardCommands; // %player% will be replaced with the player's name; %boss% will be replaced with the boss's name; %int% will be replaced with a random int between 1 and 4 inclusive

    public BossReward(String bossName, String[] rewardCommands) {
        this.bossName = bossName;
        this.rewardCommands = rewardCommands;
    }

    public String getBossName() {
        return this.bossName;
    }

    public String[] getRewardCommands() {
        return this.rewardCommands;
    }

    public void executeRewardCommands(String playerName) {
        for (String command : this.rewardCommands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", playerName).replace("%boss%", this.bossName).replace("%int%", String.valueOf((int) (Math.random() * 4) + 1)));
        }
    }
}
