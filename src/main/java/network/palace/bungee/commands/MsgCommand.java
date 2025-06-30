package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.DMPacket;
import network.palace.bungee.utils.EmojiUtil;

import java.util.UUID;

/**
 * Represents the implementation of the `/msg` command that allows players to send private messages
 * to another player on the server.
 * <p>
 * This command supports direct messaging functionality with additional checks for permissions,
 * muted status, and message processing. It also includes tab completion support for player names.
 * </p>
 *
 * <h3>Command Functionality</h3>
 * <p>The main features of the MsgCommand include:</p>
 * <ul>
 *   <li>Verifying input arguments to ensure the correct command usage.</li>
 *   <li>Checking if the sender meets the required online time to send messages.</li>
 *   <li>Ensuring muted players cannot message non-staff members.</li>
 *   <li>Verifying if direct messaging is enabled for the server or target player.</li>
 *   <li>Processing and formatting messages (including emoji conversion).</li>
 *   <li>Notifying both sender and recipient of the private message.</li>
 *   <li>Enabling `/msg` auto-complete suggestions for player names.</li>
 * </ul>
 *
 * <h3>Command Execution</h3>
 * <p>When executed, the MsgCommand validates all necessary conditions before processing and delivering
 * the message. It also handles error scenarios, such as:</p>
 * <ul>
 *   <li>Insufficient arguments or incorrect command usage.</li>
 *   <li>Attempting to message while muted (with restrictions).</li>
 *   <li>Target player not found or having messaging disabled.</li>
 *   <li>Server restrictions on messaging for certain ranks or configurations.</li>
 *   <li>Handling exceptions during message processing and delivery.</li>
 * </ul>
 *
 * <h3>Tab Completion</h3>
 * <p>The command supports tab completion for player names to enhance user experience. Players
 * can interactively type the command with suggestions for currently available player names.</p>
 *
 * <h3>Logging and Status Management</h3>
 * <ul>
 *   <li>Messages are logged for server monitoring via the social spy feature.</li>
 *   <li>Direct message reply information is stored for both sender and recipient.</li>
 * </ul>
 */
public class MsgCommand extends PalaceCommand {

    /**
     * Constructs a new {@code MsgCommand} instance with predefined command names
     * and aliases for sending private messages between players.
     *
     * <p>The {@code MsgCommand} is part of the PalaceCommand system and is used
     * to facilitate private communication between players. It includes support for
     * tab-complete functionality, including player name suggestions.</p>
     *
     * <h3>Command Features:</h3>
     * <ul>
     *   <li>Primary command name: {@code msg}.</li>
     *   <li>Aliases: {@code m}, {@code tell}, {@code w}.</li>
     *   <li>Tab-completion support enabled for ease of use.</li>
     *   <li>Player name tab-completion enabled for quick message recipient selection.</li>
     * </ul>
     *
     * <p>The primary use case of this command is to allow players to send direct
     * messages to one another using the defined command name or its aliases.
     * This class extends the {@code PalaceCommand} base class, inheriting its
     * behavior and structure.</p>
     */
    public MsgCommand() {
        super("msg", "m", "tell", "w");
        tabComplete = true;
        tabCompletePlayers = true;
    }

