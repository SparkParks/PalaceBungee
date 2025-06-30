package network.palace.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import network.palace.bungee.PalaceBungee;
import network.palace.bungee.handlers.PalaceCommand;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.utils.DateUtil;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * The {@code PunishmentsCommand} class is responsible for managing and displaying
 * punishment history details for a player. This includes showing summaries, bans,
 * mutes, kicks, and warnings applied to the player.
 *
 * <p>This class extends {@code PalaceCommand} and overrides the {@code execute} method
 * to handle player commands for retrieving punishment-related data. The punishment data
 * is fetched from a backend database using {@code PalaceBungee.getMongoHandler()}.
 *
 * <p>The command syntax is: <code>/punishments [Summary/Bans/Mutes/Kicks/Warns]</code>.
 * If no arguments are provided or an invalid section is specified, a usage message is
 * displayed to the player. Valid sections are:
 *
 * <ul>
 *   <li><b>summary:</b> Displays an overview of all punishments, including the count of
 *       bans, mutes, kicks, and warnings.</li>
 *   <li><b>bans:</b> Provides detailed ban history, including reasons, start time, and
 *       expiration details.</li>
 *   <li><b>mutes:</b> Provides detailed mute history, including reasons, start time,
 *       expiration details, and active status.</li>
 *   <li><b>kicks:</b> Displays the history of player kicks, showing the reason and time
 *       of each kick.</li>
 *   <li><b>warns/warnings:</b> Lists all warnings issued to the player, including the
 *       reason and time of issuance.</li>
 * </ul>
 *
 * <p>The command behavior is defined as follows:
 * <ul>
 *   <li>If the section is "summary", a quick overview of the player's punishment counts
 *       is displayed.</li>
 *   <li>For "bans", "mutes", "kicks", or "warns/warnings", detailed punishment data is
 *       retrieved and formatted for display.</li>
 *   <li>If the specified section does not exist, a usage message is displayed.</li>
 * </ul>
 *
 * <p>The punishment details fetched include:
 * <ul>
 *   <li><b>Reason:</b> The reason for the ban, mute, kick, or warning.</li>
 *   <li><b>Start time:</b> The time the punishment was issued.</li>
 *   <li><b>Expires:</b> The time the punishment expires, if applicable.</li>
 *   <li><b>Length:</b> The duration of the punishment, formatted.</li>
 *   <li><b>Status:</b> Indicates whether the punishment is permanent or active, where
 *       applicable.</li>
 * </ul>
 *
 * <p>Player messages are sent using formatted colors to distinguish different pieces
 * of information. A clean record is acknowledged with a congratulatory message.
 */
public class PunishmentsCommand extends PalaceCommand {

    /**
     * Constructs a new {@code PunishmentsCommand} instance with the command name "punishments".
     * <p>
     * The PunishmentsCommand is designed to handle interactions related to
     * managing or displaying player punishments within the system.
     * This command acts as part of the overall infrastructure for handling
     * administrative or moderation tasks.
     * </p>
     *
     * <p>This class extends the {@code PalaceCommand} base class, inheriting its functionality
     * and structure, while specifying its own use-case through the "punishments" command name.</p>
     *
     * <h3>Purpose:</h3>
     * <ul>
     *   <li>Enables efficient handling of punishment-related tasks.</li>
     *   <li>Extends the functionality of the overall command framework.</li>
     * </ul>
     *
     * <p>When invoked, this command likely interacts with player data or administrative tools
     * to provide the necessary functionality for managing punishments. The specific behavior
     * and implementation details are defined or overridden as needed.</p>
     */
    public PunishmentsCommand() {
        super("punishments");
    }

