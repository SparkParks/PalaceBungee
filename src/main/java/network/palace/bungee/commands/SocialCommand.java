package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;

/**
 * The {@code SocialCommand} class extends {@link PalaceCommand}
 * and provides a command to display social media links for the Palace Network.
 * <p>
 * The class sends a formatted list of clickable social media links, each
 * styled with colors and hover text, to the player when executed.
 * </p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Displays clickable links to various social networks, such as:
 *       <ul>
 *           <li>Forums</li>
 *           <li>Discord</li>
 *           <li>Twitter</li>
 *           <li>YouTube</li>
 *           <li>Twitch</li>
 *       </ul>
 *   </li>
 *   <li>Contains hover effects with additional descriptions for each link.</li>
 *   <li>Uses {@link ComponentBuilder}, {@link ChatColor}, {@link HoverEvent},
 *       and {@link ClickEvent} to implement message formatting and interactivity.</li>
 * </ul>
 *
 * <p>This command is executed by a player typing the relevant keyword, displaying
 * the social media links directly in the player's chat with interactive functionality.</p>
 */
public class SocialCommand extends PalaceCommand {
    /**
     * Represents a collection of informative messages with interactive components designed
     * for sharing social and community links related to the Palace Network.
     *
     * <p>The {@code message} variable is a pre-constructed array of {@code BaseComponent}
     * objects that display links to community resources, such as forums, Discord, social media
     * accounts, and streaming platforms, within the in-game chat system. The links include
     * interactive features such as hover text and clickable URLs to enhance user engagement.</p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Provides clickable URLs for quick access to external platforms.</li>
     *   <li>Hover effects display additional informational text for better context.</li>
     *   <li>Uses distinct colors for each link to visually differentiate them.</li>
     * </ul>
     *
     * <h3>Interactive Links Included:</h3>
     * <ul>
     *   <li><span style="color:green;">Forums</span>: Links to the Palace Network forums.</li>
     *   <li><span style="color:lightpurple;">Discord</span>: Connects to the Palace Network Discord server.</li>
     *   <li><span style="color:aqua;">Twitter</span>: Links to the official Palace Network Twitter account.</li>
     *   <li><span style="color:red;">YouTube</span>: Points to the Palace Network's YouTube channel.</li>
     *   <li><span style="color:red;">Twitch</span>: Directs to the Palace Network's Twitch streaming channel.</li>
     * </ul>
     *
     * <p>This variable is primarily used to provide dynamic and visually appealing social
     * information through chat, allowing players to engage with the community
     * outside of the in-game environment.</p>
     */
    static BaseComponent[] message = new ComponentBuilder("Forums: https://forums.palace.network\n").color(ChatColor.GREEN).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://forums.palace.network").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://forums.palace.network"))
            .append("Discord: https://palnet.us/Discord\n").color(ChatColor.LIGHT_PURPLE).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/Discord").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord"))
            .append("Twitter: @PalaceNetwork\n").color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://twitter.com/PalaceNetwork").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitter.com/PalaceNetwork"))
            .append("YouTube: https://youtube.com/MCMagicParks\n").color(ChatColor.RED).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://youtube.com/MCMagicParks").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://youtube.com/MCMagicParks"))
            .append("Twitch: https://www.twitch.tv/palacenetwork\n").color(ChatColor.RED).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://www.twitch.tv/palacenetwork").color(ChatColor.GREEN).create())).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.twitch.tv/palacenetwork"))
            .create();

    /**
     * Represents the SocialCommand in the PalaceCommand system.
     * <p>
     * The SocialCommand is designed to provide players with a way to access
     * social media links or related resources directly through an in-game command.
     * </p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Accessible through the command name "social".</li>
     *   <li>Provides integration to direct users to external social platforms
     *       or informational links.</li>
     *   <li>Enhances player interaction by offering a streamlined way to connect
     *       to external resources.</li>
     * </ul>
     *
     * <p>This class extends the {@code PalaceCommand} base class and serves
     * as a specific implementation of a command within the system.</p>
     */
    public SocialCommand() {
        super("social");
    }

    /**
     * Executes the command associated with displaying social links to the player.
     *
     * <p>This method is invoked when a player executes this specific command,
     * and it sends a formatted message with social links to the player.</p>
     *
     * @param player the {@link Player} who executed the command. This represents the individual
     *               receiving the social links message.
     * @param args   an array of {@link String} arguments provided with the command.
     *               These arguments may be used to further handle or validate the command input.
     */
    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "\nPalace Network Social Links:");
        player.sendMessage(message);
    }
}