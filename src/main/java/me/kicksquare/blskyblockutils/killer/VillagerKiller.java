package me.kicksquare.blskyblockutils.killer;

import org.bukkit.entity.Villager;

public class VillagerKiller {
    public static void killVillagersEtc() {
        // go through all mobs, if they are zombie villagers, wandering traders, or trader llamas, kill them
        for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof org.bukkit.entity.ZombieVillager || entity instanceof org.bukkit.entity.WanderingTrader || entity instanceof org.bukkit.entity.TraderLlama) {
                    
                    entity.remove();
                }
            }
        }
    }
}
