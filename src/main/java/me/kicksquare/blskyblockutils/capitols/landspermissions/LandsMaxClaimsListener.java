package me.kicksquare.blskyblockutils.capitols.landspermissions;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LandsMaxClaimsListener implements Listener {
    private BLSkyblockUtils plugin;

    public LandsMaxClaimsListener(BLSkyblockUtils blSkyblockUtils) {
        this.plugin = blSkyblockUtils;
    }

    @EventHandler
    public void playerFirstJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPlayedBefore()) {
            return;
        }

        // give player 15 claims
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "updatemaxclaims " + event.getPlayer().getName() + " set 15");
    }
}
