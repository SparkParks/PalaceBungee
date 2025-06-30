package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.utils.DateUtil;

/**
 * The <code>UptimeCommand</code> class represents a command within the PalaceCommand system
 * that provides the uptime duration of the BungeeCord proxy. This command is triggered
 * by the "uptime" keyword.
 *
 * <p>The command, when executed, sends a message to the player detailing how long
 * the proxy has been online since its start time.</p>
 *
 * <p><b>Primary Functionality:</b></p>
 * <ul>
 *     <li>Obtains the current uptime of the BungeeCord proxy.</li>
 *     <li>Sends the formatted uptime duration to the player who executed the command.</li>
 * </ul>
 *
 * <p>This class inherits from the {@link PalaceCommand} base class and overrides
 * the {@code execute} method to define its specific functionality.</p>
 *
 * <p><b>Command Details:</b></p>
 * <ul>
 *     <li>Command Name: "uptime"</li>
 *     <li>Function: Provides details about the proxy's uptime.</li>
 * </ul>
 *
 * <p><b>Method Details:</b></p>
 * <ul>
 *     <li><code>execute(Player player, String[] args)</code>: Sends the formatted uptime message to the player.</li>
 * </ul>
 */
public class UptimeCommand extends PalaceCommand {

    public UptimeCommand() {
        super("uptime");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(new ComponentBuilder("\nThis BungeeCord proxy has been online for " +
                DateUtil.formatDateDiff(PalaceBungee.getStartTime())).color(ChatColor.GREEN).create());
    }
}