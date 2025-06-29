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
    @Getter private final Rank rank;
    @Getter private final RankTag tag;
    protected boolean tabComplete = false, tabCompletePlayers = false;

    public PalaceCommand(String name) {
        this(name, Rank.GUEST);
    }

    public PalaceCommand(String name, String... aliases) {
        this(name, Rank.GUEST, aliases);
    }

    public PalaceCommand(String name, Rank rank) {
        this(name, rank, (RankTag) null);
    }

    public PalaceCommand(String name, Rank rank, String... aliases) {
        this(name, rank, null, aliases);
    }

    public PalaceCommand(String name, Rank rank, RankTag tag, String... aliases) {
        super(name, "", aliases);
        this.rank = rank;
        this.tag = tag;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (!(sender instanceof ProxiedPlayer)) return false;
        Player player = PalaceBungee.getPlayer(((ProxiedPlayer) sender).getUniqueId());
        if (player == null) return false;
        return player.getRank().getRankId() >= rank.getRankId() || (tag != null && player.getTags().contains(tag));
    }

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

    public abstract void execute(Player player, String[] args);

    public Iterable<String> onTabComplete(Player player, List<String> args) {
        return new ArrayList<>();
    }
}
