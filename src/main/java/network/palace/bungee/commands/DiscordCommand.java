package network.palace.bungee.commands;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.protocol.packet.Chat;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.mongo.MongoHandler;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

/**
 * The {@code DiscordCommand} class extends the {@code PalaceCommand} and represents
 * a command to handle Discord-related interactions within a Minecraft server environment.
 * This command allows players to access Discord-related resources or manage
 * Discord account linking/unlinking processes.
 *
 * <p><b>Command Overview:</b></p>
 * <ul>
 *   <li>Command Name: {@code discord}</li>
 *   <li>Subcommands:
 *     <ul>
 *       <li>{@code link} - Starts the process of linking a Discord account to the player's Minecraft account.</li>
 *       <li>{@code unlink} - Unlinks an already linked Discord account from the player's Minecraft account.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Shows a clickable message with information about Discord.</li>
 *   <li>Performs account linking by providing an OAuth2 authorization link.</li>
 *   <li>Allows players to unlink their existing Discord accounts.</li>
 *   <li>Uses hover and click events to provide an interactive chat experience.</li>
 *   <li>Includes URL redirection for linking and unlinking processes.</li>
 * </ul>
 *
 * <p><b>Command Behavior:</b></p>
 * <ul>
 *   <li>When no arguments are supplied, a default message with Discord information is displayed.</li>
 *   <li>When the {@code link} subcommand is issued:
 *     <ul>
 *       <li>If the Discord account is already linked, a message displays the relevant information.</li>
 *       <li>If not linked, provides an interactive message to start the linking process via a URL.</li>
 *     </ul>
 *   </li>
 *   <li>When the {@code unlink} subcommand is issued:
 *     <ul>
 *       <li>Removes any existing Discord account linkage.</li>
 *       <li>Prompts the user to restart the linking process if needed.</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class DiscordCommand extends PalaceCommand {
    public DiscordCommand() {
        super("discord");
    }

    private static final BaseComponent[] message = new ComponentBuilder("\nClick for more information about Discord!\n").color(ChatColor.YELLOW).bold(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click to open ").color(ChatColor.AQUA).append("https://palnet.us/Discord").color(ChatColor.GREEN).create()))
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord")).create();

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(message);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "link": {
                boolean isLinked = PalaceBungee.getMongoHandler().verifyDiscordLink(player.getUniqueId());

                BaseComponent[] linkMessage = new ComponentBuilder("\nClick to start linking your Discord account.\n").color(ChatColor.YELLOW).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Opens a browser window to start the discord linking process.").color(ChatColor.GREEN).create()))
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.com/api/oauth2/authorize?client_id=543141358496383048&redirect_uri=https%3A%2F%2Finternal-api.palace.network%2Fdiscord%2Flink&response_type=code&scope=identify&state="
                + Base64.getEncoder().encodeToString(player.getUniqueId().toString().getBytes()) + "")).create();

                if (isLinked) {
                    player.sendMessage(ChatColor.GREEN + "Hey " + ChatColor.YELLOW + ChatColor.BOLD + player.getUsername() + ChatColor.GREEN + " you currently have a discord account linked. To unlink, run " + ChatColor.YELLOW + ChatColor.BOLD + "/discord unlink");
                } else {
                    player.sendMessage(linkMessage);
                }
                return;
            }
            case "unlink": {
                PalaceBungee.getMongoHandler().removeDiscordLink(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have successfully unlinked your discord account. Please run " + ChatColor.YELLOW + ChatColor.BOLD +  "/discord link" + ChatColor.GREEN + " to restart the linking process");
            }
        }
    }
}
