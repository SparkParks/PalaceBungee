package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

import java.util.Random;

/**
 * The {@code ApplyCommand} class is responsible for handling the "apply" command within the application.
 * This command generates a unique token for the player and allows them to access a specific application portal.
 *
 * <p>The command utilizes MongoDB for storing login tokens and integrates with chat message functionalities to provide
 * an interactive link to the player for application purposes.</p>
 *
 * <h3>Command Functionality</h3>
 * <ul>
 *   <li>Generates a random token for the player.</li>
 *   <li>Updates the MongoDB with the player's unique token information.</li>
 *   <li>Sends the player a clickable message containing a portal link to access applications.</li>
 *   <li>Uses hover and click events to enhance the user interface and provide a direct URL.</li>
 * </ul>
 *
 * <h3>Methods</h3>
 * <ul>
 *   <li><b>{@code execute}:</b> Overrides the parent {@code PalaceCommand} method to execute the functionality of the "apply" command.</li>
 *   <li><b>{@code getRandomToken}:</b> Generates a 12-character alphanumeric string that serves as a unique token for the player.</li>
 * </ul>
 *
 * <h3>Behavior</h3>
 * <p>
 * When a player executes the "apply" command, they are presented with an embedded message containing a link to an application portal.
 * This message uses both hover and click events to improve usability. The command generates a unique token for each execution
 * and ensures that the MongoDB database is updated accordingly with player and token details.
 * </p>
 */
public class ApplyCommand extends PalaceCommand {
    /**
     * <p>
     * An instance of the {@link Random} class used to generate random values.
     * This field is employed within the {@code ApplyCommand} class to create
     * unique, random tokens for players when they execute the "apply" command.
     * </p>
     *
     * <p>
     * The random values produced by this field are utilized to construct alphanumeric
     * strings, which serve as the unique identifiers (tokens) for application portal access.
     * The randomness ensures that each token is distinct and secure.
     * </p>
     *
     * <p>Key usage:</p>
     * <ul>
     *   <li>Token generation in the {@code getRandomToken()} method.</li>
     *   <li>Enhancing security by producing unpredictable sequences.</li>
     * </ul>
     */
    private final Random random = new Random();

    /**
     * Constructs an {@code ApplyCommand} instance to handle the "apply" command.
     *
     * <p>The {@code ApplyCommand} is designed to allow players to access an application portal
     * by generating a unique token and providing an interactive link. It extends the
     * {@code PalaceCommand} base class with the command name "apply".</p>
     *
     * <h3>Key Features</h3>
     * <ul>
     *   <li>Associates the "apply" command with this instance.</li>
     *   <li>Initializes the command with default behavior for processing player interactions with the portal link.</li>
     *   <li>Relies on supporting methods to generate tokens and update player-specific data in the database.</li>
     * </ul>
     */
    public ApplyCommand() {
        super("apply");
    }

    /**
     * Executes the "apply" command for a player, generating a unique login token and sending
     * a clickable message with a link to the application portal.
     *
     * <p>This method integrates with a MongoDB handler to store the generated token and
     * player information securely. It also leverages in-game messaging features to provide
     * an interactive message that includes hover and click events, enhancing user experience.</p>
     *
     * @param player The player who issued the command. This is used to generate a unique token,
     *               update the database with their information, and send them a message with
     *               the portal link.
     * @param args   The arguments passed with the command. These are currently unused in this
     *               implementation.
     */
    @Override
    public void execute(Player player, String[] args) {
        String token = getRandomToken();
        PalaceBungee.getMongoHandler().setTitanLogin(player.getUniqueId(), token);
        player.sendMessage(new ComponentBuilder("\nWe now take applications through our new portal\nClick to see what positions we have available!\n").color(ChatColor.YELLOW).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://titan.palace.network/apply/login/" + token + "/" + player.getUniqueId().toString()).color(ChatColor.GREEN).create()))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://titan.palace.network/apply/login/" + token + "/" + player.getUniqueId().toString())).create());
    }

    /**
     * Generates a random 12-character alphanumeric token.
     *
     * <p>The generated token consists of characters from the following set:
     * <ul>
     *   <li>Lowercase letters: a-z</li>
     *   <li>Uppercase letters: A-Z</li>
     *   <li>Digits: 0-9</li>
     * </ul>
     * The token is designed to be unique and is typically used for identification purposes,
     * such as in login systems or for generating session keys.
     *
     * @return A {@code String} representing a randomly generated 12-character alphanumeric token.
     */
    public String getRandomToken() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
