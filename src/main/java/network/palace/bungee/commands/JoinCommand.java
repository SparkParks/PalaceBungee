package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.handlers.Server;

import java.util.*;
import java.util.logging.Level;

/**
 * The {@code JoinCommand} class allows players to join various servers within the context
 * of the Palace Minecraft server network. This command provides players with a list of
 * available servers, enables quick navigation through clickable server links, and manages
 * server transitions.
 *
 * <p>Features of this command include:</p>
 * <ul>
 *   <li>Command-based switching between servers.</li>
 *   <li>Tab completion for available server names.</li>
 *   <li>Validation to ensure that players cannot join a server they are already connected to.</li>
 *   <li>Error handling for issues encountered during server transitions.</li>
 *   <li>Dynamic generation of a clickable list of servers for easy navigation.</li>
 * </ul>
 *
 * <p>Functional flow of the {@code JoinCommand}:</p>
 * <ul>
 *   <li>Given a server name as an argument, the command checks whether the server exists
 *       and is available for players to join.</li>
 *   <li>If the player is already connected to the target server, a relevant error message
 *       is displayed.</li>
 *   <li>If the server exists and matches the given argument, the player is directed to the
 *       target server.</li>
 *   <li>An error message is displayed if the server is unavailable or a transition fails.</li>
 *   <li>If no valid server argument is provided, the command displays a clickable list of
 *       all available servers.</li>
 * </ul>
 *
 * <p>Tab completion functionality:</p>
 * <ul>
 *   <li>Provides a dynamically sorted list of available servers based on partial input.</li>
 *   <li>Filters server names to match the player's input.</li>
 *   <li>Displays the remaining available options as the player types.</li>
 * </ul>
 *
 * <p>Key internal methods include:</p>
 * <ul>
 *   <li>{@code exists(String s)}: Checks if a server exists in the defined list of servers.</li>
 *   <li>{@code endsInNumber(String s)}: Verifies if a server name ends with a numeric character.</li>
 *   <li>{@code formatName(String s)}: Formats a given server name for standardization and consistency.</li>
 * </ul>
 */
public class JoinCommand extends PalaceCommand {
    /**
     * A collection of predefined server names used within the application.
     *
     * <p>This list contains the names of various servers or server groups that
     * can be utilized for referencing or processing server-specific operations
     * within the system.</p>
     *
     * <h3>List of Servers:</h3>
     * <ul>
     *   <li>"Hub" - Represents the main hub server.</li>
     *   <li>"WDW" - Represents the Walt Disney World server.</li>
     *   <li>"MK/TTC" - Represents the Magic Kingdom/Transportation and Ticket Center server.</li>
     *   <li>"DHS/Epcot" - Represents the Disney's Hollywood Studios and Epcot server.</li>
     *   <li>"AK/Typhoon" - Represents the Animal Kingdom and Typhoon Lagoon server.</li>
     *   <li>"USO" - Represents the Universal Studios Orlando server.</li>
     *   <li>"Seasonal" - Represents the seasonal events server.</li>
     *   <li>"Creative" - Represents the creative server for player building activities.</li>
     * </ul>
     *
     * <p>This variable is declared as {@code private}, ensuring encapsulation, and
     * {@code static final}, indicating it is a constant shared across instances of
     * the class and cannot be modified after initialization.</p>
     */
    private static final LinkedList<String> servers = new LinkedList<>(Arrays.asList("Hub", "WDW", "MK/TTC", "DHS/Epcot", "AK/Typhoon", "USO", "Seasonal", "Creative"));

    /**
     * Constructs a new {@code JoinCommand} instance with the command name "join".
     * <p>
     * The JoinCommand is designed to allow players to join a specific server or
     * sub-server in a network environment. This command facilitates smooth
     * navigation across various server instances in the system.
     * </p>
     *
     * <h3>Features:</h3>
     * <ul>
     *   <li>Initializes the command with the name "join".</li>
     *   <li>Enables tab-completion for this command to assist users.</li>
     *   <li>Integrates into the {@code PalaceCommand} system as part of the framework.</li>
     * </ul>
     *
     * <p>The JoinCommand makes it easier for players to access specific servers by
     * providing an intuitive command for server navigation.</p>
     */
    public JoinCommand() {
        super("join");
        tabComplete = true;
    }

