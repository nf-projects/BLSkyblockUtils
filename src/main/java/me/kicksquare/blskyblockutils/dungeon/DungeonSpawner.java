package me.kicksquare.blskyblockutils.dungeon;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DungeonSpawner {
    public static void attemptToSpawnMobInDungeon(List<Location> validSpawnLocations, int limit, MythicBukkit mythicBukkit, BLSkyblockUtils plugin) {
        if (validSpawnLocations.size() == 0) {
            return;
        }

        Collection<ActiveMob> activeMobs = mythicBukkit.getMobManager().getActiveMobs();

        int mythicMobsInDungeon = 0;

        for (ActiveMob activeMob : activeMobs) {
            if (activeMob.getLocation().getWorld().getName().equals("dungeon")) {
                mythicMobsInDungeon++;
            }
        }

        if (mythicMobsInDungeon > limit) {
            return;
        }

        // get a random spawn location from spawnlist
        int randomIndex = (int) (Math.random() * validSpawnLocations.size());
        Location randomSpawnLocation = validSpawnLocations.get(randomIndex);

        // make it 3 blocks higher so mobs don't spawn in the floor
        randomSpawnLocation.setY(randomSpawnLocation.getY() + 3);

        // constant: list of possible mob type names
        List<String> mobTypes = plugin.getMainConfig().getStringList("dungeon-mobs");

        // randomly select a mob type to spawn
        int randomMobTypeIndex = (int) (Math.random() * mobTypes.size());
        String randomMobType = mobTypes.get(randomMobTypeIndex);

        mythicBukkit.getMobManager().spawnMob(randomMobType, randomSpawnLocation);
    }

    public static void killOldDungeonMobs(MythicBukkit mythicBukkit) {
        // kills any mythic mobs that have been alive for longer than 10 minutes
        // only in the dungeon world
        Collection<ActiveMob> activeMobs = mythicBukkit.getMobManager().getActiveMobs();

        for (ActiveMob activeMob : activeMobs) {
            if (activeMob.getLocation().getWorld().getName().equals("dungeon")) {
                if (activeMob.getAliveTime() > 10 * 60 * 1000) {
                    activeMob.getEntity().remove();
                }
            }
        }
    }
}
