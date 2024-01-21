package me.kicksquare.blskyblockutils.capitols;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.nation.Nation;
import me.angeschossen.lands.api.player.LandPlayer;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Collection;

import static me.kicksquare.blskyblockutils.util.ColorUtil.colorize;

public class WarKillListener implements Listener {
    private final BLSkyblockUtils blSkyblockUtils;

    public WarKillListener(BLSkyblockUtils blSkyblockUtils) {
        this.blSkyblockUtils = blSkyblockUtils;
    }

    @EventHandler
    public void onWarKill(PlayerDeathEvent event) {
        War war = blSkyblockUtils.getCurrentWar();
        if (war == null) {
            return;
        }

        // only if it's a player kill
        if (event.getEntity().getKiller() == null || !(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        Player dying = event.getEntity();

        // only if the killer is in the war
        if (!(war.isAtWar(killer) && war.isAtWar(dying))) {
            return;
        }

        // the kill must have happened in the warzone
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld("world")));

        ProtectedRegion region = regions.getRegion(war.capitol.getRegionName());
        Location loc = killer.getLocation();

        // convert Bukkit location to WorldGuard location
        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(loc);

        if (!region.contains(wgLoc.getBlockX(), wgLoc.getBlockY(), wgLoc.getBlockZ())) {
            return;
        }

        // at this point, it's a valid kill
        // determine which team the killer is on
        LandsIntegration api = LandsIntegration.of(BLSkyblockUtils.getPlugin());
        LandPlayer landPlayer = api.getLandPlayer(killer.getUniqueId());
        Collection<? extends Land> lands = landPlayer.getLands();

        boolean found = false;
        if (!lands.isEmpty()) {
            for (Land land : lands) {
                Nation nation = land.getNation();
                if (nation != null) {
                    if (nation.equals(war.nationBlue)) {
                        war.addKillToBlue();
                        found = true;
                        break;
                    } else if (nation.equals(war.nationRed)) {
                        war.addKillToRed();
                        found = true;
                        break;
                    }
                }
            }
        }

        if (found) {
            // send a message to the killer
            killer.sendMessage(colorize("&e&lKILL&7 You killed an enemy at war! &9&l+1 &fKill   &a&l+50 &fNation Points"));
        } else {
            // should never happen
            killer.sendMessage(colorize("&cFatal: You killed a player in a warzone, but you are not in a nation that is at war! Report this to an admin!"));
        }
    }
}
