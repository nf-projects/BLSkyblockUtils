package me.kicksquare.blskyblockutils.onboarding.listener;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;

public class FullDiamondEvent implements Listener {
    private static BLSkyblockUtils plugin = BLSkyblockUtils.getPlugin();

    @EventHandler
    public void onCraftingTableClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.WORKBENCH && event.getSlotType() == InventoryType.SlotType.RESULT) {
            checkInventory(player);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        checkInventory(player);
    }

    private void checkInventory(Player p) {
        Bukkit.broadcastMessage("1");

        // wait 1 second to make sure the item is in the inventory
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage("2");
            // check if they have full diamond armor (in their inventory and/or equipped)
            ItemStack[] armorContents = p.getInventory().getArmorContents();
            ItemStack[] inventoryContents = p.getInventory().getContents();

            boolean hasHelmet = false;
            boolean hasChestplate = false;
            boolean hasLeggings = false;
            boolean hasBoots = false;

            for (ItemStack item : armorContents) {
                if (item != null) {
                    if (item.getType() == Material.DIAMOND_HELMET) {
                        hasHelmet = true;
                    } else if (item.getType() == Material.DIAMOND_CHESTPLATE) {
                        hasChestplate = true;
                    } else if (item.getType() == Material.DIAMOND_LEGGINGS) {
                        hasLeggings = true;
                    } else if (item.getType() == Material.DIAMOND_BOOTS) {
                        hasBoots = true;
                    }
                }
            }

            for (ItemStack item : inventoryContents) {
                if (item != null) {
                    if (item.getType() == Material.DIAMOND_HELMET) {
                        hasHelmet = true;
                    } else if (item.getType() == Material.DIAMOND_CHESTPLATE) {
                        hasChestplate = true;
                    } else if (item.getType() == Material.DIAMOND_LEGGINGS) {
                        hasLeggings = true;
                    } else if (item.getType() == Material.DIAMOND_BOOTS) {
                        hasBoots = true;
                    }
                }
            }

            if (hasHelmet && hasChestplate && hasLeggings && hasBoots) {
                // has full diamond
                p.sendMessage("DEBUG: has full diamond");

                // for reference
                // CREATE TABLE onboarding_achievements (
                //  id INT PRIMARY KEY AUTO_INCREMENT,
                //  event_type VARCHAR(35),
                //  player_uuid VARCHAR(40),
                //  event_timestamp TIMESTAMP(0)
                //);
                String query = "INSERT INTO onboarding_achievements (event_type, player_uuid, event_timestamp) VALUES (?, ?, ?)";
                Object event_type = "full_diamond";
                Object player_uuid = p.getUniqueId().toString();
                // Date object, not any more exact than seconds (timestamp(0))
                Object event_timestamp = new java.sql.Timestamp(System.currentTimeMillis());

                plugin.getDatabase().update(query, event_type, player_uuid, event_timestamp);
            }
        }, 20L);


    }
}
