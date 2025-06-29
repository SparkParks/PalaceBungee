package network.palace.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import network.palace.bungee.commands.*;
import network.palace.bungee.commands.admin.*;
import network.palace.bungee.commands.chat.*;
import network.palace.bungee.commands.guide.GuideAnnounceCommand;
import network.palace.bungee.commands.guide.GuideHelpCommand;
import network.palace.bungee.commands.guide.GuideListCommand;
import network.palace.bungee.commands.guide.HelpMeCommand;
import network.palace.bungee.commands.moderation.*;
import network.palace.bungee.commands.staff.*;
import network.palace.bungee.handlers.Player;
import network.palace.bungee.listeners.PlayerChat;
import network.palace.bungee.listeners.PlayerJoinAndLeave;
import network.palace.bungee.listeners.ProxyPing;
import network.palace.bungee.listeners.ServerSwitch;
import network.palace.bungee.messages.MessageHandler;
import network.palace.bungee.mongo.MongoHandler;
import network.palace.bungee.utils.*;
import network.palace.bungee.utils.chat.JaroWinkler;
import network.palace.bungee.handlers.ShowReminder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Represents the core plugin class for the PalaceBungee system.
 * <p>
 * This class is responsible for managing the overall lifecycle of the plugin,
 * providing utilities for player management, command handling, event registration,
 * and additional server-related operations. It extends the {@code Plugin} base class
 * provided by the BungeeCord API.
 * </p>
 *
 * <h2>Primary Responsibilities</h2>
 * <ul>
 *   <li>Initialize the plugin lifecycle, including setup of utilities, configuration handling, and event listeners.</li>
 *   <li>Handle player login and logout processes as well as maintain the active player cache.</li>
 *   <li>Register commands and manage various administrative, moderation, and user commands.</li>
 *   <li>Provide utility services for chat management, player assistance, and server communication.</li>
 *   <li>Ensure resources are correctly initialized and cleaned up during enabling and disabling phases.</li>
 * </ul>
 *
 * <h2>Class Fields</h2>
 * <p>
 * This class contains multiple fields to manage resources, utilities, and key server components:
 * </p>
 * <ul>
 *   <li><b>proxyID</b> - Identifies the proxy instance for operations.</li>
 *   <li><b>instance</b> - A global instance reference for access across the plugin.</li>
 *   <li><b>configUtil</b> - Handles configuration-related operations.</li>
 *   <li><b>serverUtil</b> - Provides utilities for managing server-related actions.</li>
 *   <li><b>afkUtil</b> - Manages the functionality for detecting and handling AFK players.</li>
 *   <li><b>broadcastUtil</b> - Utility for sending broadcast messages across the proxy.</li>
 *   <li><b>chatAlgorithm</b> - Implements specific algorithms to moderate or manage chat functionality.</li>
 *   <li><b>chatUtil</b> - General utility class for handling chat features.</li>
 *   <li><b>forumUtil</b> - Provides features for forum integration.</li>
 *   <li><b>guideUtil</b> - Assists in guiding players with tutorials or automated help systems.</li>
 *   <li><b>moderationUtil</b> - Handles actions related to moderation and rule enforcement.</li>
 *   <li><b>partyUtil</b> - Manages functionality for player party or group systems.</li>
 *   <li><b>passwordUtil</b> - Utility for managing secure password operations.</li>
 *   <li><b>slackUtil</b> - Provides integration with Slack for posting or receiving notifications.</li>
 *   <li><b>mongoHandler</b> - Handles MongoDB interactions for data persistence.</li>
 *   <li><b>messageHandler</b> - Manages message queuing and delivery within the plugin environment.</li>
 *   <li><b>startTime</b> - Captures the timestamp of the plugin startup.</li>
 *   <li><b>players</b> - Represents the collection of online players connected to the proxy.</li>
 *   <li><b>usernameCache</b> - Cache for pairing player UUIDs with usernames for quick access.</li>
 *   <li><b>testNetwork</b> - Flag indicating whether the plugin is functioning in a testing environment.</li>
 * </ul>
 *
 * <h2>Key Methods</h2>
 * <p>
 * This class provides the following critical methods:
 * </p>
 * <ul>
 *   <li><b>{@link #onEnable()}</b> - Manages the initialization of the plugin and sets up services, listeners, and commands.</li>
 *   <li><b>{@link #onDisable()}</b> - Ensures graceful shutdown of resources and cleanup operations.</li>
 *   <li><b>{@link #registerListeners()}</b> - Registers event listeners for proxy events, including player interactions and server pings.</li>
 *   <li><b>{@link #registerCommands()}</b> - Registers commands for specific functionalities grouped into categories like admin, moderation, and chat commands.</li>
 *   <li><b>{@link #getProxyServer()}</b> - Retrieves the proxy server instance.</li>
 *   <li><b>{@link #getPlayer(UUID)}</b> - Fetches a player object using their UUID.</li>
 *   <li><b>{@link #getPlayer(String)}</b> - Fetches a player object using their username.</li>
 *   <li><b>{@link #login(Player)}</b> - Handles player login, including updating cache and initializing player-specific data.</li>
 *   <li><b>{@link #logout(UUID, Player)}</b> - Manages player logout and clears relevant data from the cache.</li>
 *   <li><b>{@link #getOnlinePlayers()}</b> - Retrieves a collection of players currently connected to the proxy.</li>
 *   <li><b>{@link #getUsername(UUID)}</b> - Returns the username corresponding to a given UUID.</li>
 *   <li><b>{@link #getUUID(String)}</b> - Fetches the UUID for a specified username.</li>
 *   <li><b>{@link #setupShowReminder()}</b> - Configures and initializes systems responsible for sending periodic player reminders.</li>
 * </ul>
 *
 * <h2>Super Class</h2>
 * <p>
 * Extends {@link Plugin}, allowing the plugin to integrate with the BungeeCord proxy framework seamlessly.
 * </p>
 */
public class PalaceBungee extends Plugin {
    /**
     * <p>Represents a unique identifier for this proxy server instance.</p>
     *
     * <p>This identifier is generated as a {@link UUID} when the application starts
     * and remains constant during the runtime of the application. It is used to
     * uniquely distinguish this proxy server instance from others.</p>
     *
     * <ul>
     *   <li>The value is statically initialized when the class is loaded.</li>
     *   <li>The field is marked as <code>final</code>, ensuring its immutability.</li>
     *   <li>It is accessed via a getter method provided by the <code>@Getter</code>
     *   annotation.</li>
     * </ul>
     */
    @Getter private static final UUID proxyID = UUID.randomUUID();

    /**
     * <p>Static instance of the {@link PalaceBungee} class.</p>
     *
     * <p>This instance provides a global point of access to the functionality
     * and utilities offered by the {@code PalaceBungee} plugin.</p>
     *
     * <p><strong>Usage Notes:</strong></p>
     * <ul>
     *   <li>Designed to follow the singleton design pattern within the context
     *       of the plugin's lifecycle.</li>
     *   <li>Provides central point of interaction with various plugin utilities
     *       and features.</li>
     *   <li>Access and use cautiously to avoid unexpected modifications
     *       or race conditions in plugin behavior.</li>
     * </ul>
     */
    @Getter private static PalaceBungee instance;

    /**
     * <p>The {@code configUtil} is a static instance of the {@link ConfigUtil} class,
     * providing centralized access to configuration management functionalities within the application.</p>
     *
     * <p>This utility is responsible for handling various configuration operations, such as:</p>
     * <ul>
     *     <li>Loading and reloading configurations from the {@code config.yml} file or the database.</li>
     *     <li>Managing and retrieving server-related settings, including MOTD (Message of the Day) and maintenance mode.</li>
     *     <li>Providing chat settings, including chat delay, strict mode configurations, and muted chats.</li>
     *     <li>Facilitating integration with external systems via database connection details (e.g., RabbitMQ, MongoDB, SQL).</li>
     *     <li>Handling announcements and adjustments to the server's behavior dynamically.</li>
     * </ul>
     *
     * <p>As a singleton, {@code configUtil} ensures that configuration state is consistent and shared across the application.</p>
     */
    @Getter private static ConfigUtil configUtil;

    /**
     * A static reference to the {@link ServerUtil} instance used within the application.
     * <p>
     * <b>Purpose:</b> The {@code serverUtil} variable provides centralized access to various server
     * management functionalities, including loading servers, determining player presence, managing
     * server connections, and handling server-specific operations.
     * </p>
     * <p>
     * <b>Key Responsibilities:</b>
     * <ul>
     *   <li>Provides utility functions for managing server instances and their attributes.</li>
     *   <li>Tracks current hub servers, online player counts, and player names.</li>
     *   <li>Facilitates server switching and distribution logic based on server type or load balancing.</li>
     *   <li>Interfaces with external systems (e.g., MongoDB) for retrieving and updating server/player data.</li>
     * </ul>
     * </p>
     * <p>
     * <b>Access:</b> The variable is marked as {@code static} and {@code private}, exposing read-access
     * via a {@code @Getter} annotation. This ensures that the single instance of {@code ServerUtil}
     * can be accessed reliably across the system while protecting against unintended modifications.
     * </p>
     */
    @Getter private static ServerUtil serverUtil;

    /**
     * Provides functionality related to managing and handling AFK (Away From Keyboard) status of players in the server.
     *
     * <p>This utility is responsible for monitoring the activity status of players, warning them after a designated
     * period of inactivity, and disconnecting them if they remain inactive for too long. It ensures that server resources
     * are efficiently managed by discouraging prolonged inactivity.
     *
     * <p><strong>Key Features:</strong>
     * <ul>
     *   <li>Periodically checks all connected players' activity status.</li>
     *   <li>Warns players with configurable messages and visual alerts if they are detected to be inactive.</li>
     *   <li>Kicks players from the server if they remain inactive beyond the allowed duration.</li>
     *   <li>Logs details of disconnected players for further analysis or moderation purposes.</li>
     * </ul>
     *
     * <p>This utility integrates with other server components, such as player rank management and logging systems,
     * to provide seamless AFK handling aligned with server policies. It also ensures critical measures such as
     * warnings and kicks are appropriately displayed and recorded.
     */
    @Getter private static AFKUtil afkUtil;

    /**
     * <p>Represents the utility responsible for broadcasting messages to players on the proxy network.</p>
     *
     * <p>This utility schedules periodic announcements to all online players, ensuring important
     * messages or updates are delivered effectively within the network.</p>
     *
     * <p>Key functionalities:</p>
     * <ul>
     *   <li>Retrieves announcements from the configuration.</li>
     *   <li>Formats and displays the announcements in a predefined, user-friendly format.</li>
     *   <li>Schedules automatic broadcasting of messages at a fixed interval.</li>
     * </ul>
     *
     * <p>This instance is statically accessible and initialized during the lifecycle of the
     * {@code PalaceBungee} plugin.</p>
     */
    @Getter private static BroadcastUtil broadcastUtil;

    /**
     * <p>The {@code chatAlgorithm} variable is an instance of the {@link JaroWinkler} class,
     * used within the system to calculate the similarity or distance between strings,
     * leveraging the Jaro-Winkler algorithm. This algorithm is particularly useful for comparing
     * strings with potential typographical errors, common in chat or textual input scenarios.</p>
     *
     * <p>Key functionalities of the Jaro-Winkler algorithm include:</p>
     * <ul>
     *   <li>Computing similarity scores between strings based on matching characters and proximity.</li>
     *   <li>Adjusting similarity scores with a prefix scale factor to enhance results for strings
     *       with common prefixes.</li>
     *   <li>Calculating distances as an inverse measure of similarity.</li>
     * </ul>
     *
     * <p>This variable is declared as {@code static} and {@code final}, ensuring it is a
     * single, immutable instance shared globally within the class.</p>
     */
    @Getter private static final JaroWinkler chatAlgorithm = new JaroWinkler();

    /**
     * Represents a singleton instance of the {@code ChatUtil} utility class.
     * <p>
     * This utility is used to handle chat-related functionalities within the application.
     * It provides methods and capabilities to process, format, or manage chat features as required.
     * </p>
     * <p>
     * Being a static field, this instance is accessible application-wide,
     * ensuring consistent behavior and data when interacting with chat operations.
     * </p>
     */
    @Getter private static ChatUtil chatUtil;

    /**
     * <p>
     * A static instance of {@link ForumUtil} used for managing interactions and utilities
     * related to forums within the application. This field provides access to methods
     * and features supporting forum functionalities.
     * </p>
     *
     * <p>
     * The {@code forumUtil} instance may include operations for managing forum posts,
     * user interactions, and other related utilities that streamline forum-based workflows
     * within the system.
     * </p>
     *
     * <p>
     * This instance is globally accessible and is initialized during the setup phase
     * of the application to ensure consistent and centralized forum management.
     * </p>
     */
    @Getter private static ForumUtil forumUtil;

    /**
     * A static instance of the {@code GuideUtil} class within the {@code PalaceBungee} system.
     *
     * <p>This instance provides functionalities and utilities related to guiding services
     * within the proxy server application. The specific operations and scope of
     * {@code GuideUtil} depend on its implementation, which is utilized by the
     * {@code PalaceBungee} class.
     *
     * <p>Key characteristics:
     * <ul>
     *   <li>Acts as a centralized utility for managing guidance-related tasks within the system.</li>
     *   <li>Accessible as a singleton instance directly through the {@code PalaceBungee} class.</li>
     *   <li>Ensures efficient management and invocation of guiding features.</li>
     * </ul>
     */
    @Getter private static GuideUtil guideUtil;

    /**
     * A static instance of the {@code ModerationUtil} class used for handling
     * moderation tasks and utilities across the application.
     *
     * <p>It provides a centralized access point for moderation-related functionalities,
     * which might include managing user behavior, monitoring activities, or applying
     * specific rules and restrictions.
     *
     * <p>Typically accessed to ensure proper control and consistency in moderation
     * throughout the system.
     */
    @Getter private static ModerationUtil moderationUtil;

    /**
     * <p>The {@code partyUtil} variable serves as a central utility to manage party-related
     * functionalities within the application. It provides features and methods to facilitate
     * the creation, management, and interaction of player parties in the system.</p>
     *
     * <p>This is a globally accessible, singleton instance designed to ensure consistent and
     * thread-safe party operations across the application.</p>
     *
     * <ul>
     *   <li>Handles party creation and disbanding mechanisms.</li>
     *   <li>Manages party invitations, accepts, and declines.</li>
     *   <li>Coordinates communication and actions within parties.</li>
     *   <li>Facilitates player grouping for events, games, or shared interactions.</li>
     * </ul>
     *
     * <p>The {@code partyUtil} is initialized and managed within the context of the enclosing
     * class, and it is expected to be properly configured before usage.</p>
     */
    @Getter private static PartyUtil partyUtil;

    /**
     * Static instance of the {@code PasswordUtil} class used for password-related utilities in the application.
     * <p>
     * The {@code PasswordUtil} provides methods for:
     * <ul>
     *   <li>Hashing and verifying passwords using secure algorithms.</li>
     *   <li>Generating salts for password hashing.</li>
     *   <li>Validating the strength of passwords.</li>
     * </ul>
     * <p>
     * As a singleton, this instance can be used globally within the application
     * for consistent password operations, ensuring security best practices.
     */
    @Getter private static PasswordUtil passwordUtil;

    /**
     * A static reference to an instance of the {@link SlackUtil} class.
     * <p>
     * This variable provides access to functionalities for interacting with Slack,
     * such as sending messages and attachments to predefined Slack webhooks.
     * </p>
     * <p>
     * The {@link SlackUtil} instance manages Slack operations, including:
     * <ul>
     *     <li>Constructing and sending messages with optional attachments.</li>
     *     <li>Formatting messages using markdown features for Slack compatibility.</li>
     *     <li>Handling different Slack webhooks based on configuration or status.</li>
     * </ul>
     * </p>
     * <p>
     * Typically used internally within the application for communication with Slack services.
     * </p>
     */
    @Getter private static SlackUtil slackUtil;

    /**
     * <p>
     * A static instance of {@link MongoHandler}, utilized for handling interactions
     * with a MongoDB database. This provides methods and utilities to perform database
     * operations required by the application.
     * </p>
     *
     * <p>
     * The {@code mongoHandler} is a critical component of the {@code PalaceBungee}
     * class, enabling persistent data storage and retrieval functionality. It ensures
     * seamless communication between the application and the MongoDB database.
     * </p>
     *
     * <h3>Key characteristics:</h3>
     * <ul>
     *   <li>Acts as the main database access point for the class.</li>
     *   <li>Shared and accessible globally within the scope of the application through this static reference.</li>
     *   <li>Essential for data persistence and application functionality dependent on database interactions.</li>
     * </ul>
     *
     * <p>
     * The lifecycle and initialization of {@code mongoHandler} are managed
     * within the application setup. Ensure proper configuration before application
     * operations depend on database connectivity.
     * </p>
     */
    @Getter private static MongoHandler mongoHandler;

    /**
     * Represents a static instance of the {@code MessageHandler}, which is responsible for
     * handling message-related functionalities within the application.
     *
     * <p>This variable provides global access to the {@code MessageHandler}
     * instance, allowing other components in the application to leverage its methods
     * and utilities for message processing and communication tasks.</p>
     *
     * <p>Typical responsibilities of the {@code MessageHandler} might include:</p>
     * <ul>
     *   <li>Processing incoming and outgoing messages.</li>
     *   <li>Managing message formatting and delivery.</li>
     *   <li>Integrating with external communication systems if applicable.</li>
     * </ul>
     *
     * <p>As this is a static field, it should be initialized properly to ensure
     * the availability of its functionalities throughout the application's lifecycle.</p>
     */
    @Getter private static MessageHandler messageHandler;

    /**
     * <p>The timestamp representing the start time of the application or process, in milliseconds,
     * since the Unix epoch (January 1, 1970, 00:00:00 GMT).</p>
     *
     * <p>This value is initialized at the time the class is loaded and remains constant throughout
     * the application's lifecycle. It can be used for benchmarking, logging, or measuring the duration
     * of operations relative to the application's start time.</p>
     *
     * <ul>
     * <li>Data Type: <code>long</code></li>
     * <li>Access Modifier: <code>private static final</code></li>
     * <li>Value: Set at runtime using <code>System.currentTimeMillis()</code></li>
     * </ul>
     */
    @Getter private static final long startTime = System.currentTimeMillis();

    /**
     * <p>A static and thread-safe {@link HashMap} that stores information about currently known players.</p>
     *
     * <p>The {@code players} map uses a {@link UUID} as the key to uniquely identify each player,
     * and a {@link Player} instance as the corresponding value that holds the player's session data.</p>
     *
     * <p>This map serves as a central storage for managing player-related data and ensures
     * quick access by {@link UUID} to associated {@link Player} objects.</p>
     *
     * <p>Common operations involving this map include:</p>
     * <ul>
     *   <li>Adding a player to the map during player login.</li>
     *   <li>Removing a player from the map during logout.</li>
     *   <li>Fetching a player's information using their {@link UUID}.</li>
     * </ul>
     *
     * <p>Modification to the {@code players} map is restricted to specific methods within the containing class, ensuring consistent state management and avoiding external access
     *  or misuse.</p>
     *
     * <p>This field is marked {@code final} to prevent reassignment and remains static to ensure global accessibility within the application lifecycle.</p>
     */
    private final static HashMap<UUID, Player> players = new HashMap<>();

    /**
     * A static cache that maps unique player identifiers (UUIDs) to their corresponding usernames.
     * <p>
     * This HashMap is used to store and retrieve player username information based on their UUID,
     * allowing for efficient lookup and reducing the need for repeated database or remote calls.
     * </p>
     *
     * <p><b>Key Characteristics:</b>
     * <lu>
     *   <li>Keys are {@link UUID} objects representing unique player identifiers.</li>
     *   <li>Values are {@link String} objects representing player usernames.</li>
     *   <li>The map is declared as {@code final static} meaning it is constant and tied to the class.</li>
     *   <li>Thread-safety is not guaranteed. If accessed concurrently, proper synchronization is required.</li>
     * </lu>
     * </p>
     *
     * <p><b>Usage Context:</b></p>
     * <p>
     * This cache is used internally by the <i>PalaceBungee</i> plugin to enhance performance when dealing with player data.
     * It helps minimize latency during username/UUID lookups by storing this data in memory for quick access.
     * </p>
     */
    @Getter private final static HashMap<UUID, String> usernameCache = new HashMap<>();

    /**
     * A static boolean flag indicating whether the application is running in a test network environment.
     * <p>
     * This variable is primarily used to differentiate between a development/test environment
     * and the production network environment. Various features or configurations can be
     * adapted based on its value.
     * </p>
     * <ul>
     *     <li>When <code>true</code>, the application operates under test network configurations.</li>
     *     <li>When <code>false</code>, the application operates under production network configurations.</li>
     * </ul>
     */
    @Getter private static boolean testNetwork;

    /**
     * This method is executed when the plugin is enabled. It performs the following actions:
     *
     * <p>Main tasks include:</p>
     * <ul>
     *   <li>Assigns the plugin instance to a static field for global access.</li>
     *   <li>Initializes the configuration utility (<code>ConfigUtil</code>).</li>
     *   <li>Checks whether the network environment is set to a testing mode:
     *       <ul>
     *           <li>If the configuration cannot be loaded, defaults to test network status enabled
     *               and logs a warning.</li>
     *           <li>If the test network is enabled, logs a warning message.</li>
     *       </ul>
     *   </li>
     *   <li>Initializes the MongoDB handler (<code>MongoHandler</code>) and reloads configurations.</li>
     *   <li>Initializes the forum utility (<code>ForumUtil</code>) for forum-related integration.</li>
     *   <li>Initializes the server utility (<code>ServerUtil</code>).</li>
     *   <li>Sets up the RabbitMQ message handling system:
     *       <ul>
     *           <li>Creates an instance of <code>MessageHandler</code>.</li>
     *           <li>Initializes its connections and resources.</li>
     *       </ul>
     *   </li>
     *   <li>Initializes various utility classes responsible for different subsystems:
     *       <ul>
     *           <li><code>AFKUtil</code>: Manages AFK (away-from-keyboard) functionality.</li>
     *           <li><code>BroadcastUtil</code>: Handles broadcast-related tasks.</li>
     *           <li><code>ChatUtil</code>: Manages chat-related utilities.</li>
     *           <li><code>GuideUtil</code>: Provides tools related to game guides.</li>
     *           <li><code>ModerationUtil</code>: Facilitates moderation tasks.</li>
     *           <li><code>PartyUtil</code>: Supports party-related mechanics.</li>
     *           <li><code>PasswordUtil</code>: Manages password-related functionality.</li>
     *           <li><code>SlackUtil</code>: Integrates notifications with Slack.</li>
     *       </ul>
     *   </li>
     *   <li>Sets up show reminders via <code>setupShowReminder</code> method.</li>
     *   <li>Registers event listeners and commands as part of plugin initialization.</li>
     * </ul>
     *
     * <p>During the initialization, any errors encountered during the setup of
     * specific components (e.g., configuration loading, external handlers, or utilities)
     * are logged, and stack traces are printed to assist with debugging.</p>
     */
    @Override
    public void onEnable() {
        // recognize the instance as this plugin
        instance = this;

        // initialize the config util
        configUtil = new ConfigUtil();
        // try to check if the network is on a testing network status
        try {
            testNetwork = configUtil.isTestNetwork();
        } catch (IOException e) { // catch the exception and set the testNetwork variable to true, print warning
            testNetwork = true;
            getLogger().log(Level.WARNING, "Error loading testNetwork setting from config file, defaulting to testNetwork ENABLED", e);
        }
        // if test network is true, send a warning notifying the test network is enabled
        if (testNetwork) getLogger().log(Level.WARNING, "Test network enabled!");

        // try to initialze the mongo db handler
        try {
            mongoHandler = new MongoHandler();
            PalaceBungee.getConfigUtil().reload();
        } catch (IOException e) { // catch the error if it does not work, print the stack trace
            e.printStackTrace();
        }

        // try to initialize the forum util
        try {
            forumUtil = new ForumUtil();
        } catch (Exception e) { // catch the error if it does not initialize, print the stack trace
            e.printStackTrace();
        }

        // initialize the server utility
        serverUtil = new ServerUtil();

        // try to initialize the rabbit mq message handler
        try {
            messageHandler = new MessageHandler();
            messageHandler.initialize();
        } catch (IOException | TimeoutException e) { // catch the error if it does not initialize, print the stack trace
            e.printStackTrace();
        }

        // initialize the other utility files
        afkUtil = new AFKUtil();
        broadcastUtil = new BroadcastUtil();
        chatUtil = new ChatUtil();
        guideUtil = new GuideUtil();
        moderationUtil = new ModerationUtil();
        partyUtil = new PartyUtil();
        passwordUtil = new PasswordUtil();
        slackUtil = new SlackUtil();

        // set up the show reminders
        setupShowReminder();

        // register listeners and commands
        registerListeners();
        registerCommands();
    }

    /**
     * This method is called when the plugin is disabled.
     *
     * <p>It is used to perform cleanup tasks and release resources. Specifically,
     * it ensures that the {@code messageHandler} is properly shut down if it is
     * not {@code null}, preventing potential resource leaks or issues with
     * lingering connections.</p>
     *
     * <p>The {@code shutdown} method of {@code messageHandler} handles the
     * termination of various connections and resources such as proxy connections,
     * chat analysis tools, and message queue channels. Any exceptions encountered
     * during the shutdown process are captured and logged.</p>
     *
     * <ul>
     *   <li>If {@code ALL_PROXIES}, {@code CHAT_ANALYSIS}, {@code PROXY_DIRECT},
     *       or {@code MC_DIRECT} are open, they are closed safely.</li>
     *   <li>All channels in the {@code channels} map are closed, ensuring proper
     *       connection termination.</li>
     * </ul>
     */
    @Override
    public void onDisable() {
        if (messageHandler != null) messageHandler.shutdown();
    }

    /**
     * Registers event listeners for various game and server events to the plugin manager.
     *
     * <p>This method links the main plugin instance with specific event handler classes
     * to manage server-side events effectively. Each registered listener responds to
     * specific server events such as player chat, player joining or leaving, server ping
     * requests, and server switching actions.</p>
     *
     * <p><b>Listeners Registered:</b></p>
     * <ul>
     *   <li><code>PlayerChat</code>: Handles player chat events, including command
     *       execution and chat moderation.</li>
     *   <li><code>PlayerJoinAndLeave</code>: Manages actions performed when players join
     *       or leave the server.</li>
     *   <li><code>ProxyPing</code>: Handles server ping events, typically used to
     *       provide server and player information to server browsers.</li>
     *   <li><code>ServerSwitch</code>: Monitors and manages actions related to players
     *       switching between servers in the network.</li>
     * </ul>
     *
     * <p>This method is typically invoked during the plugin's initialization phase to
     * ensure that event handling is appropriately configured before the plugin starts
     * operating.</p>
     */
    private void registerListeners() {
        PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(this, new PlayerChat());
        pm.registerListener(this, new PlayerJoinAndLeave());
        pm.registerListener(this, new ProxyPing());
        pm.registerListener(this, new ServerSwitch());
    }

    /**
     * Registers all the commands for the plugin, organizing them into various categories such as
     * admin commands, chat commands, guide commands, moderation commands, staff commands, and
     * general commands. Each command is associated with specific functionality and permissions.
     *
     * <p>This method utilizes the {@link PluginManager} to register all required commands,
     * enabling them to be executed in the system.</p>
     *
     * <p><strong>Command Categories:</strong></p>
     *
     * <ul>
     *   <li><strong>Admin Commands:</strong> Commands intended for administrative tasks,
     *       including managing logs, toggling maintenance modes, and reloading proxy settings.</li>
     *   <li><strong>Chat Commands:</strong> Commands to control various chat functionalities such as
     *       toggling chat, chat monitoring, or sending specific types of messages.</li>
     *   <li><strong>Guide Commands:</strong> Commands to assist users with help-related requests,
     *       guide announcements, and related administrative tasks.</li>
     *   <li><strong>Moderation Commands:</strong> Commands for moderation tasks like banning users,
     *       muting players, checking user details, and issuing warnings.</li>
     *   <li><strong>Staff Commands:</strong> Commands to aid staff members in managing network-related
     *       functions like broadcasting messages, capturing motion, and managing staff lists.</li>
     *   <li><strong>General Commands:</strong> Common commands available for enhancing user experience,
     *       such as managing friendships, accessing social channels, and joining specific functionalities.</li>
     * </ul>
     *
     * <p>This method ensures that all commands are accessible where needed and are correctly registered within
     * the plugin's framework.</p>
     */
    private void registerCommands() {
        PluginManager pm = getProxy().getPluginManager();
        /* Admin Commands */
        pm.registerCommand(this, new GuideLogCommand());
        pm.registerCommand(this, new MaintenanceCommand());
        pm.registerCommand(this, new MsgToggleCommand());
        pm.registerCommand(this, new ProxyCountsCommand());
        pm.registerCommand(this, new ProxyReloadCommand());
        pm.registerCommand(this, new ProxyVersionCommand());
        pm.registerCommand(this, new SendCommand());
        pm.registerCommand(this, new UpdateHashesCommand());
        /* Chat Commands */
        pm.registerCommand(this, new AdminChatCommand());
        pm.registerCommand(this, new ChatCommand());
        pm.registerCommand(this, new ChatDelayCommand());
        pm.registerCommand(this, new ChatStatusCommand());
        pm.registerCommand(this, new ClearChatCommand());
        pm.registerCommand(this, new GuideChatCommand());
        pm.registerCommand(this, new PartyChatCommand());
        pm.registerCommand(this, new StaffChatCommand());
        /* Guide Commands */
        pm.registerCommand(this, new GuideAnnounceCommand());
        pm.registerCommand(this, new GuideHelpCommand());
        pm.registerCommand(this, new GuideListCommand());
        pm.registerCommand(this, new HelpMeCommand());
        /* Moderation Commands */
        pm.registerCommand(this, new AltAccountsCommand());
        pm.registerCommand(this, new BanCommand());
        pm.registerCommand(this, new BanIPCommand());
        pm.registerCommand(this, new BannedProvidersCommand());
        pm.registerCommand(this, new BanProviderCommand());
        pm.registerCommand(this, new DMToggleCommand());
        pm.registerCommand(this, new FindCommand());
        pm.registerCommand(this, new IPCommand());
        pm.registerCommand(this, new KickCommand());
        pm.registerCommand(this, new LookupCommand());
        /* Remove functionality after July 1st */
        pm.registerCommand(this, new BseenDeprecatedCommand());
        pm.registerCommand(this, new ModlogCommand());
        pm.registerCommand(this, new MuteChatCommand());
        pm.registerCommand(this, new MuteCommand());
        pm.registerCommand(this, new NamecheckCommand());
        pm.registerCommand(this, new PartiesCommand());
        pm.registerCommand(this, new StrictCommand());
        pm.registerCommand(this, new TempBanCommand());
        pm.registerCommand(this, new UnbanCommand());
        pm.registerCommand(this, new UnbanIPCommand());
        pm.registerCommand(this, new UnbanProviderCommand());
        pm.registerCommand(this, new UnmuteCommand());
        pm.registerCommand(this, new WarnCommand());
        /* Staff Commands */
        pm.registerCommand(this, new BroadcastClockCommand());
        pm.registerCommand(this, new BroadcastCommand());
        pm.registerCommand(this, new CharListCommand());
        pm.registerCommand(this, new DirectChatCommand());
        pm.registerCommand(this, new MotionCaptureCommand());
        pm.registerCommand(this, new ServerCommand());
        pm.registerCommand(this, new SGListCommand());
        pm.registerCommand(this, new StaffCommand());
        pm.registerCommand(this, new StaffListCommand());
        pm.registerCommand(this, new CharApproval());
        /* General Commands */
        pm.registerCommand(this, new ApplyCommand());
        pm.registerCommand(this, new AudioCommand());
        pm.registerCommand(this, new BugCommand());
        pm.registerCommand(this, new DiscordCommand());
        pm.registerCommand(this, new IgnoreCommand());
        pm.registerCommand(this, new FriendCommand());
        pm.registerCommand(this, new JoinCommand());
        pm.registerCommand(this, new LinkCommand());
        pm.registerCommand(this, new MentionsCommand());
        pm.registerCommand(this, new MsgCommand());
        pm.registerCommand(this, new OnlineCountCommand());
        pm.registerCommand(this, new PartyCommand());
        pm.registerCommand(this, new PunishmentsCommand());
        pm.registerCommand(this, new ReplyCommand());
        pm.registerCommand(this, new RulesCommand());
        pm.registerCommand(this, new SocialCommand());
        pm.registerCommand(this, new StoreCommand());
        pm.registerCommand(this, new UptimeCommand());
        pm.registerCommand(this, new VirtualQueueJoinCommand());
        pm.registerCommand(this, new WhereAmICommand());
    }

    /**
     * Retrieves the proxy server instance associated with this application.
     *
     * <p>This method provides access to the underlying {@code ProxyServer} object,
     * which is responsible for handling player connections, scheduling tasks,
     * executing commands, and managing the server's overall behavior.
     *
     * <p>The returned {@code ProxyServer} instance can be utilized to perform various
     * operations such as obtaining player information, scheduling asynchronous tasks,
     * or logging server activities.
     *
     * @return the {@code ProxyServer} instance currently in use.
     */
    public static ProxyServer getProxyServer() {
        return instance.getProxy();
    }

    /**
     * Retrieves a {@link Player} object associated with the given unique identifier (UUID).
     *
     * <p>
     * This method fetches a player from the internal collection using their unique identifier.
     * It is primarily used to obtain a player's data for further processing or interaction within the system.
     * </p>
     *
     * @param uuid the unique identifier of the player to retrieve. Must not be null.
     * @return the {@link Player} object corresponding to the specified UUID, or <code>null</code> if no player is found with the provided UUID.
     */
    public static Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    /**
     * Retrieves a {@link Player} object based on the provided username.
     *
     * <p>This method searches through the currently available players to find
     * and return the player whose username matches the specified name.
     * The comparison is case-insensitive.
     *
     * @param name the username of the player to be retrieved.
     *             It is case-insensitive.
     * @return the {@link Player} object that matches the provided username,
     *         or {@code null} if no such player is found.
     */
    public static Player getPlayer(String name) {
        Player p = null;
        for (Player tp : players.values()) {
            if (tp.getUsername().equalsIgnoreCase(name)) {
                p = tp;
                break;
            }
        }
        return p;
    }

    /**
     * Handles the login process for a player. This includes adding the player
     * to the active players map, notifying the database of the login, and starting
     * the tutorial if the player is a new guest.
     *
     * <p>Steps performed during the login process:
     * <ul>
     *     <li>Add the player to the local player storage.</li>
     *     <li>Perform database actions to record the login using the database handler.</li>
     *     <li>If the player is a new guest, initiate the tutorial sequence.</li>
     * </ul>
     *
     * @param player The player who is attempting to log in. This object contains
     *               all relevant data needed for the login process.
     */
    public static void login(Player player) {
        players.put(player.getUniqueId(), player);
        mongoHandler.login(player);
        if (player.isNewGuest()) {
            player.runTutorial();
        }
    }

    /**
     * Logs out a player from the server.
     * <p>
     * This method handles the logout process for a player. It performs the following:
     * <ul>
     *     <li>Checks if the player is a new guest and cancels their tutorial if necessary.</li>
     *     <li>Removes the player from the list of active players.</li>
     *     <li>Performs any necessary database operations to handle the player's logout.</li>
     * </ul>
     *
     * @param uuid The unique identifier of the player being logged out.
     * @param player The {@code Player} object representing the player being logged out. This can be {@code null}.
     */
    public static void logout(UUID uuid, Player player) {
        if (player != null && player.isNewGuest()) player.cancelTutorial();
        players.remove(uuid);
        mongoHandler.logout(uuid, player);
    }

    /**
     * Retrieves a collection of all currently online players.
     *
     * <p>This method provides access to the list of players currently connected to the server.
     * The returned collection is a snapshot and does not reflect real-time changes to the player list.
     *
     * @return A {@code Collection<Player>} containing all players currently online.
     */
    public static Collection<Player> getOnlinePlayers() {
        return new ArrayList<>(players.values());
    }

    /**
     * Retrieves the username associated with the provided UUID.
     * <p>
     * The method first checks a local cache for the username. If the username is not found
     * in the cache, it queries the database and retrieves the username. If no username is
     * associated with the UUID in the database, the method returns "Unknown".
     * <p>
     * Once retrieved from the database, the username is added to the cache to optimize
     * future lookups for the same UUID.
     *
     * @param uuid the unique identifier (UUID) of the player whose username is to be retrieved
     * @return the username associated with the provided UUID, or "Unknown" if not found
     */
    public static String getUsername(UUID uuid) {
        String name = usernameCache.get(uuid);
        if (name == null) {
            name = mongoHandler.uuidToUsername(uuid);
            if (name == null) {
                name = "Unknown";
            } else {
                PalaceBungee.getUsernameCache().put(uuid, name);
            }
        }
        return name;
    }

    /**
     * Retrieves the UUID of a player based on their username.
     * <p>
     * This method checks if a player is currently online on the proxy server. If the player is online, their UUID is retrieved directly
     * from the proxy server. If the player is offline, their UUID is fetched from a persistent data store.
     * </p>
     *
     * @param username The username of the player whose UUID is to be retrieved.
     *                 This should be a non-null and non-empty string.
     * <ul>
     * <li>If the player is currently online, their UUID is fetched from proxy server data.</li>
     * <li>If the player is offline, an attempt is made to fetch their UUID from the database.</li>
     * </ul>
     *
     * @return The UUID of the player associated with the specified username, or {@code null} if the UUID cannot be found.
     */
    public static UUID getUUID(String username) {
        ProxiedPlayer p = getProxyServer().getPlayer(username);
        if (p != null) return p.getUniqueId();
        return mongoHandler.usernameToUUID(username);
    }


    /**
     * Configures and schedules reminders for various show times throughout the day.
     * <p>
     * This method sets up reminders for shows occurring at 10:00 AM, 2:00 PM, 6:00 PM, and 10:00 PM in the
     * "America/New_York" timezone. It calculates the delays required for the reminders based on the current
     * time and schedules them to repeat daily using a {@code ScheduledExecutorService}.
     * </p>
     * <p>
     * The reminders notify staff 40, 30, and 20 minutes before each scheduled show time.
     * </p>
     *
     * <p><b>Details of the implementation:</b></p>
     * <ul>
     *   <li>Uses {@code LocalDateTime} and {@code ZonedDateTime} to work with time in the specified timezone.</li>
     *   <li>Schedules reminders using the {@code scheduleAtFixedRate()} method with predefined intervals.</li>
     *   <li>Each alert is represented by a {@link ShowReminder} object, which sends a notification message to staff.</li>
     *   <li>The scheduled tasks are configured to repeat every 24 hours.</li>
     *   <li>
     *     Reminder messages include specific times and countdowns such as
     *     "Please get ready to run the 10am Show in 40 minutes!".
     *   </li>
     * </ul>
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>If the current time is already past a specific reminder's scheduled time, the reminder is shifted to the
     *       next day (using {@code plusDays(1)}).</li>
     *   <li>Delays between the current time and each reminder are calculated using {@link Duration}.</li>
     * </ul>
     *
     * <p><b>Concurrency:</b></p>
     * <ul>
     *   <li>A single-threaded {@code ScheduledExecutorService} instance is used for scheduling tasks.</li>
     *   <li>All reminders are executed on the same thread as part of the executor's scheduling.</li>
     * </ul>
     */
    public void setupShowReminder() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.of("America/New_York");
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNext10 = zonedNow.withHour(9).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext10_2 = zonedNow.withHour(9).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext10_3 = zonedNow.withHour(9).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext14 = zonedNow.withHour(13).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext14_2 = zonedNow.withHour(13).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext14_3 = zonedNow.withHour(13).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext18 = zonedNow.withHour(17).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext18_2 = zonedNow.withHour(17).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext18_3 = zonedNow.withHour(17).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext22 = zonedNow.withHour(21).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext22_2 = zonedNow.withHour(21).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext22_3 = zonedNow.withHour(21).withMinute(40).withSecond(0);

        if (zonedNow.compareTo(zonedNext10) > 0) zonedNext10 = zonedNext10.plusDays(1);
        if (zonedNow.compareTo(zonedNext10_2) > 0) zonedNext10_2 = zonedNext10_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext10_3) > 0) zonedNext10_3 = zonedNext10_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext14) > 0) zonedNext14 = zonedNext14.plusDays(1);
        if (zonedNow.compareTo(zonedNext14_2) > 0) zonedNext14_2 = zonedNext14_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext14_3) > 0) zonedNext14_3 = zonedNext14_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext18) > 0) zonedNext18 = zonedNext18.plusDays(1);
        if (zonedNow.compareTo(zonedNext18_2) > 0) zonedNext18_2 = zonedNext18_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext18_3) > 0) zonedNext18_3 = zonedNext18_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext22) > 0) zonedNext22 = zonedNext22.plusDays(1);
        if (zonedNow.compareTo(zonedNext22_2) > 0) zonedNext22_2 = zonedNext22_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext22_3) > 0) zonedNext22_3 = zonedNext22_3.plusDays(1);

        long d1 = Duration.between(zonedNow, zonedNext10).getSeconds();
        long d2 = Duration.between(zonedNow, zonedNext10_2).getSeconds();
        long d3 = Duration.between(zonedNow, zonedNext10_3).getSeconds();
        long d4 = Duration.between(zonedNow, zonedNext14).getSeconds();
        long d5 = Duration.between(zonedNow, zonedNext14_2).getSeconds();
        long d6 = Duration.between(zonedNow, zonedNext14_3).getSeconds();
        long d7 = Duration.between(zonedNow, zonedNext18).getSeconds();
        long d8 = Duration.between(zonedNow, zonedNext18_2).getSeconds();
        long d9 = Duration.between(zonedNow, zonedNext18_3).getSeconds();
        long d10 = Duration.between(zonedNow, zonedNext22).getSeconds();
        long d11 = Duration.between(zonedNow, zonedNext22_2).getSeconds();
        long d12 = Duration.between(zonedNow, zonedNext22_3).getSeconds();

        ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 40 minutes!"), d1,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 30 minutes!"), d2,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 20 minutes!"), d3,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 40 minutes!"), d4,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 30 minutes!"), d5,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 20 minutes!"), d6,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 40 minutes!"), d7,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 30 minutes!"), d8,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 20 minutes!"), d9,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 40 minutes!"), d10,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 30 minutes!"), d11,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 20 minutes!"), d12,
                24 * 60 * 60, TimeUnit.SECONDS);
    }
}
