package watson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.GuiNewChat;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.mod_ClientCommands;
import net.minecraft.src.mod_Watson;
import watson.analysis.Sherlock;
import watson.chat.ChatHighlighter;
import watson.chat.ChatProcessor;
import watson.chat.Colour;
import watson.cli.AnnoCommand;
import watson.cli.CalcCommand;
import watson.cli.CaseInsensitivePrefixFileFilter;
import watson.cli.HighlightCommand;
import watson.cli.TagCommand;
import watson.cli.WatsonCommand;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * Provides a centralised Facade to control the facilities of this mod.
 */
public class Controller
{
  // --------------------------------------------------------------------------
  /**
   * Singleton.
   */
  public static final Controller instance      = new Controller();

  // --------------------------------------------------------------------------
  /**
   * The colour of the output of Watson commands.
   */
  public static final String     OUTPUT_COLOUR = Colour.lightblue.getCodeString();

  /**
   * The colour of the output of Watson error messages.
   */
  public static final String     ERROR_COLOUR  = Colour.red.getCodeString();

  // --------------------------------------------------------------------------
  /**
   * Mod-wide initialisation tasks, including loading configuration files and
   * setting up commands.
   */
  public void initialise()
  {
    ChatProcessor.getInstance().loadChatCategories();
    ChatProcessor.getInstance().loadChatExclusions();
    _sherlock = new Sherlock(ChatProcessor.getInstance().getChatClassifier());
    BlockTypeRegistry.instance.loadBlockTypes();
    _chatHighlighter.loadHighlights();

    // Set up some extra chat exclusions that may not yet be in the config file
    // but ought to be. The "No results found." line is suppressed to hide
    // the LogBlock query that checks server time. The line is manually
    // re-echoed after that.
    ChatProcessor.getInstance().setChatTagVisible("lb.header.timecheck", false);
    ChatProcessor.getInstance().setChatTagVisible("lb.header.noresults", false);

    // Initialise the commands.
    mod_ClientCommands.getInstance().registerCommand(new WatsonCommand());
    mod_ClientCommands.getInstance().registerCommand(new AnnoCommand());
    mod_ClientCommands.getInstance().registerCommand(new TagCommand());
    mod_ClientCommands.getInstance().registerCommand(new HighlightCommand());
    mod_ClientCommands.getInstance().registerCommand(new CalcCommand());
  }