    /**
     * Executes the JoinCommand, allowing a player to join a specified server or view a list of available servers.
     * This method handles server availability checks, type formatting, and navigation for the player.
     *
     * <p> When a valid server type is provided in the arguments, this method manages the player's transition
     * to the server if it's available and they are not already on it. If the server is not available or the
     * player is already on it, appropriate feedback messages are sent to keep the player informed.
     *
     * <p> If no valid server type is provided, the player is presented with a clickable, hoverable list of
     * available servers, generated from the predefined list of server names.
     *
     * @param player The {@link Player} instance who invoked the command. This represents the player attempting
     *               to execute the server join operation or view the list of servers.
     * @param args   A {@link String} array of arguments provided by the player when executing the command.
     *               The first argument can specify a target server type or name.
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            if (exists(args[0])) {
                Server currentServer;
                if ((currentServer = PalaceBungee.getServerUtil().getServer(player.getServerName(), true)) != null && currentServer.getServerType().equalsIgnoreCase(args[0])) {
                    player.sendMessage(ChatColor.RED + "You are already on this server!");
                    return;
                }
                try {
                    String serverArg;
                    if (args[0].equals("WDW")) {
                        serverArg = "MK/TTC";
                    } else {
                        serverArg = args[0];
                    }
                    String type = formatName(serverArg);

                    Server server = PalaceBungee.getServerUtil().getServerByType(type);
                    if (server == null) {
                        player.sendMessage(ChatColor.RED + "No '" + type + "' server is available right now! Please try again soon.");
                        return;
                    }
                    server.join(player);
                } catch (Exception e) {
                    PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error sending player to server", e);
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
            Server server;
            if (endsInNumber(args[0]) && exists(args[0].substring(0, args[0].length() - 1)) &&
                    (server = PalaceBungee.getServerUtil().getServer(formatName(args[0]), true)) != null) {
                try {
                    PalaceBungee.getServerUtil().sendPlayer(player, server.getName());
                } catch (Exception e) {
                    PalaceBungee.getProxyServer().getLogger().log(Level.SEVERE, "Error sending player to server", e);
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
        }
        TextComponent top = new TextComponent(ChatColor.GREEN + "Here is a list of servers you can join: " +
                ChatColor.GRAY + "(Click to join)");
        player.sendMessage(top);
        for (String server : servers) {
            if (server.trim().isEmpty()) continue;
            TextComponent txt = new TextComponent(ChatColor.GREEN + "- " + ChatColor.AQUA + server);
            txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.GREEN + "Click to join the " + ChatColor.AQUA +
                            server + ChatColor.GREEN + " server!").create()));
            txt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + server));
            player.sendMessage(txt);
        }
    }

    /**
     * Determines whether a given string ends with a numeric character.
     * <p>
     * This method checks the last character of the input string to determine
     * if it is a digit (0-9). If the last character can be successfully
     * parsed as a number, the method returns {@code true}. Otherwise, it returns
     * {@code false}.
     * </p>
     *
     * @param s the input string to evaluate. It must not be {@code null}.
     *          If the string is empty, this method will return {@code false}.
     * @return {@code true} if the last character of the string is a number,
     *         {@code false} otherwise.
     */
    private boolean endsInNumber(String s) {
        try {
            Integer.parseInt(s.substring(s.length() - 1));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Checks if a given string exists in the list of servers, ignoring case sensitivity.
     *
     * <p>The method iterates through the {@code servers} list and compares each element
     * with the input string using a case-insensitive comparison. If a match is found,
     * the method returns {@code true}, otherwise {@code false} is returned.</p>
     *
     * @param s the string to check for existence in the list of servers.
     * @return {@code true} if the string exists in the list of servers (ignoring case),
     *         {@code false} otherwise.
     */
    private boolean exists(String s) {
        for (String server : servers) {
            if (server.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Formats the given string based on specific rules:
     * <ul>
     *   <li>If the string, after removing digits, has a length less than 4 and is not equal to "hub" (ignoring case),
     *       all characters of the string are converted to uppercase.</li>
     *   <li>Otherwise, the first character of the string and any character following a space
     *       are capitalized, while other characters remain unchanged.</li>
     * </ul>
     *
     * <p>This method modifies the input to create a consistent formatted output depending on
     * specific conditions, which may include stripping digits and adjusting character casing.</p>
     *
     * @param s the input string to be formatted
     * @return the formatted string with the applied rules
     */
    private String formatName(String s) {
        StringBuilder ns = new StringBuilder();
        String t = s.replaceAll("\\d", "");
        if (t.length() < 4 && !t.equalsIgnoreCase("hub")) {
            for (char c : s.toCharArray()) {
                ns.append(Character.toUpperCase(c));
            }
            return ns.toString();
        }
        Character last = null;
        for (char c : s.toCharArray()) {
            if (last == null) {
                last = c;
                ns.append(Character.toUpperCase(c));
                continue;
            }
            if (Character.toString(last).equals(" ")) {
                ns.append(Character.toUpperCase(c));
            } else {
                ns.append(c);
            }
            last = c;
        }
        return ns.toString();
    }

    /**
     * Provides a list of possible completions for a command argument based on the current input.
     *
     * <p>This method is primarily used to assist users by auto-completing their input during command usage.
     * The completion is determined by matching available server names that start with the currently
     * typed prefix (last argument in the input).</p>
     *
     * <p>The list of available server names (`servers`) is sorted before being matched, and the matches
     * are also sorted alphabetically before being returned.</p>
     *
     * @param sender The player who is requesting tab completion.
     * @param args A list of arguments currently entered by the player. The last argument is used
     *             as the prefix to filter the completions.
     * @return An {@code Iterable<String>} containing all possible completions for the last input argument,
     *         sorted alphabetically. If no argument is provided, all available completions are returned.
     */
    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = servers;
        Collections.sort(list);
        if (args.size() == 0) {
            return list;
        }
        List<String> l2 = new ArrayList<>();
        String arg = args.get(args.size() - 1);
        for (String s : list) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                l2.add(s);
            }
        }
        Collections.sort(l2);
        return l2;
    }
}