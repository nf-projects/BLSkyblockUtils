package me.kicksquare.blskyblockutils.leaderboards;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.leonhard.storage.Config;
import me.clip.placeholderapi.PlaceholderAPI;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LeaderboardUpdater {
    public static void attemptToUpdateLeaderboards(BLSkyblockUtils plugin){
        System.out.println(1);
        Config config = plugin.getMainConfig();

        // get the last time the leaderboards were updated
        String lastSentDateTime = config.getString("leaderboards-last-sent-date-time");

        // turn it into a date object
        Date lastSentDate;
        try {
            lastSentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(lastSentDateTime);
        } catch (ParseException e) {
            System.out.println(2);
            return;
        }


        Date currentDate = new Date();

        // leaderboards update every 24 hours - if the current date is more than 24 hours after the last update, update the leaderboards and update config
        if(currentDate.getTime() - lastSentDate.getTime() > 1000 * 60 * 60 * 24){
            // update leaderboards
            WebhookClient webhookClient = WebhookClient.withUrl(config.getString("leaderboards-webhook-url"));
            WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();
            embedBuilder.setTitle(new WebhookEmbed.EmbedTitle("Skyblock Leaderboard - " + new SimpleDateFormat("yyyy-MM-dd").format(currentDate), null));
            embedBuilder.setColor(0x00FF00);

            // generate the strings
            String islandLevelString = "";
            String baltopString = "";
            String gemTopString = "";
            for (String line : config.getStringList("leaderboards-level-lines")) {
                islandLevelString += PlaceholderAPI.setPlaceholders(null, line) + "\n";
            }

            for (String line : config.getStringList("leaderboards-baltop-lines")) {
                baltopString += PlaceholderAPI.setPlaceholders(null, line) + "\n";
            }

            for (String line : config.getStringList("leaderboards-gems-lines")) {
                gemTopString += PlaceholderAPI.setPlaceholders(null, line) + "\n";
            }

            embedBuilder.addField(new WebhookEmbed.EmbedField(false, "👑 Island Level Leaderboard", islandLevelString));
            embedBuilder.addField(new WebhookEmbed.EmbedField(false, "💴 Baltop Leaderboard", baltopString));
            embedBuilder.addField(new WebhookEmbed.EmbedField(false, "💎 Gem Top Leaderboard", gemTopString));

            webhookClient.send(embedBuilder.build());
            webhookClient.close();

            // update config
            config.set("leaderboards-last-sent-date-time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(currentDate));
        }
    }
}
