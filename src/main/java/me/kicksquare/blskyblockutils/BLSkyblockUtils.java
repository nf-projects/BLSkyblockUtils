package me.kicksquare.blskyblockutils;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.kicksquare.blskyblockutils.dungeon.DungeonSpawnLocationUtil;
import me.kicksquare.blskyblockutils.mine.DeathListener;
import me.kicksquare.blskyblockutils.mine.MineSoundListener;
import me.kicksquare.blskyblockutils.mine.MineSpawnLocationUtil;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggListener;
import me.kicksquare.blskyblockutils.spawneggs.SpawnEggManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.kicksquare.blskyblockutils.dungeon.DungeonSpawner.killOldDungeonMobs;
import static me.kicksquare.blskyblockutils.leaderboards.LeaderboardUpdater.attemptToUpdateLeaderboards;
import static me.kicksquare.blskyblockutils.mine.MineSpawner.attemptToSpawnMobInMine;
import static me.kicksquare.blskyblockutils.dungeon.DungeonSpawner.attemptToSpawnMobInDungeon;

public final class BLSkyblockUtils extends JavaPlugin {
    private static BLSkyblockUtils plugin;
    private Config mainConfig;

    public List<Location> validMineSpawnLocations = new ArrayList<>();
    public List<Location> validDungeonSpawnLocations = new ArrayList<>();

    private SpawnEggManager spawnEggManager;

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
            this.getLogger().info("Generating mine spawn locations");
            MineSpawnLocationUtil.generateValidSpawnLocations();
        }, 20 * 5);

        // generate valid dungeon spawn locations
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.getLogger().info("Generating dungeon spawn locations");
            DungeonSpawnLocationUtil.generateValidSpawnLocations();
        }, 20 * 5);

        // twice a second
        long interval = 10L;
        int mineLimit = mainConfig.getInt("respawn-mine-mobs-limit");

        // periodically spawn mobs in the mine
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInMine(validMineSpawnLocations, mineLimit), 20 * 60, interval); // every 0.5 seconds, but 60 seconds after server start

        // play a sound when you mine ores in the mine
        Bukkit.getPluginManager().registerEvents(new MineSoundListener(), this);

        // halve XP on death and remove all ores from inventory in the mine
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);


        // DUNGEON BANDIT SPAWNING:
        MythicBukkit mythicBukkit = MythicBukkit.inst();

        int dungeonLimit = 17;

        // periodically spawn mobs in the dungeon
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInDungeon(validDungeonSpawnLocations, dungeonLimit, mythicBukkit), 0, interval); // every 0.5 seconds, but 60 seconds after server start

        // every 10 minutes, kill off old dungeon mobs
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> killOldDungeonMobs(mythicBukkit), 0, 60 * 10 * 20);

        // Custom Spawn Eggs
        spawnEggManager = new SpawnEggManager(this, mythicBukkit);
        getCommand("getCustomSpawnEgg").setExecutor(new CustomSpawnEggCommand(this, spawnEggManager));
        getServer().getPluginManager().registerEvents(new CustomSpawnEggListener(this, spawnEggManager), this);

        // periodically attempt to update leaderboards
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToUpdateLeaderboards(this), 0 * 60, 20 * 10); // every 10 seconds starting 60 seconds after server start
    }

    @Override
    public void onDisable() {
        spawnEggManager.despawnAllMythicMobs();
    }

    public Config getMainConfig() {
        return mainConfig;
    }
}
