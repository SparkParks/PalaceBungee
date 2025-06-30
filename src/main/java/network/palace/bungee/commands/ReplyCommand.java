package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.DMPacket;
import network.palace.bungee.utils.EmojiUtil;

import java.util.UUID;

/**
 * <p>The {@code ReplyCommand} class represents a command that allows players to reply to the
 * last player who directly messaged them. This command is used to continue private conversations
 * between players on the server.</p>
 *
 * <p>This command supports the following key functionalities:</p>
 * <ul>
 *   <li>Allows players to send a private message to the player they last communicated with via direct message.</li>
 *   <li>Ensures that certain conditions, such as mute status, server rules, and rank permissions, are met before sending messages.</li>
 *   <li>Handles chat message processing, including applying ranks, formatting, and emoji conversion.</li>
 *   <li>Provides error handling for various scenarios, such as attempting to reply without a prior message or when the target is unavailable.</li>
 * </ul>
 *
 * <p><strong>Behavior Notes:</strong></p>
 * <ul>
 *   <li>If the command is used without a message argument, the player will receive a prompt specifying the correct usage.</li>
 *   <li>Players with insufficient online time will be restricted from using this command to prevent spam from new guests.</li>
 *   <li>Muted players can only send replies to staff members using this command.</li>
 *   <li>If direct messaging is disabled globally or by the target player, the sender will be notified accordingly.</li>
 *   <li>If the originally messaged player is offline, the command attempts to find and message them through proxy servers if applicable.</li>
 * </ul>
 *
 * <p>Ensures compliance with server chat rules and integrates with various server subsystems, such as:</p>
 * <ul>
 *   <li>Rank-based permissions and message restrictions</li>
 *   <li>Chat moderation tools and analysis</li>
 *   <li>Emoji processing for enhanced message display</li>
 *   <li>Backend proxy communication for cross-server messaging</li>
 * </ul>
 *
 * <p>This command is an extension of the {@code PalaceCommand} base class and uses the same
 * command structure while adding functionality specific to replying in private messages.</p>
 */
public class ReplyCommand extends PalaceCommand {

    /**
     * Constructs a new {@code ReplyCommand} instance with the command name "reply"
     * and an alias "r".
     * <p>
     * This command is designed to facilitate quick and efficient responses to players'
     * previous messages or conversations. The inclusion of an alias ensures ease of use
     * and accessibility for players.
     * </p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Primary command name: "reply".</li>
     *   <li>Alias for the command: "r".</li>
     *   <li>Enhances communication among players by providing an efficient reply mechanism.</li>
     * </ul>
     *
     * <p>This class extends the {@code PalaceCommand} base class, inheriting its core
     * functionalities, and is primarily aimed at improving user interaction within
     * the system.</p>
     */
    public ReplyCommand() {
        super("reply", "r");
    }

    /**
     * Executes the reply command, allowing a player to send a direct message
     * to the last player they interacted with via direct messaging.
     *
     * <p>The method enforces various checks including:</p>
     * <ul>
     *   <li>Whether the player has sufficient online time to use the chat.</li>
     *   <li>Whether the player they are replying to is available to receive messages.</li>
     *   <li>Whether direct messaging is enabled both server-wide and for the receiving player.</li>
     *   <li>Permissions and conditions specific to the sender's rank and muted status.</li>
     * </ul>
     *
     * <p>If all conditions are met, the message is processed and delivered to the target player,
     * including support for features such as social spying, mentions, and rank-based formatting.</p>
     *
     * @param player The player executing the reply command.
     * @param args   The message arguments supplied by the player. Should include the message to be sent.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/reply [Message]");
            return;
        }
        if (player.getTotalOnlineTime() < 600) {
            player.sendMessage(ChatColor.RED + "New guests must be on the server for at least 10 minutes before talking in chat." +
                    ChatColor.DARK_AQUA + " Learn more at palnet.us/rules");
            return;
        }
        boolean onlyStaff = player.isMuted();
        UUID replyTo = player.getReplyTo();
        long replyTime = player.getReplyTime();
        if (replyTo == null || replyTime == 0) {
            player.sendMessage(ChatColor.AQUA + "No one to reply to! Message someone with " + ChatColor.YELLOW + "/msg [Username] [Message]");
            return;
        }
        String message = String.join(" ", args);
        Player targetPlayer = PalaceBungee.getPlayer(replyTo);
        if (player.getRank().getRankId() < Rank.CHARACTER.getRankId() && !PalaceBungee.getConfigUtil().isDmEnabled()) {
            player.sendMessage(ChatColor.RED + "Direct messages are currently disabled.");
            return;
        }
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

                PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "DM Reply to " + args[0], () -> {
                    PalaceBungee.getChatUtil().saveMessageCache(player.getUniqueId(), processed);
                    try {
                        String msg;
                        try {
                            msg = EmojiUtil.convertMessage(player, processed);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return;
                        }
                        PalaceBungee.getChatUtil().socialSpyMessage(player.getUniqueId(), targetPlayer.getUniqueId(), player.getUsername(), targetPlayer.getUsername(), PalaceBungee.getServerUtil().getChannel(player), msg, "r");
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
                if (onlyStaff && PalaceBungee.getMongoHandler().getRank(replyTo).getRankId() < Rank.TRAINEE.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't direct message this player while muted.");
                    return;
                }
                String username = PalaceBungee.getMongoHandler().uuidToUsername(replyTo);
                String processed = PalaceBungee.getChatUtil().processChatMessage(player, message, "DM", true, false);
                if (processed == null) return;

                PalaceBungee.getChatUtil().analyzeMessage(player.getUniqueId(), player.getRank(), processed, "DM Reply to " + username, () -> {
                    try {
                        String msg;
                        try {
                            msg = EmojiUtil.convertMessage(player, processed);
                        } catch (IllegalArgumentException e) {
                            player.sendMessage(ChatColor.RED + e.getMessage());
                            return;
                        }
                        UUID targetProxy = PalaceBungee.getMongoHandler().findPlayer(replyTo);
                        if (targetProxy == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }
                        DMPacket packet = new DMPacket(player.getUsername(), username, msg, PalaceBungee.getServerUtil().getChannel(player), "r",
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
}
