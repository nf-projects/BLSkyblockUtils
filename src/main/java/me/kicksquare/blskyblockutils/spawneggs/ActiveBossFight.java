package me.kicksquare.blskyblockutils.spawneggs;

public class ActiveBossFight {
    public String playerName;
    public String bossName;

    public ActiveBossFight(String playerName, String bossName) {
        this.playerName = playerName;
        this.bossName = bossName;
    }

    public String getMythicMobName() {
        return bossName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
