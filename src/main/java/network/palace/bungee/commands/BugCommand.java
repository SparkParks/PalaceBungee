package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * Represents the BugCommand in the PalaceCommand system.
 * <p>
 * The BugCommand is designed to provide easy access for players
 * to report a bug in the system. When this command is executed,
 * it sends a clickable link to players enabling them to access the bug
 * report webpage.
 * </p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Triggers a message in the chat with a clickable link to the bug report page.</li>
 *   <li>Link includes a hover effect with additional informational text.</li>
 *   <li>Encourages user interaction seamlessly using the in-game chat system.</li>
 * </ul>
 *
 * <p>This class extends the {@code PalaceCommand} base class and overrides
 * the {@code execute} method to define its behavior when invoked.</p>
 */
public class BugCommand extends PalaceCommand {

    /**
     * Constructs a new {@code BugCommand} instance with the command name "bug".
     * <p>
     * The BugCommand provides the functionality to allow players to easily report
     * bugs in the system through an in-game clickable link.
     * </p>
     *
     * <h3>Purpose:</h3>
     * <ul>
     *   <li>Facilitates reporting of issues to the development team.</li>
     *   <li>Improves user interaction by integrating a direct command for bug reports.</li>
     * </ul>
     *
     * <p>When this command is invoked, it displays a clickable message in the chat
     * that redirects players to the specified bug report webpage when clicked.</p>
     */
    public BugCommand() {
        super("bug");
    }

    /**
     * Executes the BugCommand, sending a message to the player with a clickable link
     * for reporting a bug. The message features stylized text and includes hover and
     * click interactions for an enhanced user experience.
     *
     * <p>The clickable link directs the user to the bug report web page. A hover text
     * is also included to provide additional feedback when the user hovers over the link.</p>
     *
     * @param player the player who executed the command; this is the recipient of the
     *               clickable message.
     * @param args   the command arguments provided by the player; unused in the current
     *               implementation of this command.
     */
    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(new ComponentBuilder("\nClick to report a bug!\n")
                .color(ChatColor.YELLOW).underlined(false).bold(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/bugreport"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit https://palnet.us/apply").color(ChatColor.GREEN).create())).create());
    }
}
