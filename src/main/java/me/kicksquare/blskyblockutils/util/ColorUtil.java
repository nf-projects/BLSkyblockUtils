package me.kicksquare.blskyblockutils.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    public static String colorizeColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /*
     * This method is used to colorize hex codes in a string.
     * Example input String: "Hello #FF0000World"
     */
    public static String colorizeHexCodes(String message) {
        Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = hexPattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String colorize(String input) {
        return colorizeHexCodes(colorizeColorCodes(input));
    }
}