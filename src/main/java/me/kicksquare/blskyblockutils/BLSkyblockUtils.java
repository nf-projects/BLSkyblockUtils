package me.kicksquare.blskyblockutils;

import me.kicksquare.blskyblockutils.mine.SpawnLocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static me.kicksquare.blskyblockutils.mine.Spawner.attemptToSpawnMobInMine;

public final class BLSkyblockUtils extends JavaPlugin {
    private static BLSkyblockUtils plugin;
    public List<Location> validSpawnLocations = new ArrayList<>();

    public static BLSkyblockUtils getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.getLogger().info("Generating spawn locations");
            SpawnLocationUtil.generateValidSpawnLocations();
        }, 20 * 5);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInMine(validSpawnLocations), 20 * 60, 10); // every 0.5 seconds, but 60 seconds after server start
    }


}
