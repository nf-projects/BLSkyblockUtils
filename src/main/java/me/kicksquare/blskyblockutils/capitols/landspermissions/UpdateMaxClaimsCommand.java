package me.kicksquare.blskyblockutils.capitols.landspermissions;

import me.kicksquare.blskyblockutils.BLSkyblockUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class UpdateMaxClaimsCommand implements CommandExecutor {
    private final BLSkyblockUtils plugin;
    private final LuckPerms luckPermsApi;

    public UpdateMaxClaimsCommand(BLSkyblockUtils blSkyblockUtils, LuckPerms luckPermsApi) {
        this.plugin = blSkyblockUtils;
        this.luckPermsApi = luckPermsApi;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        try {
            if (!(commandSender instanceof Player) && !commandSender.hasPermission("blskyblockutils.updatemaxclaims")) {
                commandSender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            if (args.length < 2 || args.length > 4) {
                commandSender.sendMessage("Usage: /updatemaxclaims <player> <view|set|add|remove> [amount] [--buycraft]");
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);
            if (targetPlayer == null) {
                commandSender.sendMessage("Player not found.");
                return true;
            }

            User user = luckPermsApi.getUserManager().getUser(targetPlayer.getUniqueId());
            if (user == null) {
                commandSender.sendMessage("Could not retrieve user data for player.");
                return true;
            }

            boolean isBuyCraft = Arrays.asList(args).contains("--buycraft");
            if (isBuyCraft) {
                args = Arrays.stream(args).filter(arg -> !"--buycraft".equals(arg)).toArray(String[]::new);
            }

            String currentPermissionPrefix = "lands.chunks.";
            String action = args[1].toLowerCase();

            if ("view".equals(action)) {
                int currentMaxClaims = getCurrentMaxClaims(user, currentPermissionPrefix);
                commandSender.sendMessage("Current max claims for player " + targetPlayer.getName() + ": " + currentMaxClaims);
                return true;
            }

            if (args.length < 3 && !"view".equals(action)) {
                commandSender.sendMessage("Amount is required for action " + action);
                return true;
            }

            int amount = Integer.parseInt(args[2]);
            int currentMaxClaims = getCurrentMaxClaims(user, currentPermissionPrefix);

            if ("add".equals(action) && isBuyCraft) {
                Map<String, Integer> rankClaims = new HashMap<>();
                rankClaims.put("bee", 5);
                rankClaims.put("rabbit", 10);
                rankClaims.put("turtle", 15);
                rankClaims.put("parrot", 20);
                rankClaims.put("panda", 25);
                rankClaims.put("tiger", 30);

                // Find the highest rank of the user and its claim chunks
                int highestRankClaims = user.getNodes(NodeType.INHERITANCE).stream()
                        .map(InheritanceNode::getGroupName)
                        .filter(rankClaims::containsKey)
                        .mapToInt(rankClaims::get)
                        .max()
                        .orElse(0);

                // user has default: highestRankClaims is 0
                // user adds 5: amount is 5, highestRankClaims is 0, we SHOULD add 5
                // scenario 2: user has turtle: highestRankClaims is 15
                // user adds 10 (rabbit): amount is 10, highestRankClaims is 15, we SHOULD add 0 because you can't downgrade
                // scenario 2: user has turtle: highestRankClaims is 15
                // user adds 25 (panda): amount is 25, highestRankClaims is 15, we SHOULD add 10

                if (amount > highestRankClaims) {

                    amount -= highestRankClaims;
                } else {
                    amount = 0;
                }
            }

            int newMaxClaims = calculateNewMaxClaims(currentMaxClaims, action, amount);
            if (newMaxClaims < 0) {
                commandSender.sendMessage("Invalid action. Use 'view', 'set', 'add', or 'remove'.");
                return true;
            }

            // Clear old permissions and set new permanent permission
            user.data().clear(node -> node.getKey().startsWith(currentPermissionPrefix));
            Node node = PermissionNode.builder(currentPermissionPrefix + newMaxClaims).build();
            user.data().add(node);

            luckPermsApi.getUserManager().saveUser(user);
            commandSender.sendMessage("Max claims updated to " + newMaxClaims + " for player " + targetPlayer.getName() + ".");
        } catch (NumberFormatException e) {
            commandSender.sendMessage("Invalid number format. Please ensure you provide a valid number for the amount.");
        } catch (Exception e) {
            commandSender.sendMessage("An error occurred while processing the command. Please check the server logs.");
            plugin.getLogger().log(Level.SEVERE, "An error occurred while executing the updatemaxclaims command", e);
        }
        return true;
    }

    private int getCurrentMaxClaims(User user, String currentPermissionPrefix) {
        return user.getNodes().stream()
                .filter(node -> node.getKey().startsWith(currentPermissionPrefix))
                .mapToInt(node -> Integer.parseInt(node.getKey().substring(currentPermissionPrefix.length())))
                .max()
                .orElse(0);
    }

    private int calculateNewMaxClaims(int currentMaxClaims, String action, int amount) {
        switch (action) {
            case "set":
                return amount;
            case "add":
                return currentMaxClaims + amount;
            case "remove":
                return Math.max(currentMaxClaims - amount, 0); // Ensure it's not negative
            default:
                return -1; // Indicate invalid action
        }
    }
}
