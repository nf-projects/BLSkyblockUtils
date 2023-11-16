package me.kicksquare.blskyblockutils.playerlevel;

public class PlayerLevelResult {
    public final int level;
    public final int levelsFromSkills;
    public final int levelsFromIslandLevel;
    public final int levelsFromPlaytime;
    public final int levelsFromMobsKilled;
    public final int levelsFromQuestsCompleted;

    public PlayerLevelResult(int level, int levelsFromSkills, int levelsFromIslandLevel, int levelsFromPlaytime, int levelsFromMobsKilled, int levelsFromQuestsCompleted) {
        this.level = level;
        this.levelsFromSkills = levelsFromSkills;
        this.levelsFromIslandLevel = levelsFromIslandLevel;
        this.levelsFromPlaytime = levelsFromPlaytime;
        this.levelsFromMobsKilled = levelsFromMobsKilled;
        this.levelsFromQuestsCompleted = levelsFromQuestsCompleted;
    }
}
