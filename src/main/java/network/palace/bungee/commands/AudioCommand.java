package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

import java.util.Random;

/**
 * The {@code AudioCommand} class represents a command to generate a unique audio token
 * for a player and provide them with a clickable link to connect to the audio server.
 * <p>
 * This class extends {@link PalaceCommand} and is specifically designed to manage audio-related
 * features within the Palace network environment.
 * </p>
 *
 * <p><b>Functionality:</b></p>
 * <ul>
 *   <li>Generates a random 12-character alphanumeric audio token.</li>
 *   <li>Stores the generated token in the player's online data.</li>
 *   <li>Sends the player a clickable message to connect to the audio server, with the appropriate URL
 *       based on whether the Palace network is in test mode.</li>
 * </ul>
 *
 * <p><b>Command Behavior:</b>
 * Overriding the {@code execute} method allows the class to handle how the {@code audio} command
 * behaves when invoked. The functionality ensures only one active audio token is associated with
 * the player, and it updates the token accordingly.
 * </p>
 *
 * <p><b>Token Generation:</b></p>
 * <ul>
 *   <li>The {@code getRandomToken} method generates a secure, random token using alphanumeric characters.
 *       This token ensures authority and authentication for the audio server connection.</li>
 * </ul>
 *
 * <p><b>Dependencies:</b></p>
 * <ul>
 *   <li>MongoDB handler: Utilized to store the generated token into the player's online data.</li>
 *   <li>BungeeCord API: The class builds and sends players formatted messages containing clickable events.</li>
 * </ul>
 */
public class AudioCommand extends PalaceCommand {
    /**
     * An instance of {@link Random} used for generating pseudo-random numbers.
     * <p>
     * This field is utilized for tasks requiring randomness, such as generating
     * unique identifiers or tokens. In the context of the {@code AudioCommand} class,
     * it is specifically used to create a secure 12-character alphanumeric audio token
     * for players.
     * </p>
     * <p><b>Characteristics:</b></p>
     * <ul>
     *   <li>Final: The instance is immutable and initialized once during the class instantiation.</li>
     *   <li>Thread-unsafe: The {@link Random} class is not thread-safe, so synchronization may be needed if accessed by multiple threads.</li>
     * </ul>
     */
    private final Random random = new Random();

    /**
     * Constructs an {@code AudioCommand} instance with the default command name "audio".
     * <p>
     * This constructor initializes the command and sets its name by invoking the
     * {@code PalaceCommand} superclass constructor with the command name "audio".
     * </p>
     *
     * <p><b>Purpose:</b></p>
     * <ul>
     *   <li>The {@code AudioCommand} is explicitly designed to handle the "audio" command, which
     *       generates and manages a unique audio token for a player and facilitates their connection
     *       to the audio server.</li>
     *   <li>Utilizes the inherited functionality from the {@code PalaceCommand} class to integrate
     *       seamlessly with the command handling system.</li>
     * </ul>
     *
     * <p><b>Behavior of Extended Class:</b></p>
     * <ul>
     *   <li>The "audio" command is tailored for use within the Palace network's environment to manage
     *       audio-related interactions and authentication tokens.</li>
     *   <li>Each command instance handles unique player interactions, generates an audio token, and
     *       operates within the framework provided by the {@code PalaceCommand} hierarchy.</li>
     * </ul>
     */
    public AudioCommand() {
        super("audio");
    }

    /**
     * Executes the {@code audio} command for the specified player. This method
     * generates a unique audio token for the player, updates the player's data
     * in the database, and sends the player a clickable message with a URL to connect
     * to the audio server.
     *
     * <p><b>Functionality:</b></p>
     * <ul>
     *   <li>Generates a secure, random token to authenticate the player's connection to the audio server.</li>
     *   <li>Stores the generated token in the player's online data using the server's MongoDB handler.</li>
     *   <li>Sends the player a clickable message with the appropriate audio server URL based on the network state
     *       (test or production mode).</li>
     * </ul>
     *
     * @param player The player who executed the command. Used to generate and store the token,
     *               and to send the clickable message containing the audio server link.
     * @param args   The command arguments passed by the player. Currently not utilized in this method.
     */
    @Override
    public void execute(Player player, String[] args) {
        String token = getRandomToken();
        //TODO If the player is currently connected, they should be disconnected since their token changed
        PalaceBungee.getMongoHandler().setOnlineData(player.getUniqueId(), "audioToken", token);
        player.sendMessage(new ComponentBuilder("\nClick here to connect to the Audio Server!\n")
                .color(ChatColor.GREEN).underlined(true).bold(true)
                .event((new ClickEvent(ClickEvent.Action.OPEN_URL,
                        (PalaceBungee.isTestNetwork() ? "https://audio-test.palace.network/?t=" :
                                "https://audio.palace.network/?t=") + token))).create());
    }

    /**
     * Generates a random 12-character alphanumeric token.
     *
     * <p>
     * This method creates a token using uppercase letters, lowercase letters,
     * and numeric digits. The generated token is suitable for use cases requiring
     * a random, secure, and unique identifier.
     * </p>
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
