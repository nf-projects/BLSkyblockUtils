package me.kicksquare.blskyblockutils;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.kicksquare.blskyblockutils.capitols.War;
import me.kicksquare.blskyblockutils.capitols.WarManager;
import me.kicksquare.blskyblockutils.dungeon.DungeonDeathListener;
import me.kicksquare.blskyblockutils.dungeon.DungeonSpawnLocationUtil;
import me.kicksquare.blskyblockutils.mine.*;
import me.kicksquare.blskyblockutils.playerlevel.PlayerLevelCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggListener;
import me.kicksquare.blskyblockutils.spawneggs.SpawnEggManager;
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

    private War currentWar = null;

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

        getCommand("bladmin").setExecutor(new AdminCommands(this));

        // ----- Mine & Nethermine
        if (mainConfig.getBoolean("mine-module")) {

            // generate valid mine spawn locations// generate valid mine spawn locations
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                this.getLogger().info("Generating mine spawn locations");
                OverworldMineSpawnLocationFinder.generateValidSpawnLocations();
            }, 20 * 1);

            // generate valid netherimine spawn locations
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                this.getLogger().info("Generating nethermine spawn locations");
                NetherMineSpawnLocationFinder.generateValidSpawnLocations();
            }, 20 * 4);


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
        }


        // ----- DUNGEON BANDIT SPAWNING:
        if (mainConfig.getBoolean("dungeon-module")) {
            // generate valid dungeon spawn locations
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                this.getLogger().info("Generating dungeon spawn locations");
                DungeonSpawnLocationUtil.generateValidSpawnLocations();
            }, 20 * 7);

            long interval = 10L;
            int dungeonLimit = 25; //todo configurable

            // periodically spawn mobs in the dungeon
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToSpawnMobInDungeon(validDungeonSpawnLocations, dungeonLimit, mythicBukkit, this), 0, interval); // every 0.5 seconds, but 60 seconds after server start

            // every 10 minutes, kill off old dungeon mobs (in case they spawned in bugged areas)
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> killOldDungeonMobs(mythicBukkit), 0, 60 * 10 * 20);

            // halve XP on death and remove all ores from inventory in the dungeon
            Bukkit.getPluginManager().registerEvents(new DungeonDeathListener(this), this);
        }


        // ----- Custom Spawn Eggs
        if (mainConfig.getBoolean("boss-module")) {
            spawnEggManager = new SpawnEggManager(this, mythicBukkit);
            getCommand("getCustomSpawnEgg").setExecutor(new CustomSpawnEggCommand(this, spawnEggManager));
            getServer().getPluginManager().registerEvents(new CustomSpawnEggListener(this, spawnEggManager), this);
        }


        // ----- Leaderboards
        if (mainConfig.getBoolean("leaderboards-module")) {
            // periodically attempt to update leaderboards
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToUpdateLeaderboards(this), 0 * 60, 20 * 10); // every 10 seconds starting 60 seconds after server start
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiExtension(this).register();
        }

        // ----- Player level
        if (mainConfig.getBoolean("playerlevel-module")) {
            getCommand("playerlevel").setExecutor(new PlayerLevelCommand(this));
        }

        // ----- Capitols
        if (mainConfig.getBoolean("capitols-module")) {
            if (getServer().getPluginManager().getPlugin("WorldGuard") == null || getServer().getPluginManager().getPlugin("Lands") == null) {
                getLogger().warning("WorldGuard or Lands not found, Capitols module will NOT work.");
                Bukkit.getPluginManager().disablePlugin(this);
            }

            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                currentWar = WarManager.tickWar(currentWar, this);
            }, 0, 20); // once a second

        }
    }

    @Override
    public void onDisable() {
        try {
            spawnEggManager.despawnAllMythicMobs();
        } catch (Exception e) {
            System.out.println("Failed to despawn all custom spawn eggs");
        }

    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public War getCurrentWar() {
        return currentWar;
    }

    public void setWar(War war) {
        currentWar = war;
    }

    public War endCurrentWar() {
        War war = currentWar;
        currentWar = null;
        return war;
    }
}
