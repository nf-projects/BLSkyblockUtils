package me.kicksquare.blskyblockutils.capitols.landspermissions;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LandsMaxClaimsListener implements Listener {
    private BLSkyblockUtils plugin;
    private LuckPerms luckPermsApi;

    public LandsMaxClaimsListener(BLSkyblockUtils blSkyblockUtils) {
        this.plugin = blSkyblockUtils;
        this.luckPermsApi = plugin.getLuckPermsApi();
    }

    @EventHandler
    public void playerFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getLuckPermsApi().getUserManager().getUser(event.getPlayer().getUniqueId());
        if (user == null) {
            System.out.println("[login debug] User is null for " + player.getName());
            return;
        };

        if ((!player.hasPlayedBefore()) || getCurrentLandsChunksPermission(player) == 0) {
            // either first join or no lands.chunks permission set
            // either way, we should set it to the highest rank's permission

            List<String> ranks = getPlayerGroups(player);

            Map<String, Integer> rankToClaims = new HashMap<>();
            rankToClaims.put("bee", 20);
            rankToClaims.put("rabbit", 25);
            rankToClaims.put("turtle", 30);
            rankToClaims.put("parrot", 35);
            rankToClaims.put("panda", 40);
            rankToClaims.put("tiger", 45);

            // get the highest rank the user has ("ranks" may also contain groups that aren't part of rankToClaims, such as ranks from other gamemodes)
            int highestRankClaims = ranks.stream()
                    .filter(rankToClaims::containsKey)
                    .mapToInt(rankToClaims::get)
                    .max()
                    .orElse(0);

            if (highestRankClaims == 0) {
                // no rank found, set to 15 claims
                setLandsChunksPermission(player, 15);
                System.out.println("[login debug] No rank found! Set lands.chunks permission to 15 for " + player.getName());
                System.out.println("[login debug] Ranks: " + ranks.toString());
            } else {
                // set to the highest rank's claims
                setLandsChunksPermission(player, highestRankClaims);
                System.out.println("[login debug] Rank found! Set lands.chunks permission to " + highestRankClaims + " for " + player.getName());
                System.out.println("[login debug] Ranks: " + ranks.toString());
            }
        } else {
            System.out.println("[login debug] User has played before and has lands.chunks permission set");
        }
    }

    public List<String> getPlayerGroups(Player player) {
        User user = luckPermsApi.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            // return empty list
            return Collections.emptyList();
        }

        return user.getNodes(NodeType.INHERITANCE).stream()
                .map(inheritanceNode -> inheritanceNode.getGroupName())
                .collect(Collectors.toList());
    }


    public Integer getCurrentLandsChunksPermission(Player player) {
        User user = luckPermsApi.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return null;
        }

        String currentPermissionPrefix = "lands.chunks.";
        return user.getNodes().stream()
                .filter(node -> node.getKey().startsWith(currentPermissionPrefix))
                .mapToInt(node -> Integer.parseInt(node.getKey().substring(currentPermissionPrefix.length())))
                .max()
                .orElse(0);
    }

    public void setLandsChunksPermission(Player player, int chunks) {
        User user = luckPermsApi.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return; // Exit if the user is not found
        }

        String currentPermissionPrefix = "lands.chunks.";
        // Remove the old permission
        user.data().clear(node -> node.getKey().startsWith(currentPermissionPrefix));
        // Add the new permission
        user.data().add(PermissionNode.builder(currentPermissionPrefix + chunks).build());
        luckPermsApi.getUserManager().saveUser(user);
    }
}
