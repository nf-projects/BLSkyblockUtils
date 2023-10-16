package me.kicksquare.blskyblockutils.mine;

import me.kicksquare.blskyblockutils.util.ExperienceUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MineDeathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!event.getEntity().getWorld().getName().equals("mine")) return;

        Player p = event.getEntity();

        // set dropped XP to 50% of original
        int xp = ExperienceUtil.getExp(p);
        ExperienceUtil.changeExp(p, -(xp / 2));

        // remove any ores from inventory
        String[] oreNames = {"IRON_ORE", "GOLD_ORE", "DIAMOND_ORE", "EMERALD_ORE",
                "COAL_ORE", "REDSTONE_ORE", "LAPIS_ORE", "QUARTZ_ORE",
                "IRON_INGOT", "GOLD_INGOT", "DIAMOND", "EMERALD",
                "COAL", "REDSTONE", "LAPIS_LAZULI", "QUARTZ", "RAW_IRON", "RAW_GOLD"};


        // loops through the inventory and removes any ores
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (p.getInventory().getItem(i) == null) continue;

            String itemName = p.getInventory().getItem(i).getType().name();

            for (int j = 0; j < oreNames.length; j++) {
                if (itemName.equals(oreNames[j])) {
                    // custom hats are iron ore with custom model data - DONT DELETE!
                    if (p.getInventory().getItem(i).getItemMeta().getDisplayName().contains("Hat")) continue;

                    p.getInventory().setItem(i, null);
                    break;
                }
            }
        }

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou died in the mine!"));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou lost 50% of your XP and all ores in your inventory!"));
    }
}