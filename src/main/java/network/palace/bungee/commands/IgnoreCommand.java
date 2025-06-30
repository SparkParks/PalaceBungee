package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Rank;
import network.palace.bungee.messages.packets.IgnoreListPacket;

import java.text.DateFormatSymbols;
import java.util.*;
import java.util.logging.Level;

/**
 * The {@code IgnoreCommand} class provides functionality for players to manage their ignore
 * lists within the game. Players can execute this command to ignore or unignore others, view
 * a list of players they have ignored, or access a help menu detailing the syntax of available
 * options.
 *
 * <p>The command supports the following subcommands:
 * <ul>
 *   <li>{@code list} - Displays the list of currently ignored players. Optional support for
 *   paginated output.</li>
 *   <li>{@code add [player]} - Adds a specified player to the ignore list, preventing their messages
 *   from being visible.</li>
 *   <li>{@code remove [player]} - Removes a specified player from the ignore list, allowing their
 *   messages to be visible again.</li>
 *   <li>{@code help} - Displays an informational help menu outlining the available subcommands.</li>
 * </ul>
 *
 * <p>On certain servers, such as "Creative", the ignore list changes may be synchronized to ensure
 * consistency across different parts of the application. Errors that occur during this syncing process
 * are logged, and the player is notified appropriately.
 *
 * <p>The class also includes functionality to provide paginated display for the {@code list} subcommand,
 * ensuring players can navigate longer lists of ignored users effectively.
 *
 * <p>Features:
 * <ul>
 *   <li>Manage ignored players list (add, remove, view).</li>
 *   <li>Synchronize ignore list with compatible servers.</li>
 *   <li>Robust error handling and user notifications.</li>
 *   <li>Help menu for ease of use and clarification of command syntax.</li>
 * </ul>
 */
public class IgnoreCommand extends PalaceCommand {

    /**
     * Represents the IgnoreCommand in the PalaceCommand system.
     * <p>
     * The IgnoreCommand is designed to allow players to ignore other players
     * within the system. This command provides functionality to manage player-to-player
     * interactions by preventing communication from certain players that the user has chosen
     * to ignore.
     * </p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Enables players to block messages from specific individuals.</li>
     *   <li>Enhances user experience by allowing better control over received communications.</li>
     *   <li>Integrates seamlessly into the system's suite of commands.</li>
     * </ul>
     *
     * <p>This class extends the {@code PalaceCommand} base class and serves as the entry
     * point to initialize the ignore feature through the designated command.</p>
     */
    public IgnoreCommand() {
        super("ignore");
    }

