package me.kicksquare.blskyblockutils.spawners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SpawnerTick {
    public static void tick(BLSkyblockUtils plugin) {
//        For refrence, this is the relevant config section
//
//# -----------------------------------
//# SPAWNERS
//# -----------------------------------
//
//# world, region, coords, mob type, max num in region
//        spawners:
//        - "spawn|spawngrinder1|248,88,133|sheep|3"
//                - "spawn|spawngrinder1|248,88,133|pig|3"
//                - "spawn|spawngrinder1|248,88,133|cow|3"

        List<String> spawners = plugin.getMainConfig().getStringList("spawners");


        for (String spawner : spawners) {
            String[] spawnerParts = spawner.split("\\|");
            String worldName = spawnerParts[0];
            String regionName = spawnerParts[1];
            String[] coords = spawnerParts[2].split(",");
            String mobType = spawnerParts[3];
            int maxNum = Integer.parseInt(spawnerParts[4]);

            // get the worldguard region instance
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));

            if (regions == null) {

                continue;
            }

            ProtectedRegion region = regions.getRegion(regionName);

            if (region == null) {

                continue;
            }

            // get the number of that mob in the region
            int numMobs = 0;
            for (Entity entity : Bukkit.getWorld(worldName).getEntities()) {
                if (entity.getType() == EntityType.valueOf(mobType)) {
                    if (region.contains(BukkitAdapter.asBlockVector(entity.getLocation()))) {
                        numMobs++;
                    }
                }
            }

            // if there are too many mobs, don't spawn any more
            if (numMobs >= maxNum) {

                continue;
            }

            // spawn a mob
            // first, make some changes to the coords
            // 1: add a random integer between -2 and 2 to each coord
            // 2: add a random delay between 1 and 4 seconds to the spawn
            int x = Integer.parseInt(coords[0]) + (int) (Math.random() * 5) - 2;
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]) + (int) (Math.random() * 5) - 2;
            int delay = (int) (Math.random() * 3) + 1;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Bukkit.getWorld(worldName).spawnEntity(Bukkit.getWorld(worldName).getBlockAt(x, y, z).getLocation(), EntityType.valueOf(mobType));
            }, delay * 20L);

        }
    }

    public static void tickLadderMobs(BLSkyblockUtils plugin) {
        // kills any mobs that are 1) in the spawn world 2) are in one of the regions from the spawn section above and 3) are on a ladder
        List<String> spawners = plugin.getMainConfig().getStringList("spawners");

        for (String spawner : spawners) {
            String[] spawnerParts = spawner.split("\\|");
            String worldName = spawnerParts[0];
            String regionName = spawnerParts[1];

            // get the worldguard region instance
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));

            if (regions == null) {

                continue;
            }

            ProtectedRegion region = regions.getRegion(regionName);

            if (region == null) {

                continue;
            }

            // kill any mobs in the region that are on a ladder
            for (Entity entity : Bukkit.getWorld(worldName).getEntities()) {
                if (region.contains(BukkitAdapter.asBlockVector(entity.getLocation()))) {
                    // check if they're on a ladder rn
                    if (entity.getLocation().getBlock().getType().toString().contains("LADDER")) {
                        entity.remove();
                    }
                }
            }
        }
    }
}
