package me.kicksquare.blskyblockutils;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.kicksquare.blskyblockutils.mine.DeathListener;
import me.kicksquare.blskyblockutils.mine.MineSoundListener;
import me.kicksquare.blskyblockutils.mine.SpawnLocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.kicksquare.blskyblockutils.mine.Spawner.attemptToSpawnMobInMine;

public final class BLSkyblockUtils extends JavaPlugin {
    private static BLSkyblockUtils plugin;
    private Config mainConfig;

    public List<Location> validSpawnLocations = new ArrayList<>();

    public static BLSkyblockUtils getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        mainConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        // generate valid mine spawn locations
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.getLogger().info("Generating spawn locations");
            SpawnLocationUtil.generateValidSpawnLocations();
        }, 20 * 5);

        long interval = mainConfig.getLong("respawn-mine-mobs-delay-seconds");
        int limit = mainConfig.getInt("respawn-mine-mobs-limit");

        // periodically spawn mobs in the mine
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInMine(validSpawnLocations, limit), 20 * 60, interval * 20); // every 0.5 seconds, but 60 seconds after server start

        // play a sound when you mine ores in the mine
        Bukkit.getPluginManager().registerEvents(new MineSoundListener(), this);

        // halve XP on death and remove all ores from inventory in the mine
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
    }

    public Config getMainConfig() {
        return mainConfig;
    }
}