    /**
     * Executes the ignore command based on player input. This command allows players to:
     * <ul>
     *     <li>View a list of ignored players</li>
     *     <li>Add a player to their ignore list</li>
     *     <li>Remove a player from their ignore list</li>
     * </ul>
     *
     * <p>Subcommands:</p>
     * <ul>
     *     <li><b>list</b>: Displays the list of ignored players. Optionally accepts a page number as a second argument to paginate results.</li>
     *     <li><b>add</b>: Adds a player to the ignore list. Requires the target player's username as the argument.</li>
     *     <li><b>remove</b>: Removes a player from the ignore list. Requires the target player's username as the argument.</li>
     * </ul>
     *
     * <p>If the first argument does not match any recognized subcommands or is invalid, a help menu will be displayed to the player.</p>
     *
     * @param player The player executing the command. This is the command sender who will interact with their ignore list.
     * @param args   The arguments provided for execution. The first element determines the subcommand, while subsequent elements may provide additional information such as player
     *  names or page numbers.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            helpMenu(player);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "list": {
                List<UUID> list = player.getIgnored();
                List<String> names = new ArrayList<>();
                for (UUID uuid : list) {
                    names.add(PalaceBungee.getUsername(uuid));
                }
                if (names.isEmpty()) {
                    player.sendMessage(ChatColor.GREEN + "No ignored players!");
                    return;
                }
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
                names.sort(Comparator.comparing(String::toLowerCase));
                int listSize = names.size();
                int maxPage = (int) Math.ceil((double) listSize / 8);
                if (page > maxPage) page = maxPage;
                int startAmount = 8 * (page - 1);
                int endAmount;
                if (maxPage > 1) {
                    if (page < maxPage) {
                        endAmount = (8 * page);
                    } else {
                        endAmount = listSize;
                    }
                } else {
                    endAmount = listSize;
                }
                names = names.subList(startAmount, endAmount);
                StringBuilder msg = new StringBuilder(ChatColor.YELLOW + "Ignored Players (Page " + page + " of " + maxPage + "):\n");
                for (String name : names) {
                    msg.append(ChatColor.AQUA).append("- ").append(ChatColor.YELLOW).append(name).append("\n");
                }
                player.sendMessage(msg.toString());
                break;
            }
            case "add": {
                if (args.length < 2) {
                    helpMenu(player);
                    return;
                }
                if (args[1].equalsIgnoreCase(player.getUsername())) {
                    player.sendMessage(ChatColor.RED + "You can't ignore yourself!");
                    return;
                }
                String name;
                UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                Rank rank = PalaceBungee.getMongoHandler().getRank(uuid);
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    player.sendMessage(ChatColor.RED + "You can't ignore that player!");
                    return;
                }
                name = PalaceBungee.getUsername(uuid);
                player.setIgnored(uuid, true);
                PalaceBungee.getMongoHandler().ignorePlayer(player, uuid);
                player.sendMessage(ChatColor.GREEN + "You have ignored " + name);
                if (player.getServerName().equals("Creative")) {
                    try {
                        PalaceBungee.getMessageHandler().sendDirectServerMessage(new IgnoreListPacket(player.getUniqueId(), player.getIgnored()), "Creative");
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "An error occurred while syncing your ignore list with Creative. Any changes you made recently may not take effect immediately. If you encounter further issues, log out and reconnect to Palace Network.");
                        PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error syncing ignore list with Creative", e);
                    }
                }
                break;
            }
            case "remove": {
                if (args.length < 2) {
                    helpMenu(player);
                    return;
                }
                String name;
                UUID uuid = PalaceBungee.getMongoHandler().usernameToUUID(args[1]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                name = PalaceBungee.getUsername(uuid);
                player.setIgnored(uuid, false);
                PalaceBungee.getMongoHandler().unignorePlayer(player, uuid);
                player.sendMessage(ChatColor.GREEN + "You have unignored " + name);
                if (player.getServerName().equals("Creative")) {
                    try {
                        PalaceBungee.getMessageHandler().sendDirectServerMessage(new IgnoreListPacket(player.getUniqueId(), player.getIgnored()), "Creative");
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "An error occurred while syncing your ignore list with Creative. Any changes you made recently may not take effect immediately. If you encounter further issues, log out and reconnect to Palace Network.");
                        PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error syncing ignore list with Creative", e);
                    }
                }
                break;
            }
            default: {
                helpMenu(player);
                break;
            }
        }
    }

    /**
     * Formats a timestamp into a readable date and time string specific to the "America/New_York" timezone.
     * <p>
     * The method converts the provided timestamp into a formatted string representing the date and time
     * in a user-friendly style. The date includes the abbreviated month name, day, and year, while the
     * time includes the hour (in 12-hour format), minutes, and an "am/pm" indicator.
     * </p>
     *
     * <h3>Format:</h3>
     * <ul>
     *   <li>Month: Abbreviated to the first three letters (e.g., Jan, Feb).</li>
     *   <li>Day: Day of the month.</li>
     *   <li>Year: The full year.</li>
     *   <li>Time: 12-hour format with leading zeros for minutes when necessary, followed by "am/pm".</li>
     * </ul>
     *
     * @param started the timestamp in milliseconds since the epoch (January 1, 1970, 00:00:00 GMT)
     *                to be formatted.
     * @return a string representing the formatted date and time in the "America/New_York" timezone.
     */
    private String format(long started) {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date(started));
        c.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String am = "am";
        if (hour > 12) {
            am = "pm";
            hour -= 12;
        } else if (hour == 0) {
            hour += 12;
        }
        String month = new DateFormatSymbols().getMonths()[c.get(Calendar.MONTH)].substring(0, 3);
        String min = String.valueOf(c.get(Calendar.MINUTE));
        if (min.length() < 2) {
            min = "0" + min;
        }
        return month + " " + c.get(Calendar.DAY_OF_MONTH) + " " +
                c.get(Calendar.YEAR) + " at " + hour + ":" + min + am;
    }

    /**
     * Displays the help menu for the ignore command to the specified player.
     * The help menu provides details on how to use ignore commands.
     *
     * <p><b>Ignore Commands:</b></p>
     * <ul>
     *   <li><b>/ignore list [page]</b> - Lists ignored players.</li>
     *   <li><b>/ignore add [player]</b> - Ignores a specified player.</li>
     *   <li><b>/ignore remove [player]</b> - Unignores a specified player.</li>
     *   <li><b>/ignore help</b> - Displays this help menu.</li>
     * </ul>
     *
     * @param player The player to whom the help menu will be displayed.
     */
    public void helpMenu(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Use /ignore to hide messages from players\n" +
                ChatColor.GREEN + "Ignore Commands:\n" + ChatColor.YELLOW + "/ignore list [page] " +
                ChatColor.AQUA + "- List ignored players\n" + ChatColor.YELLOW + "/ignore add [player] " +
                ChatColor.AQUA + "- Ignore a player\n" + ChatColor.YELLOW + "/ignore remove [player] " +
                ChatColor.AQUA + "- Unignore a player\n" + ChatColor.YELLOW + "/ignore help " +
                ChatColor.AQUA + "- Show this help menu");
    }
}