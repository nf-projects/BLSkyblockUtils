package me.kicksquare.blskyblockutils.mine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class MineNetheriteListener implements Listener {
    @EventHandler
    public void onBlockMine(BlockBreakEvent e) {
        if(!e.getBlock().getWorld().getName().equals("nethermine")) return;

        if(e.getBlock().getBlockData().getMaterial() != Material.NETHERITE_BLOCK) return;

        // spawn a blaze shard in any air block next to the netherite block (so it doesn't get glitched inside)

        // find any air block touching the netherite block
        int x = e.getBlock().getX();
        int y = e.getBlock().getY();
        int z = e.getBlock().getZ();

        // check all 6 sides
        if(e.getBlock().getRelative(1, 0, 0).getType() == Material.AIR) {
            x++;
        } else if(e.getBlock().getRelative(-1, 0, 0).getType() == Material.AIR) {
            x--;
        } else if(e.getBlock().getRelative(0, 1, 0).getType() == Material.AIR) {
            y++;
        } else if(e.getBlock().getRelative(0, -1, 0).getType() == Material.AIR) {
            y--;
        } else if(e.getBlock().getRelative(0, 0, 1).getType() == Material.AIR) {
            z++;
        } else if(e.getBlock().getRelative(0, 0, -1).getType() == Material.AIR) {
            z--;
        } else {
            // no air block found
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ei drop blazeshard 1 nethermine " + x + " " + y + " " + z);
    }


    // failsafe - under no circumstances should a netherite block drop itself
    @EventHandler
    public void onBlockDrop(BlockBreakEvent e) {
        if(!e.getBlock().getWorld().getName().equals("nethermine")) return;

        if(e.getBlock().getBlockData().getMaterial() == Material.NETHERITE_BLOCK) {
            e.setDropItems(false);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if(!e.getEntity().getWorld().getName().equals("nethermine")) return;

        if(e.getEntity().getItemStack().getType() == Material.NETHERITE_BLOCK) {
            e.setCancelled(true);
        }
    }
}
