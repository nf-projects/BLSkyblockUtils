package me.kicksquare.blskyblockutils.mine;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MineSoundListener implements Listener {
    @EventHandler
    public void onMine(BlockBreakEvent event) {
        // we only want this to happen in the mine
        if (!(event.getBlock().getWorld().getName().equals("mine") || event.getBlock().getWorld().getName().equals("nethermine"))) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        // if the mined block is an ore
        Sound sound;
        if (event.getBlock().getType().toString().contains("ORE")) {
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        } else {
            sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
        // pitch is between 1.7 and 2.0 inclusive
        Random rand = new Random();
        float randomFloat = rand.nextFloat() * 0.5f + 1.7f;
        float pitch = Math.round(randomFloat * 10.0f) / 10.0f;
        event.getPlayer().playSound(event.getBlock().getLocation(), sound, 1, pitch);

        if (event.getBlock().getType().toString().contains("ORE")) {
            // calculate special ore drops
            // 5% chance of getting 3x the ore drops
            // 1% chance of getting 10x the ore drops


            Material itemDrop;
            switch (event.getBlock().getType()) {
                case IRON_ORE:
                    itemDrop = Material.RAW_IRON;
                    break;
                case GOLD_ORE:
                    itemDrop = Material.RAW_GOLD;
                    break;
                case DIAMOND_ORE:
                    itemDrop = Material.DIAMOND;
                    break;
                case EMERALD_ORE:
                    itemDrop = Material.EMERALD;
                    break;
                case REDSTONE_ORE:
                    itemDrop = Material.REDSTONE;
                    break;
                case LAPIS_ORE:
                    itemDrop = Material.LAPIS_LAZULI;
                    break;
                case COAL_ORE:
                    itemDrop = Material.COAL;
                    break;
                default:
                    itemDrop = Material.AIR;
                    break;
            }

            // random number between 0 and 100 inclusive
            int randomInt = rand.nextInt(101);
            if (randomInt == 6) {
                event.getPlayer().sendMessage("You found a very rare ore! 10x drops!");

                int amount = event.getBlock().getDrops().iterator().next().getAmount() * 9;
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(itemDrop, amount));

                event.getPlayer().playSound(event.getBlock().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else if (randomInt <= 5) {
                event.getPlayer().sendMessage("You found a rare ore! 3x drops!");

                int amount = event.getBlock().getDrops().iterator().next().getAmount() * 2;
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(itemDrop, amount));

                event.getPlayer().playSound(event.getBlock().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }
    }
}
