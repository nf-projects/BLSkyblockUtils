package me.kicksquare.blskyblockutils.dungeon;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.util.ExperienceUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DungeonDeathListener implements Listener {
    private final BLSkyblockUtils plugin;

    public DungeonDeathListener(BLSkyblockUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!event.getEntity().getWorld().getName().equals("dungeon")) return;

        Player p = event.getEntity();

        // set dropped XP to 50% of original
        int xp = ExperienceUtil.getExp(p);
        ExperienceUtil.changeExp(p, -(xp / 2));

        // remove dungeon drops from inventory
        String[] dungeonItemNames = plugin.getMainConfig().getStringList("dungeon-death-items").toArray(new String[0]);


        // loops through the inventory and removes any ores
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (p.getInventory().getItem(i) == null) continue;

            String itemName = p.getInventory().getItem(i).getType().name();

            for (int j = 0; j < dungeonItemNames.length; j++) {
                if (itemName.equals(dungeonItemNames[j])) {
                    p.getInventory().setItem(i, null);
                    break;
                }
            }
        }

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou died in the dungeon!"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou lost 50% of your XP and all Bandit drops in your inventory!"));
    }
}
