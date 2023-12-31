package me.kicksquare.blskyblockutils.util;

public class TimeFormatterUtil {
    public static String formatSeconds(int totalSeconds) {
        if (totalSeconds < 0) {
            return "Invalid input";
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return (hours > 0 ? hours + "h " : "") +
                (minutes > 0 || hours > 0 ? minutes + "m " : "") +
                seconds + "s";
    }
}