    /**
     * Executes the punishment command for a player, providing detailed information
     * based on the specified argument about their punishment history, such as bans,
     * mutes, kicks, and warnings.
     *
     * <p>The `execute` method processes the following sections:
     * <ul>
     *     <li><b>summary</b>: Displays a summary of the player's punishment history.</li>
     *     <li><b>bans</b>: Provides details of the player's ban history.</li>
     *     <li><b>mutes</b>: Provides details of the player's mute history.</li>
     *     <li><b>kicks</b>: Provides details of the player's kick history.</li>
     *     <li><b>warns</b> or <b>warnings</b>: Provides details of the player's warning history.</li>
     * </ul>
     * </p>
     *
     * <p>If no valid argument is provided, displays the correct usage for the command.</p>
     *
     * @param player The player who executed the command.
     * @param args   The command arguments used to specify the operation:
     *               <ul>
     *                   <li><b>args[0]</b>: Defines the punishment category (e.g., "summary", "bans", "mutes", "kicks", "warns").</li>
     *               </ul>
     */
    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/punishments [Summary/Bans/Mutes/Kicks/Warns]");
            return;
        }
        String section = args[0].toLowerCase();
        UUID uuid = player.getUniqueId();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        switch (section) {
            case "summary": {
                int bans = PalaceBungee.getMongoHandler().getBans(uuid).size();
                int mutes = PalaceBungee.getMongoHandler().getMutes(uuid).size();
                int kicks = PalaceBungee.getMongoHandler().getKicks(uuid).size();
                int warns = PalaceBungee.getMongoHandler().getWarnings(uuid).size();
                player.sendMessage(ChatColor.GREEN + "Your Punishment History: " + ChatColor.YELLOW +
                        bans + " Bans, " + mutes + " Mutes, " + kicks + " Kicks, " + warns + " Warnings");
                if (bans == 0 && mutes == 0 && kicks == 0 && warns == 0) {
                    player.sendMessage(ChatColor.GREEN + "A clean record, nice work!");
                }
                break;
            }
            case "bans": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Ban History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getBans(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long created = doc.getLong("created");
                    long expires = doc.getLong("expires");
                    boolean permanent = doc.getBoolean("permanent");
                    boolean active = doc.getBoolean("active");
                    Calendar createdCal = Calendar.getInstance();
                    createdCal.setTimeInMillis(created);
                    Calendar expiresCal = Calendar.getInstance();
                    expiresCal.setTimeInMillis(expires);
                    if (permanent) {
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Started: " + ChatColor.GREEN + df.format(created) +
                                ChatColor.RED + " | Length: " + ChatColor.GREEN + "Permanent");
                    } else {
                        player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                                ChatColor.RED + " | Started: " + ChatColor.GREEN + df.format(created) +
                                ChatColor.RED + (active ? " | Expires: " : " | Expired: ") +
                                ChatColor.GREEN + df.format(expires) + ChatColor.RED + " | Length: " +
                                ChatColor.GREEN + DateUtil.formatDateDiff(createdCal, expiresCal) +
                                ChatColor.RED + " | Permanent: " + ChatColor.GREEN + "False");
                    }
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No bans, good job! :)");
                }
                break;
            }
            case "mutes": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Mute History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getMutes(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long created = doc.getLong("created");
                    long expires = doc.getLong("expires");
                    boolean active = doc.getBoolean("active");
                    Calendar createdCal = Calendar.getInstance();
                    createdCal.setTimeInMillis(created);
                    Calendar expiresCal = Calendar.getInstance();
                    expiresCal.setTimeInMillis(expires);
                    player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                            ChatColor.RED + " | Started: " + ChatColor.GREEN + df.format(created) +
                            ChatColor.RED + (active ? " | Expires: " : " | Expired: ") +
                            ChatColor.GREEN + df.format(expires) + ChatColor.RED + " | Length: " +
                            ChatColor.GREEN + DateUtil.formatDateDiff(createdCal, expiresCal));
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No mutes, great job! :)");
                }
                break;
            }
            case "kicks": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Kick History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getKicks(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long time = doc.getLong("time");
                    player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                            ChatColor.RED + " | Time: " + ChatColor.GREEN + df.format(time));
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No kicks, nice job! :)");
                }
                break;
            }
            case "warns":
            case "warnings": {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Warning History:");
                boolean empty = true;
                for (Object o : PalaceBungee.getMongoHandler().getWarnings(uuid)) {
                    empty = false;
                    Document doc = (Document) o;
                    String reason = doc.getString("reason");
                    long time = doc.getLong("time");
                    player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.GREEN + reason.trim() +
                            ChatColor.RED + " | Time: " + ChatColor.GREEN + df.format(time));
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "No warnings, great work! :)");
                }
                break;
            }
            default: {
                player.sendMessage(ChatColor.RED + "/punishments [Summary/Bans/Mutes/Kicks/Warns]");
                break;
            }
        }
    }
}