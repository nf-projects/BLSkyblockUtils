package me.kicksquare.blskyblockutils;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.kicksquare.blskyblockutils.dungeon.DungeonDeathListener;
import me.kicksquare.blskyblockutils.dungeon.DungeonSpawnLocationUtil;
import me.kicksquare.blskyblockutils.mine.*;
import me.kicksquare.blskyblockutils.playerlevel.PapiExtension;
import me.kicksquare.blskyblockutils.playerlevel.PlayerLevelCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggListener;
import me.kicksquare.blskyblockutils.spawneggs.SpawnEggManager;
import me.kicksquare.blskyblockutils.util.AdminUtilCommands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.kicksquare.blskyblockutils.dungeon.DungeonSpawner.killOldDungeonMobs;
import static me.kicksquare.blskyblockutils.leaderboards.LeaderboardUpdater.attemptToUpdateLeaderboards;
import static me.kicksquare.blskyblockutils.mine.NetherMineSpawner.attemptToSpawnMobInNetherMine;
import static me.kicksquare.blskyblockutils.mine.OverworldMineSpawner.attemptToSpawnMobInOverworldMine;
import static me.kicksquare.blskyblockutils.dungeon.DungeonSpawner.attemptToSpawnMobInDungeon;

public final class BLSkyblockUtils extends JavaPlugin {
    private static BLSkyblockUtils plugin;
    private Config mainConfig;

    public List<Location> validOverworldMineSpawnLocations = new ArrayList<>();
    public List<Location> validNetherMineSpawnLocations = new ArrayList<>();
    public List<Location> validDungeonSpawnLocations = new ArrayList<>();

    private SpawnEggManager spawnEggManager;

    public static BLSkyblockUtils getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        MythicBukkit mythicBukkit = MythicBukkit.inst();

        mainConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        getCommand("bladmin").setExecutor(new AdminUtilCommands(this));

        // generate valid mine spawn locations
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.getLogger().info("Generating mine spawn locations");
            OverworldMineSpawnLocationFinder.generateValidSpawnLocations();
        }, 20 * 1);

        // generate valid netherimine spawn locations
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.getLogger().info("Generating nethermine spawn locations");
            NetherMineSpawnLocationFinder.generateValidSpawnLocations();
        }, 20 * 4);

        // generate valid dungeon spawn locations
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.getLogger().info("Generating dungeon spawn locations");
            DungeonSpawnLocationUtil.generateValidSpawnLocations();
        }, 20 * 7);

        // twice a second
        long interval = 10L;
        int mineLimit = mainConfig.getInt("respawn-mine-mobs-limit");

        // periodically spawn mobs in the mine
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInOverworldMine(validOverworldMineSpawnLocations, mineLimit), 20 * 15, interval);

        // periodically spawn mobs in the nether mine
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInNetherMine(validNetherMineSpawnLocations, this, mythicBukkit), 20 * 15, interval);

        // play a sound when you mine ores in the mine
        Bukkit.getPluginManager().registerEvents(new MineSoundListener(), this);

        // halve XP on death and remove all ores from inventory in the mine
        Bukkit.getPluginManager().registerEvents(new MineDeathListener(), this);

        // netherite block nether mine drops custom ExecutableItems drop
        Bukkit.getPluginManager().registerEvents(new MineNetheriteListener(), this);

        // gold nugget drop listener
        Bukkit.getPluginManager().registerEvents(new MineGoldnuggetListener(), this);


        // DUNGEON BANDIT SPAWNING:

        int dungeonLimit = 25;

        // periodically spawn mobs in the dungeon
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInDungeon(validDungeonSpawnLocations, dungeonLimit, mythicBukkit), 0, interval); // every 0.5 seconds, but 60 seconds after server start

        // every 10 minutes, kill off old dungeon mobs (in case they spawned in bugged areas)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> killOldDungeonMobs(mythicBukkit), 0, 60 * 10 * 20);

        // halve XP on death and remove all ores from inventory in the mine
        Bukkit.getPluginManager().registerEvents(new DungeonDeathListener(), this);

        // Custom Spawn Eggs
        spawnEggManager = new SpawnEggManager(this, mythicBukkit);
        getCommand("getCustomSpawnEgg").setExecutor(new CustomSpawnEggCommand(this, spawnEggManager));
        getServer().getPluginManager().registerEvents(new CustomSpawnEggListener(this, spawnEggManager), this);

        // ----- leader boards

        // periodically attempt to update leaderboards
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToUpdateLeaderboards(this), 0 * 60, 20 * 10); // every 10 seconds starting 60 seconds after server start

        // player level
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExtension(this).register();
        }

        getCommand("playerlevel").setExecutor(new PlayerLevelCommand(this));
    }

    @Override
    public void onDisable() {
        spawnEggManager.despawnAllMythicMobs();
    }

    public Config getMainConfig() {
        return mainConfig;
    }
}
