package me.kicksquare.blskyblockutils.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

import java.util.List;

public class MineSpawner {
    public static void attemptToSpawnMobInMine(List<Location> validSpawnLocations, int limit) {
        if (validSpawnLocations.size() == 0) {
            return;
        }

        // if there are already over 150 zombies, skeletons, and creepers combined in the world "mine", don't spawn more
        if (Bukkit.getWorld("mine").getEntitiesByClass(Zombie.class).size() +
                Bukkit.getWorld("mine").getEntitiesByClass(Skeleton.class).size() +
                Bukkit.getWorld("mine").getEntitiesByClass(Creeper.class).size() > limit) {
            return;
        }

        // get a random spawn location from spawnlist
        int randomIndex = (int) (Math.random() * validSpawnLocations.size());
        Location randomSpawnLocation = validSpawnLocations.get(randomIndex);

        // randomly select either zombie, skeleton, or creeper to spawn
        int randomMob = (int) (Math.random() * 3);
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
        }
    }
}
