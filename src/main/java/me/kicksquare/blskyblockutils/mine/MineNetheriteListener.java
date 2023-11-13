package me.kicksquare.blskyblockutils.mine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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

        System.out.println("Spawned blazeshard at " + x + " " + y + " " + z + " original block was broken at " + e.getBlock().getX() + " " + e.getBlock().getY() + " " + e.getBlock().getZ());
    }
}
