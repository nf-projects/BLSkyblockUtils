package me.kicksquare.blskyblockutils.spawneggs;

import de.leonhard.storage.Config;
import de.leonhard.storage.sections.FlatFileSection;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnEggManager {

    private final BLSkyblockUtils plugin;
    private final Map<String, MythicMobInfo> mythicMobInfoMap;
    private final Map<String, String> spawnedMythicMobs;

    public SpawnEggManager(BLSkyblockUtils plugin) {
        this.plugin = plugin;
        mythicMobInfoMap = loadMythicMobInfo();
        System.out.println("SpawnEggManager.SpawnEggManager: mythicMobInfoMap = " + mythicMobInfoMap);
        spawnedMythicMobs = new HashMap<>();
    }

    public boolean giveCustomSpawnEgg(Player player, String spawnEggName) {
        MythicMobInfo info = mythicMobInfoMap.get(spawnEggName);
        if (info == null) {
            return false;
        }

        ItemStack spawnEgg = info.createSpawnEgg();
        player.getInventory().addItem(spawnEgg);
        return true;
    }

    public void handleSpawnEggInteraction(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String spawnEggName = ChatColor.stripColor(meta.getDisplayName());
        MythicMobInfo mobInfo = mythicMobInfoMap.get(spawnEggName);
        if (mobInfo == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!mobInfo.getAllowedWorlds().contains(player.getWorld().getName())) {
            player.sendMessage("You can't use this spawn egg in this world.");
            return;
        }

        if (spawnedMythicMobs.containsKey(mobInfo.getMythicMobName())) {
            player.sendMessage("A mob of this type is already alive.");
            return;
        }

        Location spawnLocation = player.getLocation().add(0, 1, 0);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m s " + mobInfo.getMythicMobName() + " " + player.getName() + " " + spawnLocation.toString());

        spawnedMythicMobs.put(mobInfo.getMythicMobName(), player.getName());
        startMythicMobTimer(mobInfo);
        item.setAmount(item.getAmount() - 1);
    }

    private void startMythicMobTimer(MythicMobInfo mobInfo) {
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            private int remainingMinutes = mobInfo.getMinutesAlive();

            @Override
            public void run() {
                if (remainingMinutes <= 0) {
                    despawnMythicMob(mobInfo.getMythicMobName());
                } else {
                    String playerName = spawnedMythicMobs.get(mobInfo.getMythicMobName());
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        player.sendMessage("Your mob will despawn in " + remainingMinutes + " minutes.");
                    }
                    remainingMinutes--;
                }
            }
        }, 0L, 20 * 60L); // Run every minute (20 ticks/second * 60 seconds)
    }

    public void despawnAllMythicMobs() {
        for (String mythicMobName : spawnedMythicMobs.keySet()) {
            despawnMythicMob(mythicMobName);
        }
    }

    private void despawnMythicMob(String mythicMobName) {
        MythicMobInfo mobInfo = getMythicMobInfoByName(mythicMobName);
        if (mobInfo == null) {
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill " + mythicMobName);
        String playerName = spawnedMythicMobs.get(mythicMobName);
        if (playerName != null) {
            for (String command : mobInfo.getCommandsOnDeath()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%PLAYER%", playerName));
            }
        }

        spawnedMythicMobs.remove(mythicMobName);
    }

    private MythicMobInfo getMythicMobInfoByName(String mythicMobName) {
        for (MythicMobInfo info : mythicMobInfoMap.values()) {
            if (info.getMythicMobName().equals(mythicMobName)) {
                return info;
            }
        }
        return null;
    }

    private Map<String, MythicMobInfo> loadMythicMobInfo() {
        Config mainConfig = plugin.getMainConfig();
        FlatFileSection configSection = mainConfig.getSection("mythicMobs");

        Map<String, MythicMobInfo> mobInfoMap = new HashMap<>();

        if (configSection != null) {
            for (String key : configSection.keySet()) {
                FlatFileSection mobSection = configSection.getSection(key);

                if (mobSection == null) {
                    continue;
                }

                String mythicMobName = mobSection.getString("name");
                String spawnEggName =ChatColor.translateAlternateColorCodes('&', mobSection.getString("spawnEggName"));
                int minutesAlive = mobSection.getInt("minutesAlive");
                List<String> allowedWorlds = mobSection.getStringList("allowedWorlds");
                List<String> commandsOnDeath = mobSection.getStringList("commandsOnDeath");

                MythicMobInfo mobInfo = new MythicMobInfo(mythicMobName, spawnEggName, minutesAlive, allowedWorlds, commandsOnDeath);
                mobInfoMap.put(spawnEggName, mobInfo);
            }
        }

        return mobInfoMap;
    }
}