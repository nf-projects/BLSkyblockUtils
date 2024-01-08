package me.kicksquare.blskyblockutils.capitols.buffs;

import dev.rosewood.rosestacker.event.PreStackedSpawnerSpawnEvent;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.capitols.Capitol;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static me.kicksquare.blskyblockutils.capitols.buffs.BuffUtil.getLocationCapitols;
import static me.kicksquare.blskyblockutils.capitols.buffs.BuffUtil.getPlayerCapitols;

public class BuffManager implements Listener {
    static BLSkyblockUtils plugin = BLSkyblockUtils.getPlugin();

    public static void tickNationBuffs() {

        // handle potion effects
        for (Player player : Bukkit.getOnlinePlayers()) {
            Capitol[] playerCapitols = getPlayerCapitols(player);
            if (playerCapitols != null) {
                for (Capitol capitol : playerCapitols) {
                    applyBuff(player, capitol);
                }
            }
        }
    }

    private static void applyBuff(Player player, Capitol capitol) {
        String buff1 = plugin.getMainConfig().getString("capitol_" + capitol.name().toLowerCase() + "_buff_1");
        String buff2 = plugin.getMainConfig().getString("capitol_" + capitol.name().toLowerCase() + "_buff_2");
        parseAndApplyBuff(player, buff1);
        parseAndApplyBuff(player, buff2);
    }

    private static void parseAndApplyBuff(Player player, String buff) {

        if (buff == null || buff.isEmpty()) return;

        String[] parts = buff.split("\\|");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid buff format: " + buff);
        }
        ;

        String buffType = parts[0];

        switch (buffType) {
            case "CONSOLE_COMMAND":
                // Assuming there's a method in your plugin to handle console commands
                executeConsoleCommand(player, buff.replace(buffType + "|", ""));
                break;
            case "POTION_EFFECT":
            case "POTION_EFFECT_PERMANENT":
                applyPotionEffect(player, parts);
                break;
            case "DOUBLE_DROP":
                // this is handled in the event listener below
                break;
            case "DOUBLE_SPAWNS":
                // this is also handled in the event listener below
                break;
            case "PERMISSION":
                // basically the same thing as CONSOLE_COMMAND, but the command is /lp user %player% permission settemp [permission] true 1d
                addTemporaryPermission(player, buff.replace(buffType + "|", ""));
                break;
            default:
                throw new IllegalArgumentException("Invalid buff type: " + buffType);
        }
    }

    private static void applyPotionEffect(Player player, String[] parts) {

        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid potion effect format: " + parts[0]);
        }

        PotionEffectType type = PotionEffectType.getByName(parts[1]);
        if (type == null) {
            throw new IllegalArgumentException("Invalid potion effect type: " + parts[1]);
        }

        int amplifier = Integer.parseInt(parts[2]) - 1; // Potion levels start at 0 in code

        if (parts[0].endsWith("_PERMANENT")) {
            // Apply the effect permanently (i.e., until it is explicitly removed)
            PotionEffect effect = new PotionEffect(type, 20 * 10, amplifier, true, false);
            player.addPotionEffect(effect, true);
        } else {
            // Apply the effect only if the player is in a chunk claimed by their nation
            Capitol[] locationCapitols = getLocationCapitols(player.getLocation());
            if (locationCapitols != null) {
                // Check if the nation controlling the location is one of the nations that the player is a member of
                boolean hasBuff = false;
                for (Capitol capitol : locationCapitols) {
                    if (Arrays.asList(getPlayerCapitols(player)).contains(capitol)) {
                        hasBuff = true;
                        break;
                    }
                }
                if (hasBuff) {
                    // Apply the effect for 10 seconds since the method is called every 10 seconds
                    PotionEffect effect = new PotionEffect(type, 20 * 10, amplifier, true, false);
                    player.addPotionEffect(effect, true);
                }
            }
        }
    }

    static void executeConsoleCommand(Player p, String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()));
    }

    public static void addTemporaryPermission(Player player, String permission) {
        User user = plugin.getLuckPermsApi().getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            Node node = PermissionNode.builder(permission)
                    .expiry(1, TimeUnit.DAYS)
                    .build();

            user.data().add(node);
            plugin.getLuckPermsApi().getUserManager().saveUser(user);
        } else {
            plugin.getLogger().warning("Could not find LuckPerms user for " + player.getName());
        }
    }

    // for double spawner chance
    @EventHandler
    public void onMobSpawn(PreStackedSpawnerSpawnEvent event) {
        if (event.isCancelled()) return;

        Location eventLocation = event.getStack().getLocation();
        Capitol[] locationCapitols = getLocationCapitols(eventLocation);

        if (locationCapitols != null) {
            for (Capitol capitol : locationCapitols) {
                String doubleSpawnBuff = plugin.getMainConfig().getString("capitol_" + capitol.name().toLowerCase() + "_buff_2");
                if (doubleSpawnBuff != null && doubleSpawnBuff.startsWith("DOUBLE_SPAWNS")) {
                    String[] parts = doubleSpawnBuff.split("\\|");
                    if (parts.length < 2) {
                        throw new IllegalArgumentException("Invalid buff format: " + doubleSpawnBuff);
                    }
                    double chance = Double.parseDouble(parts[1]);
                    if (Math.random() < chance) {
                        // Clone the entity and spawn it
                        event.setSpawnAmount(event.getSpawnAmount() * 2);
                    }
                }
            }
        }
    }

    // for double drop chance
    @EventHandler
    public void onItemDropFromMine(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();
        Capitol[] locationCapitols = getLocationCapitols(blockLocation);

        // Check if the player is using Silk Touch; if so, do not apply double drops
        if (player.getInventory().getItemInMainHand() != null) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.hasItemMeta()) {
                if (tool.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
                    return;
                }
            }
        }

        // Check if the player is using a Fortune pickaxe; if so, do not apply double drops
        if (player.getInventory().getItemInMainHand() != null) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.hasItemMeta()) {
                if (tool.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
                    return;
                }
            }
        }

        // Check if the player is in creative mode; if so, do not apply double drops
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (locationCapitols != null) {
            for (Capitol capitol : locationCapitols) {
                String doubleDropBuff = plugin.getMainConfig().getString("capitol_" + capitol.name().toLowerCase() + "_buff_1");
                if (doubleDropBuff != null && doubleDropBuff.startsWith("DOUBLE_DROP")) {
                    String[] parts = doubleDropBuff.split("\\|");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Invalid buff format: " + doubleDropBuff);
                    }

                    String[] ores = parts[1].split(",");
                    Material blockType = event.getBlock().getType();
                    for (String ore : ores) {
                        if (blockType == Material.getMaterial(ore.trim())) {
                            // Drop the block's item again
                            Collection<ItemStack> drops = event.getBlock().getDrops();
                            for (ItemStack drop : drops) {
                                blockLocation.getWorld().dropItemNaturally(blockLocation, drop);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

}