  // --------------------------------------------------------------------------
  /**
   * Return the full version string, in the form:
   * 
   * <pre>
   * version-YYYY-MM-DD)
   * </pre>
   * 
   * where version should match the Minecraft version number.
   * 
   * The version text is loaded from the watson/version resource.
   * 
   * @return the full version string.
   */
  public String getVersion()
  {
    if (_version == null)
    {
      ClassLoader loader = getClass().getClassLoader();
      InputStream in = loader.getResourceAsStream(MOD_PACKAGE + "/version");
      if (in == null)
      {
        _version = "unknown";
      }
      else
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try
        {
          _version = reader.readLine();
        }
        catch (IOException ex)
        {
          Log.exception(Level.SEVERE, "error reading version resource", ex);
          _version = "unknown";
        }
      }
    }
    return _version;
  } // getVersion

  // --------------------------------------------------------------------------
  /**
   * Return the initial numeric portion of the version, which should be of the
   * form <major>.<minor>[.<micro>].
   * 
   * @return the initial numeric portion of the version.
   */
  public String getVersionNumber()
  {
    final Pattern PATTERN = Pattern.compile("\\d+(\\.\\d+)+");
    Matcher matcher = PATTERN.matcher(getVersion());
    return (matcher.find()) ? matcher.group() : "";
  }

  // --------------------------------------------------------------------------
  /**
   * Return the Sherlock instance.
   * 
   * @return the Sherlock instance.
   */
  public Sherlock getSherlock()
  {
    return _sherlock;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the (@link ChatHighlighter} that colours naughty words and whatnot
   * in chat lines.
   * 
   * @return the {@link ChatHighlighter}.
   */
  public ChatHighlighter getChatHighlighter()
  {
    return _chatHighlighter;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link DisplaySettings} which control what is drawn.
   * 
   * @return the {@link DisplaySettings} which control what is drawn.
   */
  public DisplaySettings getDisplaySettings()
  {
    return _displaySettings;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the IP address of DNS name of the currently connected server, or
   * null if not connected.
   * 
   * @return the IP address of DNS name of the currently connected server, or
   *         null if not connected.
   */
  public String getServerIP()
  {
    Minecraft mc = ModLoader.getMinecraftInstance();
    return (!mc.isSingleplayer() && mc.getServerData() != null) ? mc.getServerData().serverIP
      : null;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the current {@link BlockEditSet} under examination.
   * 
   * A separate {@link BlockEditSet} is maintained for each dimension
   * (overworld, nether, end).
   * 
   * @return the current {@link BlockEditSet} under examination.
   */
  public BlockEditSet getBlockEditSet()
  {
    // Compute id of the form: address/dimension
    // Note: Minecraft.theWorld.getWorldInfo().getDimension() doesn't update.
    Minecraft mc = ModLoader.getMinecraftInstance();
    StringBuilder idBuilder = new StringBuilder();

    // This code might get referenced at startup when changing display settings
    // if the mod happens to be disabled in the config file. At that time,
    // getServerIP() will be null. Let's avoid that crash.
    String serverIP = getServerIP();
    if (serverIP != null)
    {
      idBuilder.append(serverIP);
    }
    idBuilder.append('/');
    idBuilder.append(mc.thePlayer.dimension);
    String id = idBuilder.toString();

    // Lookup BlockEditSet or create new mapping if not found.
    BlockEditSet edits = _edits.get(id);
    if (edits == null)
    {
      edits = new BlockEditSet();
      _edits.put(id, edits);
    }
    return edits;
  } // getBlockEditSet

  // --------------------------------------------------------------------------
  /**
   * Save the current {@link BlockEditSet} to the specified file in
   * getSaveDirectory().
   * 
   * @param fileName the file name to write; if it is null and there is a
   *          current player variable value, a default file name of the form
   *          player-YYYY-MM-DD-hh.mm.ss is used.
   * 
   */
  public void saveBlockEditFile(String fileName)
  {
    // Compute default fileName?
    if (fileName == null)
    {
      String player = (String) getVariables().get("player");
      if (player == null)
      {
        localError("No current player set, so you must specify a file name.");
        return;
      }
      else
      {
        Calendar calendar = Calendar.getInstance();
        fileName = String.format(Locale.US, "%s-%4d-%02d-%02d-%02d.%02d.%02d",
          player, calendar.get(Calendar.YEAR),
          calendar.get(Calendar.MONTH) + 1,
          calendar.get(Calendar.DAY_OF_MONTH),
          calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
          calendar.get(Calendar.SECOND));
      }
    } // if

    createBlockEditDirectory();

    File file = new File(getBlockEditDirectory(), fileName);
    try
    {
      BlockEditSet edits = getBlockEditSet();
      int editCount = edits.save(file);
      int annoCount = edits.getAnnotations().size();
      localOutput(String.format(Locale.US,
        "Saved %d edits and %d annotations to %s", editCount, annoCount,
        fileName));
    }
    catch (IOException ex)
    {
      Log.exception(Level.SEVERE, "error saving BlockEditSet to " + file, ex);
      localError("The file " + fileName + " could not be saved.");
    }
  } // saveBlockEditFile

  // --------------------------------------------------------------------------
  /**
   * Load the set of {@link BlockEdit}s from the specified file.
   * 
   * @param fileName the file name, or the start of the file name (beginning of
   *          player name), in the BlockEdit saves directory.
   * 
   * @TODO: Does this need to be smarter about which dimension/server we're in?
   */
  public void loadBlockEditFile(String fileName)
  {
    File file = new File(getBlockEditDirectory(), fileName);
    if (!file.canRead())
    {
      // Try to find a file that begins with fileName, i.e. treat that as the
      // player name.
      File[] files = getBlockEditFileList(fileName);
      if (files.length > 0)
      {
        // Chose the most recent matching file.
        file = files[files.length - 1];
      }
    }

    if (file.canRead())
    {
      try
      {
        BlockEditSet edits = getBlockEditSet();
        int editCount = edits.load(file);
        int annoCount = edits.getAnnotations().size();
        localOutput(String.format(Locale.US,
          "Loaded %d edits and %d annotations from %s", editCount, annoCount,
          file.getName()));
      }
      catch (Exception ex)
      {
        Log.exception(Level.SEVERE, "error loading BlockEditSet from " + file,
          ex);
        localError("The file " + fileName + " could not be loaded.");
      }
    }
    else
    {
      localError("Can't open " + fileName + " to read.");
    }
  } // loadBlockEditFile

  // --------------------------------------------------------------------------
  /**
   * List all of the {@link BlockEditSet} save files whose names begin with the
   * specified prefix, matched case-insensitively.
   */
  public void listBlockEditFiles(String prefix)
  {
    File[] files = getBlockEditFileList(prefix);
    if (files.length == 0)
    {
      localOutput("No matching files.");
    }
    else if (files.length == 1)
    {
      localOutput("1 matching file:");
    }
    else
    {
      localOutput(files.length + " matching files:");
    }

    for (File file : files)
    {
      localOutput(file.getName());
    }
  } // listBlockEditFiles

  // --------------------------------------------------------------------------
  /**
   * Return an array of {@link BlockEditSet} save files whose names begin with
   * the specified prefix, matched case insensitively.
   * 
   * @param prefix the case-insensitive prefix.
   * @return the array of files.
   */
  public File[] getBlockEditFileList(String prefix)
  {
    File[] files = getBlockEditDirectory().listFiles(
      new CaseInsensitivePrefixFileFilter(prefix));
    Arrays.sort(files);
    return files;
  }

  // --------------------------------------------------------------------------
  /**
   * Clear the BlockEditSet for the current server and dimension.
   * 
   * Also clear the variables scraped from chat.
   */
  public void clearBlockEditSet()
  {
    getBlockEditSet().clear();
    _variables.clear();
    localOutput("Watson edits cleared.");
  }

  // --------------------------------------------------------------------------
  /**
   * Issue a LogBlock query that selects the edits that came immediately before
   * the most recent coalblock, /lb query result, or /lb tp destination. The
   * query takes the form:
   * 
   * <pre>
   * /lb before DD.MM.YYYY hh:mm:ss player name coords limit 45
   * </pre>
   * 
   * This method is called in response to the "/w pre" command.
   */
  public void queryPreviousEdits()
  {
    if (_variables.containsKey("player") && _variables.containsKey("time"))
    {
      _calendar.setTimeInMillis((Long) _variables.get("time"));
      int day = _calendar.get(Calendar.DAY_OF_MONTH);
      int month = _calendar.get(Calendar.MONTH) + 1;
      int year = _calendar.get(Calendar.YEAR);
      int hour = _calendar.get(Calendar.HOUR_OF_DAY);
      int minute = _calendar.get(Calendar.MINUTE);
      int second = _calendar.get(Calendar.SECOND);
      String player = (String) _variables.get("player");

      String query = String.format(Locale.US,
        "/lb before %d.%d.%d %02d:%02d:%02d player %s coords limit 45", day,
        month, year, hour, minute, second, player);
      Log.debug(query);
      serverChat(query);
    }
  } // queryPreviousEdits

  // --------------------------------------------------------------------------
  /**
   * Get a mutable reference to the Map of all of the variables scraped from
   * chat lines.
   * 
   * @return the variables.
   */
  public HashMap<String, Object> getVariables()
  {
    return _variables;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the current state variables from a {@link BlockEdit}, the same as they
   * would be set if they were set from a LogBlock toolblock (coal ore) query.
   * 
   * Do nothing if the edit is null.
   * 
   * @param edit the edit to select.
   */
  public void selectBlockEdit(BlockEdit edit)
  {
    if (edit != null)
    {
      _variables.put("time", edit.time);
      _variables.put("player", edit.player);
      _variables.put("block", edit.type.getId());
      _variables.put("x", edit.x);
      _variables.put("y", edit.y);
      _variables.put("z", edit.z);
    }
  } // selectBlockEdit

  // --------------------------------------------------------------------------
  /**
   * Teleport to the middle of the block specified by integer coordinates.
   * 
   * Essentials only accepts integer coordinates for teleports. Other plugins
   * use floating point coordinates. This method hides the difference by
   * formatting the teleport command using the
   * Configuration.getTeleportCommand() setting.
   * 
   * @param x the x coordinate of the block.
   * @param y the y coordinate of the block.
   * @param z the z coordinate of the block.
   */
  public void teleport(int x, int y, int z)
  {
    // Find %d and %g in the command format.
    String format = Configuration.instance.getTeleportCommand();
    Pattern specifier = Pattern.compile("%[dg]");
    Matcher specifiers = specifier.matcher(format);

    // If unspecified, default is false ("integer").
    BitSet isDouble = new BitSet();
    int i = 0;
    while (specifiers.find())
    {
      isDouble.set(i, specifiers.group().equals("%g"));
      ++i;
    }

    // I think (hope) it's reasonable to assume /tppos style commands will list
    // x, y and z in that order.
    Number nx = (isDouble.get(0) ? (Number) (x + 0.5) : x);
    Number ny = (isDouble.get(1) ? (Number) (y + 0.5) : y);
    Number nz = (isDouble.get(2) ? (Number) (z + 0.5) : z);
    String command = String.format(Locale.US, format, nx, ny, nz);
    Log.debug(command);
    serverChat(command);
  } // teleport

  // --------------------------------------------------------------------------
  /**
   * Display the specified chat message in the local client's chat GUI.
   * 
   * @param message the chat message to display.
   */
  public void localChat(String message)
  {
    if (getChatGui() != null)
    {
      getChatGui().printChatMessage(_chatHighlighter.highlight(message));
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Display the specified Watson command output in the local client's chat GUI.
   * 
   * Watson command outpus is all displayed in the colour OUTPUT_COLOUR;
   * 
   * @param message the message to display.
   */
  public void localOutput(String message)
  {
    localChat(OUTPUT_COLOUR + message);
  }

  // --------------------------------------------------------------------------
  /**
   * Display the specified error message in bright red in the local client's
   * chat GUI.
   * 
   * TODO: check with people with various types of colour blindness to see if
   * this is ok by them or causes problems.
   * 
   * @param message the chat message to display.
   */
  public void localError(String message)
  {
    if (getChatGui() != null)
    {
      getChatGui().printChatMessage(ERROR_COLOUR + message);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Queue the specified message in a chat packet for transmission to the
   * server.
   * 
   * @param message the chat message to send.
   */
  public void serverChat(String message)
  {
    _serverChatQueue.add(message);
  }

  // --------------------------------------------------------------------------
  /**
   * Send the specified chat message to the server immediately (not throttled).
   * 
   * @param message the chat message to send.
   */
  public void immediateServerChat(String message)
  {
    if (message != null)
    {
      ModLoader.clientSendPacket(new Packet3Chat(message));
    }
  }

  // --------------------------------------------------------------------------
  /**
   * To prevent Watson's "/w pre" command from automatically issuing /lb page
   * commands at a rate that would annoy the server's spam filter, outgoing
   * (programmatically generated) chat packets are added to this queue and rate
   * limited to at most one per Configuration.getChatTimeoutMillis()
   * milliseconds.
   */
  public void processServerChatQueue()
  {
    // Just in case System.currentTimeMillis() is still relatively expensive...
    if (!_serverChatQueue.isEmpty())
    {
      long now = System.currentTimeMillis();
      if (now - _lastServerChatTime >= (long) (1000 * Configuration.instance.getChatTimeoutSeconds()))
      {
        _lastServerChatTime = now;
        String message = _serverChatQueue.poll();
        immediateServerChat(message);
      }
    }
  } // processServerChatQueue

  // --------------------------------------------------------------------------
  /**
   * Private constructor to enforce Singleton pattern.
   */
  private Controller()
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * Return the cached reference to the Minecraft GuiNewChat instance that draws
   * the chat GUI, or null if not yet available.
   * 
   * @return the cached reference to the Minecraft GuiNewChat instance that
   *         draws the chat GUI, or null if not yet available.
   */
  private GuiNewChat getChatGui()
  {
    if (_chatGui == null)
    {
      Minecraft mc = ModLoader.getMinecraftInstance();
      if (mc != null)
      {
        GuiIngame ingame = mc.ingameGUI;
        if (ingame != null)
        {
          _chatGui = ingame.getChatGUI();
        }
      }
    }
    return _chatGui;
  } // getChatGui

  // --------------------------------------------------------------------------
  /**
   * Create the mod-specific subdirectory and subdirectories of that.
   */
  public static void createDirectories()
  {
    File modDir = getModDirectory();
    if (!modDir.isDirectory())
    {
      try
      {
        modDir.mkdirs();
      }
      catch (Exception ex)
      {
        Log.exception(Level.SEVERE,
          "could not create mod directory: " + modDir, ex);
      }
    }
  } // createDirectories

  // --------------------------------------------------------------------------
  /**
   * Ensure that the BlockEditSet saves directory exists.
   */
  public static void createBlockEditDirectory()
  {
    File dir = getBlockEditDirectory();
    if (!dir.isDirectory())
    {
      try
      {
        dir.mkdirs();
      }
      catch (Exception ex)
      {
        Log.exception(Level.SEVERE, "could not create saves directory: " + dir,
          ex);
      }
    }
  } // createBlockEditDirectory

  // --------------------------------------------------------------------------
  /**
   * Return the directory where this mod's data files are stored.
   * 
   * @return the directory where this mod's data files are stored.
   */
  public static File getModDirectory()
  {
    File minecraftDir = Minecraft.getMinecraftDir();
    return new File(minecraftDir, MOD_SUBDIR);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the directory where BlockEditSet files are saved.
   * 
   * @return the directory where BlockEditSet files are saved.
   */
  public static File getBlockEditDirectory()
  {
    return new File(getModDirectory(), SAVE_SUBDIR);
  }

  // --------------------------------------------------------------------------
  /**
   * Return an input stream that reads the specified file or resource name.
   * 
   * If the file exists in the mod-specific configuration directory, it is
   * loaded from there. Otherwise, the resource of the same name is loaded from
   * the minecraft.jar file.
   * 
   * @return an input stream that reads the specified file or resource name.
   */
  public static InputStream getConfigurationStream(String fileName)
    throws IOException
  {
    File file = new File(Controller.getModDirectory(), fileName);
    if (file.canRead())
    {
      Log.info("Loading \"" + fileName + "\" from file.");
      return new BufferedInputStream(new FileInputStream(file));
    }
    else
    {
      Log.info("Loading \"" + fileName + "\" from resource in minecraft.jar.");
      ClassLoader loader = mod_Watson.class.getClassLoader();
      return loader.getResourceAsStream(Controller.MOD_PACKAGE + '/' + fileName);
    }
  } // getConfigurationStream

  // --------------------------------------------------------------------------
  /**
   * Cache the version string after it is loaded from a resource.
   */
  protected String                        _version;

  /**
   * Makes inferences based on LogBlock query results.
   */
  protected Sherlock                      _sherlock;

  /**
   * The settings affecting what is displayed and how.
   */
  protected DisplaySettings               _displaySettings = new DisplaySettings();

  /**
   * A map from the a String containing the server address and dimension number
   * to the corresponding set of {@link BlockEdit}s that are displayed by
   * {@link RenderWatson}.
   */
  protected HashMap<String, BlockEditSet> _edits           = new HashMap<String, BlockEditSet>();

  /**
   * A cached reference to the GuiNewChat instance, set up as soon as it becomes
   * available.
   */
  protected GuiNewChat                    _chatGui;

  /**
   * The chat highlighter.
   */
  protected ChatHighlighter               _chatHighlighter = new ChatHighlighter();

  /**
   * Map from name to value of all of the variables scraped from chat lines.
   */
  protected HashMap<String, Object>       _variables       = new HashMap<String, Object>();

  /**
   * Used to compute time stamps for queryPreviousEdits().
   */
  protected Calendar                      _calendar        = Calendar.getInstance();

  /**
   * A queue of programmatically generated chats (commands to be sent to the
   * server).
   */
  protected ConcurrentLinkedQueue<String> _serverChatQueue = new ConcurrentLinkedQueue<String>();

  /**
   * The last local time at which a queued up chat was sent to the server.
   */
  protected long                          _lastServerChatTime;

  /**
   * The main package name of the classes of this mod, and also the name of the
   * subdirectory of .minecraft/mods/ where mod-specific settings are stored.
   */
  protected static final String           MOD_PACKAGE      = "watson";

  /**
   * Directory where mod files reside, relative to the .minecraft/ directory.
   */
  protected static final String           MOD_SUBDIR       = "mods"
                                                             + File.separator
                                                             + MOD_PACKAGE;
  /**
   * Subdirectory of the mod specific directory where {@link BlockEditSet}s are
   * saved.
   */
  protected static final String           SAVE_SUBDIR      = "saves";

} // class Controller
