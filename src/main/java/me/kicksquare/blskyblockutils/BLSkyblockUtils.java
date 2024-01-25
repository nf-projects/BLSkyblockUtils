package me.kicksquare.blskyblockutils;

import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.live.bemmamin.gps.api.GPSAPI;
import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.kicksquare.blskyblockutils.capitols.landspermissions.LandsMaxClaimsListener;
import me.kicksquare.blskyblockutils.capitols.landspermissions.UpdateMaxClaimsCommand;
import me.kicksquare.blskyblockutils.capitols.War;
import me.kicksquare.blskyblockutils.capitols.WarKillListener;
import me.kicksquare.blskyblockutils.capitols.WarUtil;
import me.kicksquare.blskyblockutils.capitols.buffs.BuffManager;
import me.kicksquare.blskyblockutils.capitols.buffs.CapitalControllerManager;
import me.kicksquare.blskyblockutils.dungeon.DungeonDeathListener;
import me.kicksquare.blskyblockutils.dungeon.DungeonSpawnLocationUtil;
import me.kicksquare.blskyblockutils.killer.VillagerKiller;
import me.kicksquare.blskyblockutils.mine.*;
import me.kicksquare.blskyblockutils.playerlevel.PlayerLevelCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggCommand;
import me.kicksquare.blskyblockutils.spawneggs.CustomSpawnEggListener;
import me.kicksquare.blskyblockutils.spawneggs.SpawnEggManager;
import me.kicksquare.blskyblockutils.spawners.SpawnerTick;
import me.kicksquare.blskyblockutils.tutorial.*;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    private CapitalControllerManager capitalControllerManager;

    private RoseStackerAPI rsAPI;
    private LuckPerms luckpermsAPI;
    private GPSAPI gpsAPI = null;
    private BukkitQuestsPlugin questAPI = null;
    private MythicBukkit mythicBukkit = null;

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

        getCommand("bladmin").setExecutor(new AdminCommands(this));

        if (
                mainConfig.getBoolean("mine-module")
                        || mainConfig.getBoolean("dungeon-module")
                        || mainConfig.getBoolean("boss-module")
        ) {
            mythicBukkit = MythicBukkit.inst();
        }

        // ----- Mine & Nethermine
        if (mainConfig.getBoolean("mine-module")) {

            // generate valid mine spawn locations// generate valid mine spawn locations
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                this.getLogger().info("Generating mine spawn locations");
                OverworldMineSpawnLocationFinder.generateValidSpawnLocations();
            }, 20);

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
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> attemptToUpdateLeaderboards(this), 0, 20 * 10); // every 10 seconds starting 60 seconds after server start
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
                currentWar = WarUtil.tickWar(currentWar, this);
            }, 0, 20); // once a second

            Bukkit.getPluginManager().registerEvents(new WarKillListener(this), this);

            capitalControllerManager = new CapitalControllerManager(this);
            capitalControllerManager.loadBuffsFromConfig();

            // for buffs

            // check for RoseStacker plugin
            if (Bukkit.getPluginManager().getPlugin("RoseStacker") != null) {
                rsAPI = RoseStackerAPI.getInstance();

                if (rsAPI == null) {
                    getLogger().warning("RoseStacker API is null, capitols module will NOT work.");
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            }

            Bukkit.getPluginManager().registerEvents(new BuffManager(), this);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                BuffManager.tickNationBuffs();
            }, 0, 20 * 10); // once every 10 seconds


            // FOR LANDS MAX-CLAIM SET/ADD/REMOVE COMMANDS
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckpermsAPI = provider.getProvider();
            } else {
                getLogger().warning("LuckPerms API is null, capitols module will NOT work.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
            getCommand("updatemaxclaims").setExecutor(new UpdateMaxClaimsCommand(this, luckpermsAPI));
            Bukkit.getPluginManager().registerEvents(new LandsMaxClaimsListener(this), this);
        }

        // module to periodically kill all villagers, zombie villagers, wandering traders, and trader llamas
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            VillagerKiller.killVillagersEtc();
        }, 0, 20 * 10); // once every 10 seconds

        if (mainConfig.getBoolean("spawners-module")) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                SpawnerTick.tick(this);
            }, 0, 20 * 15); // once every 15 seconds

            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                SpawnerTick.tickLadderMobs(this);
            }, 0, 10); // once every 0.5 seconds, kill any mobs on ladders (trying to escape!)
        }

        if (mainConfig.getBoolean("tutorial-module")) {
            if (Bukkit.getPluginManager().getPlugin("GPS").isEnabled()) {
                this.gpsAPI = new GPSAPI(this);
            } else {
                getLogger().warning("GPS plugin not found, tutorial module will NOT work.");
                Bukkit.getPluginManager().disablePlugin(this);
            }

            if (Bukkit.getPluginManager().getPlugin("Quests").isEnabled()) {
                this.questAPI = (BukkitQuestsPlugin) Bukkit.getPluginManager().getPlugin("Quests");
            } else {
                getLogger().warning("Quests plugin not found, tutorial module will NOT work.");
                Bukkit.getPluginManager().disablePlugin(this);
            }

            Bukkit.getPluginManager().registerEvents(new TutorialEventsManager(this), this);
            getCommand("exittutorialconfirm").setExecutor(new ExitTutorialConfirmCommand(this));

            // custom task types
//            questAPI.getTaskTypeManager().registerTaskType(new CrateOpenTaskType(this));
            questAPI.getTaskTypeManager().registerTaskType(new LandCreateTaskType(this));
            questAPI.getTaskTypeManager().registerTaskType(new IslandCreateTaskType(this));
            questAPI.getTaskTypeManager().registerTaskType(new ClaimChunksTaskType(this));
        }

    }

    @Override
    public void onDisable() {
        try {
            spawnEggManager.despawnAllMythicMobs();
        } catch (Exception e) {

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

    public CapitalControllerManager getCapitalControllerManager() {
        return capitalControllerManager;
    }

    public LuckPerms getLuckpermsAPI() {
        return luckpermsAPI;
    }

    public RoseStackerAPI getRsAPI() {
        return rsAPI;
    }

    public GPSAPI getGpsAPI() {
        return gpsAPI;
    }

    public BukkitQuestsPlugin getQuestAPI() {
        return questAPI;
    }
}
