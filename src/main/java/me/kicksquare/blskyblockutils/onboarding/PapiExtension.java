package me.kicksquare.blskyblockutils.onboarding;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Objects;

public class PapiExtension extends PlaceholderExpansion {

    private final BLSkyblockUtils plugin;

    public PapiExtension(BLSkyblockUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "kicksquare";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "blskyblockutils";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2.3";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else papi will unregister the expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        if (!player.isOnline()) {
            return null;
        }

        Player p = player.getPlayer();

        String param = params.toLowerCase();

        // options: %blskyblockutils_onboarding_sb_1%, %blskyblockutils_onboarding_sb_2%, %blskyblockutils_onboarding_sb_3%, %blskyblockutils_onboarding_sb_4%

        // each placeholder will be 1 scoreboard line
        // it will be a "checklist" of things to do when the player first plays (like creating an island)
        // the scoreboard will be shown to the player until they have completed all of the tasks

        // first one: create an island. if they have an island, it will be checked off
        // second one: go to the mine. if they have been to the mine, it will be checked off
        // third one: get full diamond armor. if they have full diamond armor, it will be checked off
        // fourth one: get a spawner. if they have a spawner, it will be checked off

        // the last 3 events use database storage to track progress. the first just uses the island plugin placeholder
        switch (param) {
            case "onboarding_sb_3":
                Object player_uuid = p.getUniqueId().toString();
                ResultSet resultSet = plugin.getDatabase().query("SELECT COUNT(*) FROM onboarding_achievements " +
                        "WHERE event_type = 'full_diamond' " +
                        "AND player_uuid = ?;", player_uuid);

//                try {
//                    while(resultSet.next())
//                    {
//                        resultSet.getString(1);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                if (resultSet != null) {
                    try {
                        if (resultSet.next()) {
                            int count = resultSet.getInt(1);
                            if (count > 0) {
                                return "Achievement: Full Diamond Armor";
                            } else {
                                return "Get full diamond armor!";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }

        return null;
    }
}
