package me.kicksquare.blskyblockutils.mine;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MineSpawnLocationUtil {
    private static final BLSkyblockUtils plugin = BLSkyblockUtils.getPlugin();

    public static void generateValidSpawnLocations() {
        int minX = -118;
        int minY = 1;
        int minZ = -95;
        int maxX = -1;
        int maxY = 105;
        int maxZ = 38;

        World world = Bukkit.getWorld("mine");

        List<Location> spawnLocations = new ArrayList<>();
        int maxAttempts = 50000;
        int attempts = 0;

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

        System.out.println("Generated " + spawnLocations.size() + " valid spawn locations in " + attempts + " attempts");
        plugin.validMineSpawnLocations = spawnLocations;
    }
}
