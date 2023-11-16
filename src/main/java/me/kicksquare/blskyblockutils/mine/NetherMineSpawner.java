package me.kicksquare.blskyblockutils.mine;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.spawneggs.BossReward;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

import java.awt.geom.Point2D;
import java.util.*;

public class NetherMineSpawner {
    public static void attemptToSpawnMobInNetherMine(List<Location> validNetherMineSpawnLocations, BLSkyblockUtils plugin, MythicBukkit mythicBukkit) {
        if (validNetherMineSpawnLocations.size() == 0) {
            Bukkit.getLogger().info("[Nether Mine Spawner] No valid spawn locations found.");
            return;
        }

        // read the mobs/spawn limits from config
        Map<String, Integer> netherMineMobs = new HashMap<String, Integer>();

        List<String> rawConf = plugin.getMainConfig().getStringList("nether-mine-mobs");


        for (String mob : rawConf) {
            String mobName = mob.split("\\|")[0];
            Integer spawnLimit = Integer.valueOf(mob.split("\\|")[1]); // ¯\_(ツ)_/¯

            netherMineMobs.put(mobName, spawnLimit);
        }

        // get a random spawn location from spawnlist
        int randomIndex = (int) (Math.random() * validNetherMineSpawnLocations.size());
        Location randomSpawnLocation = validNetherMineSpawnLocations.get(randomIndex);

        // go through all mobs. if the limit for that mob hasn't been reached, spawn it and return
        for (Map.Entry<String, Integer> entry : netherMineMobs.entrySet()) {
            String mobName = entry.getKey();
            Integer spawnLimit = entry.getValue();

            Collection<ActiveMob> activeMobs = mythicBukkit.getMobManager().getActiveMobs();

            int mobCounterThisType = 0;

            for (ActiveMob activeMob : activeMobs) {
                if (activeMob.getLocation().getWorld().getName().equals("nethermine")) {
                    if (activeMob.getType().getInternalName().equals(mobName)) {
                        mobCounterThisType++;
                    }
                }
            }

            if (mobCounterThisType < spawnLimit) {
                mythicBukkit.getMobManager().spawnMob(mobName, randomSpawnLocation);
                return;
            }
        }
    }
}
