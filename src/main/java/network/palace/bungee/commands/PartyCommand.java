package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * Represents a command handler for Party-related functionality in the application.
 * This class manages various party operations such as creating, inviting, managing, and
 * interacting with parties, and is designed to extend {@code PalaceCommand}.
 *
 * <p>The PartyCommand handles a variety of sub-commands to allow players to manage
 * their parties and communicate effectively with party members. The supported commands include:
 *
 * <ul>
 * <li><b>help</b>: Displays the party help menu with details on available commands.</li>
 * <li><b>create</b>: Creates a new party.</li>
 * <li><b>accept</b>: Accepts a pending party invitation.</li>
 * <li><b>close</b>: Disbands the current party.</li>
 * <li><b>leave</b>: Allows a player to leave their current party.</li>
 * <li><b>list</b>: Lists all current members of the party.</li>
 * <li><b>warp</b>: Teleports all party members to the server of the party leader.</li>
 * <li><b>remove [player]</b>: Removes a specified player from the party.</li>
 * <li><b>promote [player]</b>: Promotes a specified player to party leader.</li>
 * <li><b>chat [message]</b>: Sends a message to all members in the party's private chat.</li>
 * <li><b>invite [player]</b>: Sends a party invitation to a specified player.</li>
 * </ul>
 *
 * <p>If no sub-command is provided, the {@code helpMenu} is displayed by default to assist users.
 * Most operations involve interaction with the {@code PartyUtil} utility class for party processing.
 *
 * <p>The class ensures robust error handling for all supported operations,
 * providing meaningful feedback to the player in case of failures.
 */
public class PartyCommand extends PalaceCommand {

    /**
     * Constructs a new {@code PartyCommand} instance with the primary command name "party"
     * and the alias "p".
     * <p>
     * {@code PartyCommand} is a part of the command system that facilitates party-related
     * functionality for players. This command allows interaction with the party system
     * through in-game commands.
     * </p>
     *
     * <h3>Key Features:</h3>
     * <ul>
     *   <li>Provides players access to party functionalities.</li>
     *   <li>Includes a shorthand alias ("p") for easier usage.</li>
     *   <li>Integrates seamlessly with the overall command architecture provided by the
     *       {@code PalaceCommand} superclass.</li>
     * </ul>
     *
     * <p>This command utilizes the <strong>default rank of {@code Rank.GUEST}</strong>,
     * ensuring accessibility to all players unless otherwise restricted later.</p>
     */
    public PartyCommand() {
        super("party", "p");
    }

    /**
     * Executes the party-related commands for the player based on the provided arguments.
     * This method handles various party management functionalities, such as creating a party,
     * inviting players, promoting leaders, and more.
     *
     * <p>The following commands are supported:</p>
     * <ul>
     *     <li><strong>help</strong>: Displays the help menu with available party commands.</li>
     *     <li><strong>create</strong>: Creates a new party for the player.</li>
     *     <li><strong>accept</strong>: Accepts a pending party invitation.</li>
     *     <li><strong>close</strong>: Closes the party if the player is the party leader.</li>
     *     <li><strong>leave</strong>: Allows the player to leave the currently joined party.</li>
     *     <li><strong>list</strong>: Lists the members of the player's current party.</li>
     *     <li><strong>warp</strong>: Warps party members to the leader's server.</li>
     *     <li><strong>remove [player]</strong>: Removes a specified player from the party.</li>
     *     <li><strong>promote [player]</strong>: Promotes the specified player to the party leader.</li>
     *     <li><strong>chat [message]</strong>: Sends a message to the party chat.</li>
     *     <li><strong>invite [player]</strong>: Invites a specific player to the party.</li>
     * </ul>
     *
     * @param player The Player executing the command.
     * @param args   The arguments provided with the command. The first argument determines the
     *               action, while additional arguments may provide necessary details such as a
     *               player's name or a message.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                    helpMenu(player);
                    return;
                case "create":
                    try {
                        PalaceBungee.getPartyUtil().createParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while creating a party! Please try again in a few minutes.");
                    }
                    return;
                case "accept":
                    try {
                        PalaceBungee.getPartyUtil().acceptRequest(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while accepting that party invite!");
                    }
                    return;
                case "close":
                    try {
                        PalaceBungee.getPartyUtil().closeParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while closing the party! Please try again in a few minutes.");
                    }
                    return;
                case "leave":
                    try {
                        PalaceBungee.getPartyUtil().leaveParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while leaving the party! Please try again in a few minutes.");
                    }
                    return;
                case "list":
                    try {
                        PalaceBungee.getPartyUtil().listParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while listing party members! Please try again in a few minutes.");
                    }
                    return;
                case "warp":
                    try {
                        PalaceBungee.getPartyUtil().warpParty(player);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while warping party members to your server! Please try again in a few minutes.");
                    }
                    return;
                case "remove":
                    if (args.length > 1) {
                        try {
                            PalaceBungee.getPartyUtil().removeFromParty(player, args[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while removing that player from your party! Please try again in a few minutes.");
                        }
                    }
                    return;
                case "promote":
                    if (args.length > 1) {
                        try {
                            PalaceBungee.getPartyUtil().promoteToLeader(player, args[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while promoting a new party leader! Please try again in a few minutes.");
                        }
                    }
                    return;
                case "chat":
                    try {
                        String[] argsMinusOne = new String[args.length - 1];
                        System.arraycopy(args, 1, argsMinusOne, 0, args.length - 1);
                        player.getProxiedPlayer().ifPresent(p -> PalaceBungee.getProxyServer().getPluginManager().dispatchCommand(p, "pchat " + String.join(" ", argsMinusOne)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while sending your party chat message! Please try again in a few minutes.");
                    }
                    return;
                case "invite":
                    if (args.length > 1) {
                        try {
                            PalaceBungee.getPartyUtil().inviteToParty(player, args[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while inviting that player to your party! Please try again in a few minutes.");
                        }
                        return;
                    }
            }
        }
        helpMenu(player);
    }

    /**
     * Displays the help menu for Party-related commands to the specified player.
     * <p>
     * The help menu includes a list of commands for managing Parties, such as creating a Party,
     * inviting players, promoting members, and other Party-related actions.
     * </p>
     *
     * @param player The player to whom the help menu will be displayed.
     */
    public void helpMenu(Player player) {
        String dash = "\n" + ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Party Commands:" +
                dash + "/party help " + y + "- Shows this help menu" +
                dash + "/party create " + y + "- Create a new Party" +
                dash + "/party invite [player] " + y + "- Invite a player to your Party" +
                dash + "/party leave " + y + "- Leave your current Party" +
                dash + "/party list " + y + "- List all of the members in your Party" +
                dash + "/party promote [player] " + y + "- Promote a player to Party Leader" +
                dash + "/party accept " + y + "- Accept a Party invite from a player" +
                dash + "/party warp " + y + "- Brings the members of your Party to your server" +
                dash + "/party remove [player] " + y + "- Removes a player from your Party" +
                dash + "/pchat [message] " + y + "- Message members of your Party" +
                dash + "/party close " + y + "- Close your Party");
    }
}