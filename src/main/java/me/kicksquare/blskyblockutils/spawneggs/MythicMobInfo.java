package me.kicksquare.blskyblockutils.spawneggs;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MythicMobInfo {

    private final String mythicMobName;
    private final String spawnEggName;
    private final int minutesAlive;
    private final List<String> allowedWorlds;
    private final List<String> commandsOnDeath;

    public MythicMobInfo(String mythicMobName, String spawnEggName, int minutesAlive, List<String> allowedWorlds, List<String> commandsOnDeath) {
        this.mythicMobName = mythicMobName;
        this.spawnEggName = spawnEggName;
        this.minutesAlive = minutesAlive;
        this.allowedWorlds = allowedWorlds;
        this.commandsOnDeath = commandsOnDeath;
    }

    public String getMythicMobName() {
        return mythicMobName;
    }

    public String getSpawnEggName() {
        return spawnEggName;
    }

    public int getMinutesAlive() {
        return minutesAlive;
    }

    public List<String> getAllowedWorlds() {
        return allowedWorlds;
    }

    public List<String> getCommandsOnDeath() {
        return commandsOnDeath;
    }

    public ItemStack createSpawnEgg() {
        ItemStack spawnEgg = new ItemStack(Material.OCELOT_SPAWN_EGG);
        ItemMeta meta = spawnEgg.getItemMeta();
        meta.setDisplayName(spawnEggName);
        spawnEgg.setItemMeta(meta);
        return spawnEgg;
    }
}