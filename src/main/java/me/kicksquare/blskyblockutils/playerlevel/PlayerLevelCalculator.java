package me.kicksquare.blskyblockutils.playerlevel;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlayerLevelCalculator {
    private static final double EXP = 2.71828182;

    public static PlayerLevelResult calculatePlayerLevel(Player p) {
        int level = 1;

        int combinedSkillLevel = papiInt(p, "%aureliumskills_power");
        // need to replace ',' with '' because PlaceholderAPI returns a string with commas
        int islandLevel = Integer.parseInt(PlaceholderAPI.setPlaceholders(p, "%superior_island_level%").replaceAll(",", ""));
        int playtimeHours = papiInt(p, "%statistic_hours_played%");
        int mobsKilled = papiInt(p, "%statistic_mob_kills%");
        int questsCompleted = papiInt(p, "%beautyquests_player_finished_amount%");

        int levelsFromSkills = getContributionFromSkillLevel(combinedSkillLevel);
        int levelsFromIslandLevel = getContributionFromIslandLevel(islandLevel);
        int levelsFromPlaytime = getContributionFromPlaytime(playtimeHours);
        int levelsFromMobsKilled = getContributionFromMobsKilled(mobsKilled);
        int levelsFromQuestsCompleted = getContributionFromQuestsCompleted(questsCompleted);

        level += levelsFromSkills + levelsFromIslandLevel + levelsFromPlaytime + levelsFromMobsKilled + levelsFromQuestsCompleted;

        PlayerLevelResult result = new PlayerLevelResult(level, levelsFromSkills, levelsFromIslandLevel, levelsFromPlaytime, levelsFromMobsKilled, levelsFromQuestsCompleted);

        return result;
    }

    private static int papiInt(Player p, String placeholder) {
        return Integer.parseInt(PlaceholderAPI.setPlaceholders(p, placeholder));
    }

    private static int getContributionFromSkillLevel(int skillLevel) {
        return (int) (125 - 125 * Math.pow(EXP, -0.005 * skillLevel));
    }

    private static int getContributionFromIslandLevel(int islandLevel) {
        return (int) (150 - 150 * Math.pow(EXP, -0.0000001 * islandLevel));
    }

    private static int getContributionFromPlaytime(int playtimeHours) {
        return (int) (75 - 75 * Math.pow(EXP, -0.003 * playtimeHours));
    }

    private static int getContributionFromMobsKilled(int mobsKilled) {
        return (int) (100 - 100 * Math.pow(EXP, -0.00001 * mobsKilled));
    }

    private static int getContributionFromQuestsCompleted(int questsCompleted) {
        return (int) (50 - 50 * Math.pow(EXP, -0.14 * questsCompleted));
    }
}
