package me.kicksquare.blskyblockutils.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

import java.util.List;

public class OverworldMineSpawner {
    public static void attemptToSpawnMobInOverworldMine(List<Location> validOverworldMineSpawnLocations, int limit) {
        if (validOverworldMineSpawnLocations.size() == 0) {
            return;
        }

        // if there are already over 50 zombies, skeletons, and creepers combined in the world "mine", don't spawn more
        if (Bukkit.getWorld("mine").getEntitiesByClass(Zombie.class).size() +
                Bukkit.getWorld("mine").getEntitiesByClass(Skeleton.class).size() +
                Bukkit.getWorld("mine").getEntitiesByClass(Creeper.class).size() > limit) {
            return;
        }

        // get a random spawn location from spawnlist
        int randomIndex = (int) (Math.random() * validOverworldMineSpawnLocations.size());
        Location randomSpawnLocation = validOverworldMineSpawnLocations.get(randomIndex);

        // to increase the concentration of mobs at the bottom, there is a 35% chance of re-rolling a new spanw location
        // if the chosen location has a y > 60
        if (randomSpawnLocation.getY() > 60 && Math.random() < 0.35) {
            attemptToSpawnMobInOverworldMine(validOverworldMineSpawnLocations, limit);
            return;
        }

        // randomly select either zombie, skeleton, or creeper to spawn
        int randomMob = (int) (Math.random() * 4);
        switch (randomMob) {
            case 0:
                Bukkit.getWorld("mine").spawn(randomSpawnLocation, Zombie.class);
                break;
            case 1:
                Bukkit.getWorld("mine").spawn(randomSpawnLocation, Skeleton.class);
                break;
            case 2:
                Bukkit.getWorld("mine").spawn(randomSpawnLocation, Creeper.class);
                break;
            case 3:
                // if y is lower than 60, spawn a spider. otherwise, just spawn a zombie
                // (cant spawn spiders at the top because they would be able to clim up to /mine spawn)
                if (randomSpawnLocation.getY() < 60) {
                    Bukkit.getWorld("mine").spawn(randomSpawnLocation, org.bukkit.entity.Spider.class);
                } else {
                    Bukkit.getWorld("mine").spawn(randomSpawnLocation, Zombie.class);
                }
        }
    }
}
