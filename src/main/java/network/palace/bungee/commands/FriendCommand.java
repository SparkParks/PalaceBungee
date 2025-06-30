package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.utils.FriendUtil;
import network.palace.bungee.utils.MiscUtil;

/**
 * Represents a command that manages friend-related actions for a player.
 * <p>
 * The <code>FriendCommand</code> allows players to perform various friend-related tasks,
 * such as adding friends, removing friends, teleporting to friends, managing friend requests,
 * and viewing friend lists.
 * </p>
 *
 * <p>
 * Supported subcommands:
 * <ul>
 *   <li><b>help</b>: Displays the friend command help menu.</li>
 *   <li><b>list</b>: Lists the player's friends. Optionally, the page number can be provided as the second argument.</li>
 *   <li><b>toggle</b>: Toggles the friend request functionality on or off for the player.</li>
 *   <li><b>requests</b>: Displays a list of incoming friend requests.</li>
 *   <li><b>tp</b>: Teleports the player to a friend. Requires the friend's username as the second argument.</li>
 *   <li><b>add</b>: Sends a friend request to another player. Requires the player's username as the second argument.</li>
 *   <li><b>remove</b>: Removes a player from the friend list. Requires the player's username as the second argument.</li>
 *   <li><b>accept</b>: Accepts a pending friend request. Requires the player's username as the second argument.</li>
 *   <li><b>deny</b>: Denies a pending friend request. Requires the player's username as the second argument.</li>
 * </ul>
 * </p>
 *
 * <p>
 * If an unrecognized or incomplete command is provided, the help menu will be displayed by default.
 * </p>
 *
 * <p><b>Usage:</b> This command is executed with the prefix "friend" or "f".</p>
 *
 * <p><b>Notes:</b></p>
 * <ul>
 *   <li>All subcommands and arguments are case-insensitive.</li>
 *   <li>When listing friends, if an invalid page number is specified, the first page will be displayed.</li>
 *   <li>Friend request toggle states are persisted via a database handler.</li>
 *   <li>Teleportation will only function if the target player is online and is on the command sender's friend list.</li>
 * </ul>
 */
public class FriendCommand extends PalaceCommand {

    /**
     * Constructs a new {@code FriendCommand} instance with the command name "friend"
     * and the alias "f".
     * <p>
     * The {@code FriendCommand} is designed to allow players to manage in-game friendships.
     * It inherits functionality from the {@code PalaceCommand} base class, enabling it to
     * be used within the command execution framework of the system.
     * </p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Provides a primary name ("friend") and an alias ("f") for convenient access.</li>
     *   <li>Integrates seamlessly into the PalaceCommand system for multiplayer interactions.</li>
     *   <li>Can be extended to support a variety of friendship-related actions such as adding, removing,
     *       or managing friends.</li>
     * </ul>
     *
     * <p>This constructor initializes the command with the default rank {@code Rank.GUEST},
     * ensuring that all players can use it by default, unless rank restrictions are later applied.</p>
     */
    public FriendCommand() {
        super("friend", "f");
    }

    /**
     * Executes the command provided by the player. The command performs various friend-related actions, such as
     * managing friend requests, listing friends, teleporting to friends, and toggling the friend request system.
     *
     * <p>The command behavior depends on the arguments provided:
     * <lu>
     * <li>If only one argument is provided, it processes basic commands such as "help", "list", "toggle", and "requests".</li>
     * <li>If two arguments are provided, it processes commands such as "list" (with pagination), "tp" (teleport to a friend),
     * "add" (add a friend), "remove" (remove a friend), "accept" (accept a friend request), and "deny" (deny a friend request).</li>
     * </lu>
     *
     * <p>If invalid or incomplete arguments are provided, the help menu is displayed.</p>
     *
     * @param player the player executing the command
     * @param args the command arguments provided by the player, where the first argument represents the command
     *             and subsequent arguments represent its parameters
     */
    @Override
    public void execute(Player player, String[] args) {
        switch (args.length) {
            case 1:
                switch (args[0].toLowerCase()) {
                    case "help":
                        FriendUtil.helpMenu(player);
                        return;
                    case "list":
                        FriendUtil.listFriends(player, 1);
                        return;
                    case "toggle":
                        PalaceBungee.getProxyServer().getScheduler().runAsync(PalaceBungee.getInstance(), () -> {
                            player.setFriendRequestToggle(!player.hasFriendToggledOff());
                            if (player.hasFriendToggledOff()) {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " + ChatColor.RED + "OFF");
                            } else {
                                player.sendMessage(ChatColor.YELLOW + "Friend Requests have been toggled " + ChatColor.GREEN + "ON");
                            }
                            PalaceBungee.getMongoHandler().setFriendRequestToggle(player.getUniqueId(), !player.hasFriendToggledOff());
                        });
                        return;
                    case "requests":
                        FriendUtil.listRequests(player);
                        return;
                }
                return;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "list":
                        if (!MiscUtil.isInt(args[1])) {
                            FriendUtil.listFriends(player, 1);
                            return;
                        }
                        FriendUtil.listFriends(player, Integer.parseInt(args[1]));
                        return;
                    case "tp":
                        String user = args[1];
                        Player tp = PalaceBungee.getPlayer(user);
                        if (tp == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        if (!player.getFriends().containsKey(tp.getUniqueId())) {
                            player.sendMessage(ChatColor.GREEN + tp.getUsername() + ChatColor.RED +
                                    " is not on your Friend List!");
                            return;
                        }
                        FriendUtil.teleportPlayer(player, tp);
                        return;
                    case "add":
                        FriendUtil.addFriend(player, args[1]);
                        return;
                    case "remove":
                        FriendUtil.removeFriend(player, args[1]);
                        return;
                    case "accept":
                        FriendUtil.acceptFriend(player, args[1]);
                        return;
                    case "deny":
                        FriendUtil.denyFriend(player, args[1]);
                        return;
                }
        }
        FriendUtil.helpMenu(player);
    }
}