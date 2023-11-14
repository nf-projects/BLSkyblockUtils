package me.kicksquare.blskyblockutils.playerlevel;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        if(!Objects.equals(params, "playerlevel")) {
            return null;
        }

        return String.valueOf(PlayerLevelCalculator.calculatePlayerLevel((Player) player).level);
    }
}
