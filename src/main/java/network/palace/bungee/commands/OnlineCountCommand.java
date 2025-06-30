package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * The {@code OnlineCountCommand} class is a command implementation that provides
 * functionality for players to retrieve the current number of online players in
 * the server ecosystem.
 *
 * <p>This command is invoked using the alias "oc" and is typically utilized
 * within the context of the PalaceBungee environment. Once executed, it
 * returns the count of online players via a message to the player who initiated
 * the command.</p>
 *
 * <h3>Command Behavior:</h3>
 * <ul>
 *   <li>Retrieves the total count of players currently online from the
 *   {@code PalaceBungee}'s MongoDB handler.</li>
 *   <li>Displays the result in a formatted message to the player.</li>
 * </ul>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Leverages the core PalaceBungee infrastructure to ensure the online
 *   player count is accurate and up-to-date.</li>
 *   <li>Sends feedback to the player in a clear, concise format using colored
 *   text for better visibility.</li>
 * </ul>
 *
 * <h3>Method Overview:</h3>
 * <ul>
 *   <li>{@code execute(Player player, String[] args)} - Executes the online
 *   count command logic and sends the formatted count to the player.</li>
 * </ul>
 *
 * <p>This class is designed for use by players on PalaceBungee-based Minecraft
 * networks and depends on the correct functionality of the {@code MongoHandler}
 * service for retrieving player counts.</p>
 */
public class OnlineCountCommand extends PalaceCommand {

    /**
     * Constructs a new {@code OnlineCountCommand} instance with the command alias "oc".
     *
     * <p>The {@code OnlineCountCommand} is used to retrieve and display the total
     * number of players currently online in the server environment. This command
     * serves as a utility for players to check the live player count.</p>
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>Maps to the "oc" alias for execution by players.</li>
     *   <li>Displays the online player count obtained through the infrastructure
     *   of the {@code PalaceBungee} system.</li>
     *   <li>Outputs the data in a formatted and visually clear message.</li>
     * </ul>
     *
     * <h3>Context:</h3>
     * <p>This command is integrated into the PalaceBungee ecosystem and provides
     * accurate, real-time player count information by leveraging the
     * {@code MongoHandler} for database access.</p>
     */
    public OnlineCountCommand() {
        super("oc");
    }

    /**
     * Executes the "Online Count" command, providing the player with the current
     * count of players online in the server ecosystem.
     *
     * <p>Upon invocation, this method retrieves the total number of online players
     * from the {@code PalaceBungee} database via its MongoHandler, and sends a
     * formatted message with the count to the specified player.</p>
     *
     * <h3>Behavior:</h3>
     * <ul>
     *   <li>Accesses the {@code PalaceBungee} instance to retrieve the online
     *   player count.</li>
     *   <li>Sends the player a clear, colored message displaying the count.</li>
     * </ul>
     *
     * @param player The player who issued the command and will receive the message with the total online player count.
     * @param args An array of command arguments passed by the player; this command does not process additional arguments.
     */
    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.GREEN + "\nTotal Players Online: " + PalaceBungee.getMongoHandler().getOnlineCount() + "\n");
    }
}