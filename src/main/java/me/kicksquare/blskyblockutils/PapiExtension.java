package me.kicksquare.blskyblockutils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.capitols.War;
import me.kicksquare.blskyblockutils.playerlevel.PlayerLevelCalculator;
import me.kicksquare.blskyblockutils.tutorial.PAPITutorial;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static me.kicksquare.blskyblockutils.capitols.PlaceholderCalculator.parseWarPlaceholder;

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
        return "1.0.0";
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

        // 2 placeholder types:
        // %blskyblockutils_playerlevel%
        // %blskyblockutils_war_[status|nation_blue|nation_red|beacon_status|blue_points|red_points|time_elapsed|time_remaining|point_goal]%

        if (params.equalsIgnoreCase("playerlevel") && plugin.getMainConfig().getBoolean("playerlevel-module")) {
            return String.valueOf(PlayerLevelCalculator.calculatePlayerLevel((Player) player).level);
        } else if (params.startsWith("war_") && plugin.getMainConfig().getBoolean("capitols-module")) {
            return parseWarPlaceholder((Player) player, params);
        } else if (params.startsWith("tutorial_") && plugin.getMainConfig().getBoolean("tutorial-module")) {
            return PAPITutorial.parseTutorialPlaceholder((Player) player, params);
        }

        return null;
    }
}
