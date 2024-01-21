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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpawnEggManager {

    private final BLSkyblockUtils plugin;
    private final MythicBukkit mythicBukkit;

    private final List<String> spawnEggNames = new ArrayList<>();
    private final List<BossReward> bossRewards = new ArrayList<>();


    private final List<ActiveBossFight> activeBossFights = new ArrayList<>();

    public SpawnEggManager(BLSkyblockUtils plugin, MythicBukkit mythicBukkit) {
        this.plugin = plugin;
        this.mythicBukkit = mythicBukkit;

        List<String> bossesConfig = plugin.getMainConfig().getStringList("bosses");

        // read the boss config. syntax example:
        // # Syntax: `[boss name]|[command to run on kill. ]
        //# %player% will be replaced with the player's name; %boss% will be replaced with the boss's name; %int% will be replaced with a random int between 1 and 4 inclusive
        //bosses:
        //- "ergeox|mi give CONSUMABLE LEGENDARY_UPGRADE_TOKEN %player% %int%"
        //- "herobrine|broadcast Herobrine killed by %player%!!"
        for (String boss : bossesConfig) {
            String bossName = boss.split("\\|")[0];
            String[] rewardCommands = boss.split("\\|")[1].split(";");

            spawnEggNames.add(bossName);
            bossRewards.add(new BossReward(bossName, rewardCommands));

            plugin.getLogger().info("[BOSS SYSTEM] Loaded boss " + bossName);
        }
    }

    // returns true if it's a valid spawn egg name
    public boolean giveCustomSpawnEgg(Player player, String spawnEggName) {
        if (!spawnEggNames.contains(spawnEggName)) {
            return false;
        }

        ItemStack spawnEgg = new ItemStack(Material.OCELOT_SPAWN_EGG);
        spawnEgg = NBTUtil.setNBTString(spawnEgg, "boss_mm_name", spawnEggName);
        ItemMeta spawnEggMeta = spawnEgg.getItemMeta();
        spawnEggMeta.setDisplayName(ChatColor.RED + "Spawn " + spawnEggName);
        // make it glow
        spawnEggMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        spawnEggMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        spawnEgg.setItemMeta(spawnEggMeta);

        player.getInventory().addItem(spawnEgg);
        player.sendMessage(ChatColor.GREEN + "You have been given a " + spawnEggName + " spawn egg!");

        return true;
    }

    public void handleSpawnEggPlaced(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() == null || event.getItem().getType() != Material.OCELOT_SPAWN_EGG) {
            return;
        }

        String nbtString = NBTUtil.getNBTString(event.getItem(), "boss_mm_name");
        if (nbtString == null) {
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

        // remove the spawn egg from the player's inventory
        event.getItem().setAmount(event.getItem().getAmount() - 1);

        spawnMythicMob(mythicMobName);
        player.sendMessage(ChatColor.GREEN + "Spawned " + mythicMobName + "!");
        activeBossFights.add(new ActiveBossFight(player.getName(), mythicMobName));

        // 10-min timer to despawn the mob
        new BukkitRunnable() {
            @Override
            public void run() {
                // Find the active boss fight instance
                ActiveBossFight activeBossFight = activeBossFights.stream()
                        .filter(bossFight -> bossFight.getMythicMobName().equals(mythicMobName))
                        .findFirst()
                        .orElse(null);

                // If the active boss fight is not found or cancelled, stop this task
                if (activeBossFight == null || activeBossFight.isCancelled()) {
                    this.cancel();
                    return;
                }

                despawnMythicMob(mythicMobName);
                player.sendMessage(ChatColor.RED + formatName(mythicMobName) + " has despawned!");
                activeBossFights.removeIf(bossFight -> bossFight.getMythicMobName().equals(mythicMobName));
            }
        }.runTaskLater(plugin, 20 * 60 * 10);

        // warning to player every 30 seconds
        new BukkitRunnable() {
            int remainingTime = 10 * 60; // 10 minutes in seconds

            @Override
            public void run() {
                // Find the active boss fight instance
                ActiveBossFight activeBossFight = activeBossFights.stream()
                        .filter(bossFight -> bossFight.getMythicMobName().equals(mythicMobName))
                        .findFirst()
                        .orElse(null);

                // If the active boss fight is not found or cancelled, stop this repeating task
                if (activeBossFight == null || activeBossFight.isCancelled()) {
                    this.cancel();
                    return;
                }

                if (remainingTime <= 0) {
                    this.cancel();
                    return;
                }

                player.sendMessage(ChatColor.YELLOW + formatName(mythicMobName) + " will despawn in " + remainingTime / 60 + " minutes and " + remainingTime % 60 + " seconds.");
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

        // find the ActiveBossFight instance and set the isCancelled flag to true
        ActiveBossFight activeBossFight = null;
        for (ActiveBossFight bossFight : activeBossFights) {
            if (bossFight.getMythicMobName().equals(mythicMobName)) {
                activeBossFight = bossFight;
                break;
            }
        }
        if (activeBossFight != null) {
            activeBossFight.setCancelled(true);
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
            Bukkit.broadcastMessage(ChatColor.RED + formatName(mythicMobName) + " has been slain by an unknown player!");
            return;
        }

        // otherwise, broadcast the death and give the player a reward
        Bukkit.broadcastMessage(ChatColor.RED + formatName(mythicMobName) + " has been slain by " + playerName + "!");
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

    private String formatName(String mythicMobName) {
        return mythicMobName.substring(0, 1).toUpperCase() + mythicMobName.substring(1).toLowerCase();
    }


    private void despawnMythicMob(String mythicMobName) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill " + mythicMobName);
    }

    private void spawnMythicMob(String mythicMobName) {
        despawnMythicMob(mythicMobName);

        // command format: /mm mobs spawn [mob_name]:<level> <amount> <world,x,y,z,yaw,pitch> Spawns mobs with the provided name.
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm mobs spawn " + mythicMobName + ":1 1 " + plugin.getMainConfig().getString("boss-spawn-location"));
    }

    public void despawnAllMythicMobs() {
        for (String mythicMobName : spawnEggNames) {
            despawnMythicMob(mythicMobName);
        }
    }
}