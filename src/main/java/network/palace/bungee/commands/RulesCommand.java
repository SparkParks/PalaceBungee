package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * The {@code RulesCommand} class defines a command used in the Palace Network that sends players
 * a clickable message with a link to view the server's rules.
 *
 * <p>The command displays a message in chat, styled with bold and yellow text. The message includes
 * a hover text indicating a clickable URL that directs users to the official rules page
 * ("https://palnet.us/rules").
 *
 * <p>Features of this command include:
 * <ul>
 *   <li>Hovering over the message shows additional text, styled in aqua and green colors.</li>
 *   <li>Clicking on the message opens the rules link in the user's web browser.</li>
 * </ul>
 *
 * <p>This class extends the {@code PalaceCommand} class and overrides the {@code execute}
 * method, which sends the predefined, interactive message to the player who invokes the command.
 *
 * <p>Command Syntax:
 * <ul>
 *   <li>Simply typing "/rules" executes the command.</li>
 * </ul>
 *
 * <p>This command ensures that all players have quick and convenient access to the server rules for reference.
 */
public class RulesCommand extends PalaceCommand {
    /**
     * Defines a predefined clickable and interactive message that directs users to
     * Palace Network's official rules page.
     *
     * <p>The message features the following attributes:
     * <ul>
     *   <li><b>Text Content:</b> Displays the text "\nClick to view Palace Network's rules!\n".</li>
     *   <li><b>Styling:</b> The text is in bold and yellow color.</li>
     *   <li><b>Hover Interaction:</b> When hovered over, it shows a tooltip with the text:
     *       <ul>
     *         <li>"Click to open" styled in aqua color.</li>
     *         <li>"https://palnet.us/rules" styled in green color.</li>
     *       </ul>
     *   </li>
     *   <li><b>Click Interaction:</b> Clicking on the message opens the URL "https://palnet.us/rules"
     *       in the user's web browser.</li>
     * </ul>
     *
     * <p>This interactive message enhances user experience by providing direct and convenient
     * access to the server's rules through the in-game chat system.
     */
    static BaseComponent[] message = new ComponentBuilder("\nClick to view Palace Network's rules!\n").color(ChatColor.YELLOW).bold(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/rules").color(ChatColor.GREEN).create()))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/rules")).create();

    /**
     * Constructs a new {@code RulesCommand} instance with the command name "rules".
     * <p>
     * The {@code RulesCommand} is designed to provide players with a direct way to
     * access the server's rules via an in-game clickable chat message.
     * </p>
     *
     * <h3>Purpose:</h3>
     * <ul>
     *   <li>Ensures that players can easily reference the official rules of the server.</li>
     *   <li>Improves accessibility to the rules by integrating interactive features in chat.</li>
     * </ul>
     *
     * <p>Players can execute this command by simply typing "/rules" in the chat. When invoked,
     * it triggers a stylized chat message that links to the rules page. The message includes
     * hover text and a clickable URL to enhance user interactivity.</p>
     */
    public RulesCommand() {
        super("rules");
    }

    /**
     * Executes the "/rules" command, sending the player a clickable message that links to
     * the Palace Network's official rules page.
     *
     * <p>The message appears in bold yellow text and includes hover effects with additional
     * descriptive text. Clicking the message opens the rules webpage ("https://palnet.us/rules")
     * in the player's default browser.
     *
     * @param player The player who executed the command. This method sends the interactive message to this player.
     * @param args The arguments provided with the command. This specific command does not require or process any arguments.
     */
    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(message);
    }
}
