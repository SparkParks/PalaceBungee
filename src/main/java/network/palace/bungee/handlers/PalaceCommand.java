package network.palace.bungee.handlers;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import network.palace.bungee.PalaceBungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a base command framework to be used within the Palace server environment.
 * <p>
 * The {@code PalaceCommand} class serves as an abstract implementation for custom commands
 * that are dependent on the rank and tag of the player executing the command.
 * It integrates permission checking, execution logic, and tab completion functionality.
 * <p>
 * It extends from the {@link Command} interface and implements the {@link TabExecutor}.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Permission check based on player rank and optional rank tags.</li>
 *     <li>Delegates execution to custom command logic via the abstract {@link #execute(Player, String[])} method.</li>
 *     <li>Supports tab-completion with optional player-name suggestions.</li>
 * </ul>
 *
 * <h2>Constructor Overview:</h2>
 * <p>
 * Constructors allow you to define the command with optional rank and tag restrictions, as well as aliases.
 * Default permission is set to {@code Rank.GUEST} if not specified.
 *
 * <h2>Key Methods:</h2>
 * <ul>
 *     <li>{@link #hasPermission(CommandSender)} - Determines if the sender has the necessary permissions.</li>
 *     <li>{@link #execute(Player, String[])} - Abstract method to be implemented with custom command behavior.</li>
 *     <li>{@link #onTabComplete(Player, List)} - Allows custom tab-completion logic.</li>
 *     <li>{@link #execute(CommandSender, String[])} - Handles the execution logic with permission checks.</li>
 *     <li>{@link #onTabComplete(CommandSender, String[])} - Handles tab-completion for commands with optional player-name suggestions.</li>
 * </ul>
 *
 * <h2>Rank and Tag Restriction:</h2>
 * <p>
 * {@code PalaceCommand} enforces restrictions based on:
 * <ul>
 *     <li>{@code rank} - Minimum rank required to execute the command.</li>
 *     <li>{@code tag} - Optional rank-based tag required for access.</li>
 * </ul>
 *
 * <h2>Tab Completion:</h2>
 * <p>
 * Tab completion is configured using the {@code tabComplete} and {@code tabCompletePlayers} flags:
 * <ul>
 *     <li>When {@code tabCompletePlayers} is true, it suggests player names as part of the completion.</li>
 *     <li>{@link #onTabComplete(Player, List)} can be overridden to define custom tab completion behavior.</li>
 * </ul>
 *
 * <p>
 * Developers must implement the {@link #execute(Player, String[])} method to define specific
 * behavior for commands derived from {@code PalaceCommand}.
 */
public abstract class PalaceCommand extends Command implements TabExecutor {
    /**
     * Represents the minimum required {@link Rank} necessary to execute this command.
     * <p>
     * This field stores the specific rank that grants the permission to use the command.
     * It plays a crucial role in determining access control, ensuring that only users
     * meeting or exceeding the specified rank can perform certain actions.
     * </p>
     * <p>
     * The {@link Rank} enum defines a hierarchy of roles with associated properties, such as
     * name, colors for tags and chat, whether they possess admin privileges, and an identifier.
     * This allows precise definition and enforcement of role-based permissions.
     * </p>
     * <p>
     * Typical uses of this field include:
     * <ul>
     *     <li>Authorizing command execution.</li>
     *     <li>Restricting access based on user rank within a system.</li>
     *     <li>Dictating supplementary visual or functional differentiations based on ranks.</li>
     * </ul>
     * </p>
     */
    @Getter private final Rank rank;

    /**
     * <p>Represents the {@link RankTag} associated with a {@link PalaceCommand}.
     * The tag defines additional metadata or categorization for commands,
     * often tied to a specific rank or identifier within the system.</p>
     *
     * <p>This is a fixed, final value, ensuring the tag is unalterable once set upon
     * object construction within the command.</p>
     *
     * <ul>
     *   <li>{@code RankTag} is enumerated to provide predefined standardized tags
     *       with specific attributes.</li>
     *   <li>Supports visual representation and categorization using associated
     *       {@code name}, {@code color}, and symbolic tag values.</li>
     * </ul>
     */
    @Getter private final RankTag tag;

    /**
     * Indicates whether tab completion is enabled for this command.
     *
     * <p>The {@code tabComplete} variable determines if this command should provide
     * suggestions when a user initiates tab completion during command input.</p>
     *
     * <p>If set to {@code true}, tab completion is enabled, and suggestions can be
     * provided by overriding the {@code onTabComplete} method. If set to {@code false},
     * no suggestions will be provided for this command.</p>
     *
     * <ul>
     *   <li>Applies to commands managed by the {@code PalaceCommand} class.</li>
     *   <li>Works in conjunction with the methods related to tab completion,
     *   such as {@code onTabComplete}.</li>
     * </ul>
     */
    protected boolean tabComplete = false, /**
     * Represents a configuration flag for enabling or disabling player-specific
     * tab completion functionality within a command execution context.
     * <p>
     * This boolean field controls whether the tab completion suggestions will
     * include or target player names. It can be enabled or disabled depending
     * on the requirements for the relevant command being executed.
     * </p>
     * <p>
     * <b>Usage considerations:</b>
     * <ul>
     *   <li>When set to <code>true</code>, player names will be included in tab completion suggestions.</li>
     *   <li>When set to <code>false</code>, player names will not be included in tab completion suggestions.</li>
     * </ul>
     * </p>
     * <p>
     * Typically used in conjunction with a command's tab completion logic to
     * modify the behavior of command suggestions for the user executing the
     * command.
     * </p>
     */
    tabCompletePlayers = false;

    /**
     * Constructs a new {@code PalaceCommand} with the specified name.
     * This constructor assigns the default rank {@code Rank.GUEST} to the command.
     *
     * @param name the name of the command
     */
    public PalaceCommand(String name) {
        this(name, Rank.GUEST);
    }

    /**
     * Constructs a {@code PalaceCommand} with the specified name and aliases,
     * defaulting the rank to {@code Rank.GUEST}.
     * <p>
     * This constructor is used when no specific rank is required for the command.
     * It initializes the command with the default rank {@code Rank.GUEST} and
     * assigns optional aliases for the command.
     *
     * @param name    the primary name of the command. This is the default identifier
     *                for invoking the command and must not be null or empty.
     * @param aliases optional additional names (aliases) for the command. These
     *                are alternative identifiers that can invoke the command.
     */
    public PalaceCommand(String name, String... aliases) {
        this(name, Rank.GUEST, aliases);
    }

    /**
     * Constructs a new {@code PalaceCommand} object with the specified name and rank.
     * <p>
     * The {@code PalaceCommand} is initialized with a given name and associated {@link Rank},
     * while the {@link RankTag} is set to {@code null}.
     *
     * @param name the name of the command
     * @param rank the required rank to execute this command
     */
    public PalaceCommand(String name, Rank rank) {
        this(name, rank, (RankTag) null);
    }

    /**
     * Constructs a new instance of the {@code PalaceCommand} class with a specified name, rank, and optional aliases.
     *
     * <p>This constructor sets the command's name and rank, while also allowing for one or more aliases to be specified.</p>
     *
     * @param name    The name of the command. This serves as the primary identifier for the command.
     * @param rank    The minimum {@code Rank} required to execute this command. Determines the permission level.
     * @param aliases Zero or more alternative names (aliases) for the command. These can be used to invoke the command as alternatives to its primary name.
     */
    public PalaceCommand(String name, Rank rank, String... aliases) {
        this(name, rank, null, aliases);
    }

    /**
     * Constructs a new {@code PalaceCommand} object.
     * <p>
     * This constructor initializes the command with a specific name, required rank, associated rank tag,
     * and any aliases for the command.
     * </p>
     *
     * @param name     The name of the command.
     * @param rank     The {@link Rank} required to execute this command.
     * @param tag      The associated {@link RankTag} providing additional grouping or informational context.
     * @param aliases  Optional aliases for the command to allow alternative names.
     */
    public PalaceCommand(String name, Rank rank, RankTag tag, String... aliases) {
        super(name, "", aliases);
        this.rank = rank;
        this.tag = tag;
    }

    /**
     * Checks whether the given sender has the required permission to execute the command.
     * <p>
     * The permission check is based on the sender's rank and optional tag.
     * </p>
     *
     * @param sender the source of the command. This must be an instance of {@link ProxiedPlayer}.
     *               If it is not, or if the associated {@link Player} instance cannot be resolved,
     *               the method will return {@code false}.
     * @return {@code true} if the sender has the required rank or possesses the associated tag (if applicable),
     *         otherwise {@code false}.
     */
    @Override
    public boolean hasPermission(CommandSender sender) {
        if (!(sender instanceof ProxiedPlayer)) return false;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) sender).getUniqueId());
        if (player == null) return false;
        return player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag));
    }

    /**
     * Executes the given command when invoked by a {@link CommandSender}. This method ensures
     * that the sender is a {@link ProxiedPlayer} and performs necessary checks for permissions
     * based on rank or tag requirements. If the sender meets the requirements, the abstract
     * {@code execute(Player, String[])} method is called, allowing subclass-specific command logic
     * to be executed.
     *
     * <p>If the player does not meet the required permissions, an error message is sent
     * to the player.</p>
     *
     * @param commandSender The {@link CommandSender} who issued the command. This method checks
     *                      if the sender is a {@link ProxiedPlayer}.
     * @param strings       An array of {@link String} arguments provided with the command. These
     *                      arguments can be used by subclasses to carry out specific logic.
     */
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof ProxiedPlayer)) return;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) commandSender).getUniqueId());
        if (player == null) return;
        if (player.isDisabled() && !getName().equals("staff")) return;
        if (player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag))) {
            // either player meets the rank requirement, or the tag requirement
            execute(player, strings);
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to perform this command!");
        }
    }

    /**
     * Handles the tab completion logic for a command, providing a list of possible suggestions
     * based on the user input and the player's permissions or rank.
     *
     * <p>This method determines if the command sender is eligible for tab completion
     * and processes player names or custom tab completions accordingly.</p>
     *
     * @param commandSender The sender of the command, expected to be a {@code ProxiedPlayer}.
     * @param args An array of strings representing the arguments provided by the command sender.
     *             The last element is typically the current input being completed.
     * @return An {@code Iterable<String>} containing a list of tab-completion suggestions.
     *         If no suggestions are available or the sender lacks permission, an empty list is returned.
     */
    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (!tabComplete || !(commandSender instanceof ProxiedPlayer)) return new ArrayList<>();
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) commandSender).getUniqueId());
        if (player == null) return new ArrayList<>();
        if (player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag))) {
            if (tabCompletePlayers) {
                List<String> list = new ArrayList<>(PalaceBungee.getServerUtil().getOnlinePlayerNames());
                if (args.length > 0) {
                    String arg2 = args[args.length - 1];
                    List<String> l2 = new ArrayList<>();
                    for (String s : list) {
                        if (s.toLowerCase().startsWith(arg2.toLowerCase())) {
                            l2.add(s);
                        }
                    }
                    Collections.sort(l2);
                    return l2;
                } else {
                    Collections.sort(list);
                    return list;
                }
            } else {
                return onTabComplete(player, Arrays.asList(args));
            }
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Executes a specific command with the provided player and arguments.
     * <p>
     * This method is designed to be overridden in subclasses to define custom command behavior.
     * </p>
     *
     * @param player the player executing the command. Cannot be null.
     * @param args   an array of strings representing the command arguments.
     *               <ul>
     *                   <li>Arguments may vary in length depending on the specific command.</li>
     *                   <li>May include subcommands, parameters, or other input values.</li>
     *               </ul>
     */
    public abstract void execute(Player player, String[] args);

    /**
     * Handles tab completion for command input by the specified player. The method allows
     * the command system to provide a list of suggestions for the user based on their
     * current input arguments.
     *
     * <p>This method is designed to be invoked when a player is typing a command and
     * presses the tab key, which triggers a request for possible completions.
     *
     * @param player The {@link Player} object representing the player who is entering
     * the command and requesting tab completion suggestions.
     * @param args   A {@link List} of {@link String} representing the current input arguments
     * the player has typed so far. This helps determine the appropriate suggestions
     * to provide.
     *
     * @return An {@link Iterable} of {@link String} containing possible suggestions for
     * the player's next input. The returned collection may be empty if no suggestions
     * are applicable.
     */
    public Iterable<String> onTabComplete(Player player, List<String> args) {
        return new ArrayList<>();
    }
}
