package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * The {@code MentionsCommand} class represents a command that allows players
 * to toggle the mention notifications in the game.
 * <p>
 * This command enables or disables the mention notification feature for a player.
 * When the command is executed, it toggles the current state of the mentions
 * setting, updates the player's preferences, and modifies the corresponding
 * database setting for persistence.
 * <p>
 * Usage of the command updates the player with a message indicating whether the
 * notifications have been enabled or disabled.
 *
 * <p><b>Command:</b> "mentions"</p>
 *
 * <h3>Functionality:</h3>
 * <ul>
 *  <li>Toggles the mention notifications for the executing player.</li>
 *  <li>Provides visual feedback to the player about the updated state of the
 *      mention notifications via colored messages.</li>
 *  <li>Updates the player's mention setting both in memory and in persistent
 *      storage.</li>
 * </ul>
 */
public class MentionsCommand extends PalaceCommand {

    /**
     * Constructs a new {@code MentionsCommand} instance with the command name "mentions".
     * <p>
     * The MentionsCommand allows players to toggle their in-game mention notifications.
     * When executed, it modifies the player's setting to enable or disable the
     * notification feature and updates both the in-memory state and the persistent
     * database record for the player.
     * </p>
     *
     * <h3>Purpose:</h3>
     * <ul>
     *   <li>Toggles the mention notification feature for the player who invoked the command.</li>
     *   <li>Provides feedback through a colored chat message indicating the updated state
     *       of the mentions (enabled or disabled).</li>
     *   <li>Ensures data consistency by updating the player's settings in persistent storage.</li>
     * </ul>
     *
     * <p>This command improves user experience by allowing players to customize their notification
     * settings dynamically during gameplay. The feedback message clearly communicates the action
     * taken, ensuring the user is informed of their new settings.</p>
     */
    public MentionsCommand() {
        super("mentions");
    }

    /**
     * Executes the "mentions" command, allowing the player to toggle their mention
     * notifications on or off.
     * <p>
     * This method updates the player's mention notification preference, displays a
     * feedback message about the new preference state, and persists the updated
     * setting to the database.
     *
     * <h3>Functionality:</h3>
     * <ul>
     *  <li>Toggles the player's mention notifications state.</li>
     *  <li>Displays a message indicating whether notifications have been enabled or
     *      disabled, with color-coded feedback.</li>
     *  <li>Synchronizes the updated preference with the database.</li>
     * </ul>
     *
     * @param player the player executing the command, whose mention notification
     *               settings will be toggled.
     * @param args   the arguments passed to the command, though they are unused
     *               in this implementation.
     */
    @Override
    public void execute(final Player player, String[] args) {
        player.setMentions(!player.hasMentions());
        player.sendMessage((player.hasMentions() ? ChatColor.GREEN : ChatColor.RED) + "You have " +
                (player.hasMentions() ? "enabled" : "disabled") + " mention notifications!");
        if (player.hasMentions()) {
            player.mention();
        }
        PalaceBungee.getMongoHandler().setSetting(player.getUniqueId(), "mentions", player.hasMentions());
    }
}