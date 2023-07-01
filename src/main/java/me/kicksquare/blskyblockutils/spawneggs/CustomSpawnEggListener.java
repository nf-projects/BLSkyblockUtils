package me.kicksquare.blskyblockutils.spawneggs;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CustomSpawnEggListener implements Listener {

    private final BLSkyblockUtils plugin;
    private final SpawnEggManager spawnEggManager;

    public CustomSpawnEggListener(BLSkyblockUtils plugin, SpawnEggManager spawnEggManager) {
        this.plugin = plugin;
        this.spawnEggManager = spawnEggManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        spawnEggManager.handleSpawnEggPlaced(event);
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        spawnEggManager.handleMythicMobDeath(event);
    }
}