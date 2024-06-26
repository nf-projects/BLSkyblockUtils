package me.kicksquare.blskyblockutils.mine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class MineGoldnuggetListener implements Listener {
    @EventHandler
    public void onBlockMine(BlockBreakEvent e) {
        if (!e.getBlock().getWorld().getName().equals("nethermine")) return;

        if (e.getBlock().getBlockData().getMaterial() != Material.NETHER_GOLD_ORE) return;

        // spawn nethergold in any air block next to the nether gold block (so it doesn't get glitched inside)

        // find any air block touching the netherite block
        int x = e.getBlock().getX();
        int y = e.getBlock().getY();
        int z = e.getBlock().getZ();

        // check all 6 sides
        if (e.getBlock().getRelative(1, 0, 0).getType() == Material.AIR) {
            x++;
        } else if (e.getBlock().getRelative(-1, 0, 0).getType() == Material.AIR) {
            x--;
        } else if (e.getBlock().getRelative(0, 1, 0).getType() == Material.AIR) {
            y++;
        } else if (e.getBlock().getRelative(0, -1, 0).getType() == Material.AIR) {
            y--;
        } else if (e.getBlock().getRelative(0, 0, 1).getType() == Material.AIR) {
            z++;
        } else if (e.getBlock().getRelative(0, 0, -1).getType() == Material.AIR) {
            z--;
        } else {
            // no air block found
            return;
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mmoitems drop ACCESSORY NETHERGOLD nethermine " + x + " " + y + " " + z + " 1 2-6 0");
    }
}
