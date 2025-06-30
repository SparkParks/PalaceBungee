package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * Represents a command that provides players with information and a direct
 * link to the server's store. This command displays a clickable message
 * in the chat, allowing players to open the store URL in their browser.
 *
 * <p>When executed, it sends a chat message to the player with the following:
 * <ul>
 *   <li>A yellow, bold message prompting the player to click on it.</li>
 *   <li>A hover text explaining what will happen upon clicking.</li>
 *   <li>An action that opens the store URL in the player's default browser upon clicking.</li>
 * </ul>
 *
 * <p>The command name is "store" and it extends the functionality of the {@link PalaceCommand}.
 */
public class StoreCommand extends PalaceCommand {
    /**
     * Represents a pre-defined message displayed in the player chat,
     * encouraging users to visit the store URL.
     *
     * <p>This message has the following characteristics:</p>
     * <ul>
     *   <li>Text content: <i>"Click to visit our store!"</i>, styled in yellow and bold.</li>
     *   <li>A hover event: Displays the text <i>"Click to open https://store.palace.network"</i>,
     *       with <i>"Click to open"</i> in aqua and the URL in green.</li>
     *   <li>A click event: Opens the URL <i>https://store.palace.network</i> in the user's default browser.</li>
     * </ul>
     *
     * <p>This message is used to enhance user interaction and provide an intuitive mechanism
     * for players to easily access the store.</p>
     */
    private static final BaseComponent[] message = new ComponentBuilder("\nClick to visit our store!\n").color(ChatColor.YELLOW).bold(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://store.palace.network").color(ChatColor.GREEN).create()))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://store.palace.network")).create();

    /**
     * Constructs a new {@code StoreCommand} with the command name "store".
     *
     * <p>This command is designed to provide players with quick access to the server's store.
     * When executed, it sends a clickable message in the chat that allows players to open
     * the store URL in their browser seamlessly.</p>
     *
     * <h3>Command Features:</h3>
     * <ul>
     *   <li>Displays a bright yellow clickable message prompting the player to visit the store.</li>
     *   <li>Includes hover text providing additional details about the action.</li>
     *   <li>Invokes a browser action to open the store URL when clicked.</li>
     * </ul>
     *
     * <p>The {@code "store"} name is registered with the base command functionality provided
     * by the {@link PalaceCommand} class.</p>
     */
    public StoreCommand() {
        super("store");
    }

    /**
     * Executes the "store" command by sending the player a clickable chat message
     * that prompts them to visit the server's store. The message includes:
     * <ul>
     *   <li>A bold, yellow clickable message.</li>
     *   <li>Hover text explaining the action upon clicking.</li>
     *   <li>An action that directs the player to the store URL in their web browser.</li>
     * </ul>
     *
     * <p>This command is used to provide easy access to the server's store for players.</p>
     *
     * @param player The player who issued the command and will receive the clickable store message.
     * @param args   The arguments passed along with the command, though they are not used in this implementation.
     */
    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(message);
    }
}