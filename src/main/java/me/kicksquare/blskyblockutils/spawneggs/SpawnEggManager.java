package me.kicksquare.blskyblockutils.spawneggs;

import de.leonhard.storage.Config;
import de.leonhard.storage.sections.FlatFileSection;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.util.NBTUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpawnEggManager {

    private final BLSkyblockUtils plugin;
    private final MythicBukkit mythicBukkit;

    private final List<String> spawnEggNames = Arrays.asList("Ergeox");
    private final List<BossReward> bossRewards = Arrays.asList(
            new BossReward("Ergeox", new String[] {
                    "mi give CONSUMABLE LEGENDARY_UPGRADE_TOKEN %player% %int%"
            }));
    private List<ActiveBossFight> activeBossFights = new ArrayList<>();

    public SpawnEggManager(BLSkyblockUtils plugin, MythicBukkit mythicBukkit) {
        this.plugin = plugin;
        this.mythicBukkit = mythicBukkit;
    }

    // returns true if it's a valid spawn egg name
    public boolean giveCustomSpawnEgg(Player player, String spawnEggName) {
        if(!spawnEggNames.contains(spawnEggName)) {
            return false;
        }

        ItemStack spawnEgg = new ItemStack(Material.OCELOT_SPAWN_EGG);
        spawnEgg = NBTUtil.setNBTString(spawnEgg, "boss_mm_name", spawnEggName);
        ItemMeta spawnEggMeta = spawnEgg.getItemMeta();
        spawnEggMeta.setDisplayName(ChatColor.RED + "Spawn " + spawnEggName);
        spawnEgg.setItemMeta(spawnEggMeta);

        player.getInventory().addItem(spawnEgg);
        player.sendMessage(ChatColor.GREEN + "You have been given a " + spawnEggName + " spawn egg!");

        return true;
    }

    public void handleSpawnEggPlaced(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getItem() == null || event.getItem().getType() != Material.OCELOT_SPAWN_EGG) {
            return;
        }

        String nbtString = NBTUtil.getNBTString(event.getItem(), "boss_mm_name");
        if(nbtString == null) {
            return;
        }

        // at this point, we can cancel the event
        event.setCancelled(true);

        String mythicMobName = nbtString;

        if (!spawnEggNames.contains(mythicMobName)) {
            plugin.getLogger().warning("Invalid spawn egg name: " + mythicMobName);
            return;
        }

        // bedrock players can't spawn bosses
        if (player.getName().startsWith("*")) {
            player.sendMessage(ChatColor.RED + "Bedrock players currently cannot spawn bosses! Sorry for the inconvenience.");
            return;
        }

        // can only be spawned in the "dungeon" world
        if (!event.getClickedBlock().getWorld().getName().equals("dungeon")) {
            player.sendMessage(ChatColor.RED + "You can only spawn this mob in the dungeon world!");
            return;
        }

        // only one boss fight at a time
        if (activeBossFights.size() > 0) {
            player.sendMessage(ChatColor.RED + "There is already a boss fight in progress!");
            return;
        }

        Location location = event.getClickedBlock().getLocation().add(0, 1, 0);

        // remove the spawn egg from the player's inventory
        event.getItem().setAmount(event.getItem().getAmount() - 1);

        spawnMythicMob(mythicMobName, location);
        player.sendMessage(ChatColor.GREEN + "Spawned " + mythicMobName + "!");
        activeBossFights.add(new ActiveBossFight(player.getName(), mythicMobName));

        // 10-min timer to despawn the mob
        new BukkitRunnable() {
            @Override
            public void run() {
                despawnMythicMob(mythicMobName);
                player.sendMessage(ChatColor.RED + mythicMobName + " has despawned!");
                activeBossFights.removeIf(bossFight -> bossFight.getMythicMobName().equals(mythicMobName));
            }
        }.runTaskLater(plugin, 20 * 60 * 10);

        // warning to player every 30 seconds
        new BukkitRunnable() {
            int remainingTime = 10 * 60; // 10 minutes in seconds

            @Override
            public void run() {
                if (remainingTime <= 0) {
                    this.cancel();
                    return;
                }

                player.sendMessage(ChatColor.YELLOW + mythicMobName + " will despawn in " + remainingTime / 60 + " minutes and " + remainingTime % 60 + " seconds.");
                remainingTime -= 30;
            }
        }.runTaskTimer(plugin, 20 * 30, 20 * 30);
    }

    public void handleMythicMobDeath(MythicMobDeathEvent e) {
        // check if the mob that died was a boss
        String mythicMobName = e.getMob().getType().getInternalName();
        if (!spawnEggNames.contains(mythicMobName)) {
            return;
        }

        // try to get the player who spawned the boss
        String playerName = null;
        for (ActiveBossFight bossFight : activeBossFights) {
            if (bossFight.getMythicMobName().equals(mythicMobName)) {
                playerName = bossFight.getPlayerName();
                break;
            }
        }

        // if we couldn't find the player, just return and broadcast the death
        if (playerName == null) {
            Bukkit.broadcastMessage(ChatColor.RED + mythicMobName + " has been slain by an unknown player!");
            return;
        }

        // otherwise, broadcast the death and give the player a reward
        Bukkit.broadcastMessage(ChatColor.RED + mythicMobName + " has been slain by " + playerName + "!");
        Player player = Bukkit.getPlayer(playerName);

        // give the player a reward
        if (player != null) {
            // find the right reward from bossRewards
            for (BossReward bossReward : bossRewards) {
                if (bossReward.getBossName().equals(mythicMobName)) {
                    bossReward.executeRewardCommands(player.getName());
                }
            }
        }

        activeBossFights.removeIf(bossFight -> bossFight.getMythicMobName().equals(mythicMobName));
    }


    private void despawnMythicMob(String mythicMobName) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill " + mythicMobName);
    }

    private void spawnMythicMob(String mythicMobName, Location location) {
        despawnMythicMob(mythicMobName);

        // command format: /mm mobs spawn [mob_name]:<level> <amount> <world,x,y,z,yaw,pitch> Spawns mobs with the provided name.
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm mobs spawn " + mythicMobName + ":1 1 " + location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch());
    }

    public void despawnAllMythicMobs() {
        for (String mythicMobName : spawnEggNames) {
            despawnMythicMob(mythicMobName);
        }
    }
}