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
        int minX = 11;
        int minY = 70;
        int minZ = -40;
        int maxX = 80;
        int maxY = 74;
        int maxZ = 39;

        World world = Bukkit.getWorld("dungeon");

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

        plugin.validDungeonSpawnLocations = spawnLocations;
    }
}
