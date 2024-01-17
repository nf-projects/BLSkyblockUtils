package me.kicksquare.blskyblockutils.capitols.buffs;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.nation.Nation;
import me.angeschossen.lands.api.player.LandPlayer;
import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import me.kicksquare.blskyblockutils.capitols.Capitol;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public class BuffUtil {
    static BLSkyblockUtils plugin = BLSkyblockUtils.getPlugin();
    static LandsIntegration api = LandsIntegration.of(plugin);

    public static Capitol[] getPlayerCapitols(Player player) {
        LandPlayer landPlayer = api.getLandPlayer(player.getUniqueId());

        if (landPlayer == null) {
            return null;
        }

        Collection<? extends Land> lands = landPlayer.getLands();
        if (!lands.isEmpty()) {
            for (Land land : lands) {
                if(land.getOwnerUID() != player.getUniqueId()) {
                    // player needs to be the OWNER of the land, not just a member/trusted
                    continue;
                }

                Nation nation = land.getNation();
                if (nation != null) {
                    Capitol[] capitols = plugin.getCapitalControllerManager().getCapitolsFromController(nation.getName());

                    if (capitols != null) {
                        return capitols;
                    }
                }
            }
        }

        return null;
    }

    public static Capitol[] getLocationCapitols(Location location) {
        Area locationArea = api.getArea(location);

        if (locationArea == null) {
            return null;
        }

        Land land = locationArea.getLand();

        Nation nation = land.getNation();

        if (nation == null) {
            return null;
        }

        Capitol[] capitols = plugin.getCapitalControllerManager().getCapitolsFromController(nation.getName());

        return capitols;
    }
}
