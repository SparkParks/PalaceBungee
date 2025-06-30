package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * <p>
 * The <code>LinkCommand</code> class represents a command that allows players to link their Minecraft accounts to
 * a forum account. This command provides multiple subcommands such as linking with an email address, confirming
 * the link with a code, and unlinking the account.
 * </p>
 *
 * <p>
 * <strong>Command Structure:</strong>
 * <ul>
 *   <li><code>/link &lt;email address&gt;</code>: Links a player's Minecraft account to the specified email address.</li>
 *   <li><code>/link confirm [six-digit code]</code>: Confirms the linking of the account using a verification code.</li>
 *   <li><code>/link cancel</code>: Cancels or unlinks a player's Minecraft account from the forum account.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Validation and Feedback:</strong>
 * <ul>
 *   <li>The provided email address must be in a valid email format to proceed with the linking.</li>
 *   <li>Players receive feedback through in-game messages if any errors occur, such as invalid email format or
 *       missing arguments.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This command is designed for use in a Minecraft server environment where account linking is supported via external
 * forum utilities.
 * </p>
 *
 * <p>
 * <strong>Example Subcommand Logic:</strong>
 * <ul>
 *   <li>When no arguments are provided, the player is prompted with usage guidance for the command.</li>
 *   <li>When the "cancel" subcommand is invoked, the player's account is unlinked.</li>
 *   <li>When the "confirm" subcommand is invoked, the system expects a six-digit code as an additional argument.</li>
 *   <li>Email validation ensures the input adheres to standard email formatting conventions.</li>
 * </ul>
 * </p>
 */
public class LinkCommand extends PalaceCommand {

    /**
     * Constructs a new {@code LinkCommand} instance with the command name "link".
     * <p>
     * The {@code LinkCommand} is designed to provide functionality specific to linking actions
     * within the system. This command is initialized with the default rank {@code Rank.GUEST}
     * as defined in the {@code PalaceCommand} superclass.
     * </p>
     *
     * <p>This constructor invokes the parent class {@code PalaceCommand}'s constructor
     * with the specified command name "link". It signifies the creation of a command
     * tailored for linking operations.</p>
     *
     * <h3>Purpose:</h3>
     * <ul>
     *   <li>Associates the command name "link" with its functionality in the system.</li>
     *   <li>Inherits basic command properties and behaviors from the {@code PalaceCommand} superclass.</li>
     * </ul>
     */
    public LinkCommand() {
        super("link");
    }

    /**
     * Executes the link command for a player. This command allows players to link or unlink their
     * forum account with their in-game Minecraft account. It supports various subcommands such as
     * providing an email to link, confirming a link with a confirmation code, or canceling an active
     * link request.
     *
     * <p>Subcommands:
     * <ul>
     *     <li><b>link [email address]</b>: Links the player's Minecraft account to the specified email.</li>
     *     <li><b>confirm [six-digit code]</b>: Confirms the link process with the provided code.</li>
     *     <li><b>cancel</b>: Cancels an ongoing linking process for the player's account.</li>
     * </ul>
     *
     * Validation:
     * <ul>
     *     <li>Email format is validated to ensure it is a syntactically valid email address.</li>
     *     <li>If no arguments are provided, instructions on using the command are shown.</li>
     * </ul>
     *
     * @param player The player executing the command. This represents the in-game user issuing the link.
     * @param args   An array of arguments passed with the command. The first argument determines the
     *               subcommand, such as "link", "confirm", or "cancel". Additional arguments may include
     *               the email address or confirmation code as required.
     */
    @Override
    public void execute(Player player, String[] args) {
//        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
//            player.sendMessage(ChatColor.YELLOW + "You will be able to link your forum account with your Minecraft account soon!");
//            return;
//        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/link [email address]");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "cancel": {
                PalaceBungee.getForumUtil().unlinkAccount(player);
                return;
            }
            case "confirm": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "/link confirm [six-digit code]");
                    return;
                }
                PalaceBungee.getForumUtil().confirm(player, args[1]);
                return;
            }
        }
        String email = args[0];
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            player.sendMessage(ChatColor.RED + "That isn't a valid email!");
            return;
        }
        PalaceBungee.getForumUtil().linkAccount(player, email);
    }
}