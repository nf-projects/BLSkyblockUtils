package me.kicksquare.blskyblockutils.dungeon;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DungeonSpawnLocationUtil {
    private static final BLSkyblockUtils plugin = BLSkyblockUtils.getPlugin();

    public static void generateValidSpawnLocations() {
        int minX = plugin.getMainConfig().getInt("dungeon-minX");
        int minY = plugin.getMainConfig().getInt("dungeon-minY");
        int minZ = plugin.getMainConfig().getInt("dungeon-minZ");
        int maxX = plugin.getMainConfig().getInt("dungeon-maxX");
        int maxY = plugin.getMainConfig().getInt("dungeon-maxY");
        int maxZ = plugin.getMainConfig().getInt("dungeon-maxZ");

        World world = Bukkit.getWorld("dungeon");

        List<Location> spawnLocations = new ArrayList<>();
        int maxAttempts = 10000;
        int attempts = 0;

        // generate 1000 dungeon spawn locations
        while (spawnLocations.size() < 1000 && attempts < maxAttempts) {
            int x = ThreadLocalRandom.current().nextInt(minX, maxX);
            int y = ThreadLocalRandom.current().nextInt(minY, maxY);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ);
            Block block = world.getBlockAt(x, y, z);

            // a block is valid if a mob can spawn there:
            // - the block below must be solid
            // - the block itself and the block above must be air

            // block must be air
            if (!block.getType().toString().contains("AIR")) {
                continue;
            }

            // block above must be air
            if (!block.getRelative(0, 1, 0).getType().toString().contains("AIR")) {
                continue;
            }

            // block below must be solid
            if (!block.getRelative(0, -1, 0).getType().isSolid()) {
                continue;
            }

            // block below must not be a liquid
            if (block.getRelative(0, -1, 0).isLiquid()) {
                continue;
            }

            // block below must not be a fence or torch
            if (block.getRelative(0, -1, 0).getType().toString().contains("FENCE") || block.getRelative(0, -1, 0).getType().toString().contains("TORCH")) {
                continue;
            }

            spawnLocations.add(block.getLocation());

            attempts++;
        }

        plugin.validDungeonSpawnLocations = spawnLocations;
    }
}