    /**
     * Executes the direct message (DM) command for a player. This method allows a player to send private messages
     * to other players if certain conditions are met, such as having sufficient playtime, being unmuted or meeting
     * rank requirements. The method also ensures that the target player has DM enabled and processes the message
     * content before delivering it.
     *
     * <p>Conditions and processing include:</p>
     * <ul>
     *   <li>Checking if the player has been online for the minimum required time.</li>
     *   <li>Validating the target player's availability and DM settings.</li>
     *   <li>Formatting and analyzing the message for chat compatibility.</li>
     *   <li>Handling muted players who attempt to use the DM command.</li>
     *   <li>Delivering messages directly or via proxy if the target player is offline.</li>
     * </ul>
     *
     * <p>Error handling includes validation for invalid inputs, message processing failures,
     * and handling exceptions during execution.</p>
     *
     * @param player The {@link Player} object representing the sender of the DM command.
     * @param args An array of {@link String} arguments provided by the player.
     *             <ul>
     *                <li>args[0]: The username of the target player.</li>
     *                <li>args[1...n]: The message content.</li>
     *             </ul>
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.GREEN + "Direct Messaging:");
            player.sendMessage(ChatColor.AQUA + "/msg [Player] [Message]");
            player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.YELLOW + "/msg " + player.getUsername() + " Hello there!");
            return;
        }
        if (player.getTotalOnlineTime() < 600) {
            player.sendMessage(ChatColor.RED + "New guests must be on the server for at least 10 minutes before talking in chat." +
                    ChatColor.DARK_AQUA + " Learn more at palnet.us/rules");
            return;
        }
        boolean onlyStaff = player.isMuted();
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();
        if (player.getRank().getRankId() < Rank.CHARACTER.getRankId() && !PalaceBungee.getConfigUtil().isDmEnabled()) {
            player.sendMessage(ChatColor.RED + "Direct messages are currently disabled.");
            return;
        }
        Player targetPlayer = PalaceBungee.getPlayer(args[0]);
        if (targetPlayer != null) {
            if (player.getRank().getRankId() < Rank.CHARACTER.getRankId() && (!targetPlayer.isDmEnabled() || (targetPlayer.isIgnored(player.getUniqueId()) && targetPlayer.getRank().getRankId() < Rank.CHARACTER.getRankId()))) {
                player.sendMessage(ChatColor.RED + "This person has messages disabled!");
                return;
            }
            if (onlyStaff && targetPlayer.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                player.sendMessage(ChatColor.RED + "You can't direct message this player while muted.");
                return;
            }
            try {
                String processed = PalaceBungee.getChatUtil().processChatMessage(player, message, "DM", true, false);
                if (processed == null) return;

                PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "DM to " + args[0], () -> {
                    PalaceBungee.getChatUtil().saveMessageCache(player.getUniqueId(), processed);
                    try {
                        String msg;
                        try {
                            msg = EmojiUtil.convertMessage(player, processed);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return;
                        }
                        PalaceBungee.getChatUtil().socialSpyMessage(player.getUniqueId(), targetPlayer.getUniqueId(), player.getUsername(), targetPlayer.getUsername(), PalaceBungee.getServerUtil().getChannel(player), msg, "msg");
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "You" + ChatColor.GREEN + " -> " + targetPlayer.getRank().getFormattedName() + ChatColor.GRAY + " " + targetPlayer.getUsername() + ": " + ChatColor.WHITE + msg);
                        targetPlayer.sendMessage(player.getRank().getFormattedName() + ChatColor.GRAY + " " + player.getUsername() + ChatColor.GREEN + " -> " + ChatColor.LIGHT_PURPLE + "You: " + ChatColor.WHITE + msg);
                        targetPlayer.mention();
                        player.setReplyTo(targetPlayer.getUniqueId());
                        player.setReplyTime(System.currentTimeMillis());
                        targetPlayer.setReplyTo(player.getUniqueId());
                        targetPlayer.setReplyTime(System.currentTimeMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
            }
        } else {
            try {
                String target = args[0];
                if (onlyStaff && PalaceBungee.getMongoHandler().getRank(target).getRankId() < Rank.TRAINEE.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't direct message this player while muted.");
                    return;
                }
                String processed = PalaceBungee.getChatUtil().processChatMessage(player, message, "DM", true, false);
                if (processed == null) return;

                PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "DM to " + args[0], () -> {
                    try {
                        String msg;
                        try {
                            msg = EmojiUtil.convertMessage(player, processed);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return;
                        }
                        UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(target);
                        if (targetProxy == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        DMPacket packet = new DMPacket(player.getUsername(), target, msg, PalaceBungee.getServerUtil().getChannel(player), "msg",
                                player.getUniqueId(), null, PalaceBungee.getProxyID(), true, player.getRank());
                        PalaceBungee.getMessageHandler().sendToProxy(packet, targetProxy);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error sending your direct message. Try again soon!");
            }
        }
    }

    /**
     * Handles the tab completion for the "MsgCommand" by providing potential argument suggestions
     * based on the player's input and context.
     *
     * <p>This method utilizes the parent's tab completion functionality to process player input
     * and return a list of possible completions.</p>
     *
     * @param commandSender The source of the command. Typically, this is the player or console issuing the command.
     *                      This parameter is used to determine eligibility for tab completion and to fetch context.
     * @param args          An array of arguments provided along with the command. The last element in the array
     *                      is typically the partial input for which completions are being requested.
     * @return An {@code Iterable<String>} containing a list of strings representing possible tab completions
     *         for the current input. If no completions are available, it returns an empty list.
     */
    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        return super.onTabComplete(commandSender, args);
    }
}
