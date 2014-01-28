package watson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import net.minecraft.client.multiplayer.ServerData;
import watson.chat.Chat;
import watson.cli.AnnoCommand;
import watson.cli.CalcCommand;
import watson.cli.CaseInsensitivePrefixFileFilter;
import watson.cli.ClientCommandManager;
import watson.cli.HighlightCommand;
import watson.cli.WatsonCommand;
import watson.db.BlockEdit;
import watson.db.BlockEditSet;
import watson.db.BlockTypeRegistry;
import watson.db.Filters;
import watson.debug.Log;
// import watson.macro.MacroIntegration;

import com.google.gson.JsonParser;

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
  public static final Controller instance = new Controller();

  // --------------------------------------------------------------------------
  /**
   * Mod-wide initialisation tasks, including loading configuration files and
   * setting up commands.
   */
  public void initialise()
  {
    createBlockEditDirectory();
    BlockTypeRegistry.instance.loadBlockTypes();
    Chat.getChatHighlighter().loadHighlights();

    // Initialise the commands.
    ClientCommandManager.instance.registerCommand(new WatsonCommand());
    ClientCommandManager.instance.registerCommand(new AnnoCommand());
    ClientCommandManager.instance.registerCommand(new HighlightCommand());
    ClientCommandManager.instance.registerCommand(new CalcCommand());
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
   * Return the IP address or DNS name of the currently connected server, or
   * null if not connected.
   * 
   * @return the IP address or DNS name of the currently connected server, or
   *         null if not connected.
   */
  public String getServerIP()
  {
    Minecraft mc = Minecraft.getMinecraft();
    ServerData serverData = mc.func_147104_D();
    if (!mc.isSingleplayer() && serverData != null)
    {
      // TODO: test, tidy.
      // SocketAddress address =
      // mc.getNetHandler().getNetManager().getSocketAddress();
      // return address != null ? address.toString() : null;
      return serverData.serverIP;
    }
    else
    {
      return null;
    }
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
    Minecraft mc = Minecraft.getMinecraft();
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
        Chat.localError("No current player set, so you must specify a file name.");
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
      Chat.localOutput(String.format(Locale.US,
        "Saved %d edits and %d annotations to %s", editCount, annoCount,
        fileName));
    }
    catch (IOException ex)
    {
      Log.exception(Level.SEVERE, "error saving BlockEditSet to " + file, ex);
      Chat.localError("The file " + fileName + " could not be saved.");
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
        Chat.localOutput(String.format(Locale.US,
          "Loaded %d edits and %d annotations from %s", editCount, annoCount,
          file.getName()));
      }
      catch (Exception ex)
      {
        Log.exception(Level.SEVERE, "error loading BlockEditSet from " + file,
          ex);
        Chat.localError("The file " + fileName + " could not be loaded.");
      }
    }
    else
    {
      Chat.localError("Can't open " + fileName + " to read.");
    }
  } // loadBlockEditFile

  // --------------------------------------------------------------------------
  /**
   * List all of the {@link BlockEditSet} save files whose names begin with the
   * specified prefix, matched case-insensitively.
   * 
   * @param prefix the start of the file name to match.
   * @param page the 1-based page number of the resuls to list.
   */
  public void listBlockEditFiles(String prefix, int page)
  {
    File[] files = getBlockEditFileList(prefix);
    if (files.length == 0)
    {
      Chat.localOutput("No matching files.");
    }
    else
    {
      if (files.length == 1)
      {
        Chat.localOutput("1 matching file:");
      }
      else
      {
        Chat.localOutput(files.length + " matching files:");
      }

      int pages = (files.length + PAGE_LINES - 1) / PAGE_LINES;
      if (page > pages)
      {
        Chat.localError(String.format(Locale.US, "The highest page number is %d.",
          pages));
      }
      else
      {
        Chat.localOutput(String.format(Locale.US, "Page %d of %d.", page, pages));

        // page <= pages
        int start = (page - 1) * PAGE_LINES;
        int end = Math.min(files.length, page * PAGE_LINES);

        for (int i = start; i < end; ++i)
        {
          Chat.localOutput("    " + files[i].getName());
        }

        Chat.localOutput(String.format(Locale.US, "Page %d of %d.", page, pages));
        if (page < pages)
        {
          Chat.localOutput(String.format(Locale.US,
            "Use \"/w file list %s %d\" to see the next page.", prefix,
            (page + 1)));
        }
      } // if the page number is valid
    } // if there are matches
  } // listBlockEditFiles

  // --------------------------------------------------------------------------
  /**
   * Delete all save files that match the specified prefix.
   * 
   * @param prefix the prefix of the block edit save file to match; use "*" for
   *          all files.
   */
  public void deleteBlockEditFiles(String prefix)
  {
    File[] files = getBlockEditFileList(prefix);
    if (files.length > 0)
    {
      int failed = 0;
      for (File file : files)
      {
        if (file.delete())
        {
          Chat.localOutput("Deleted " + file.getName());
        }
        else
        {
          ++failed;
        }
      }
      String message = String.format(Locale.US,
        "Deleted %d out of %d save files matching \"%s\".",
        (files.length - failed), files.length, prefix);
      if (failed == 0)
      {
        Chat.localOutput(message);
      }
      else
      {
        Chat.localError(message);
      }
    }
    else
    {
      Chat.localOutput(String.format(Locale.US,
        "There are no save files matching \"%s\".", prefix));
    }
  } // deleteBlockEditFiles

  // --------------------------------------------------------------------------
  /**
   * Delete any edit save files that were last modified before the specified
   * date.
   * 
   * @param date the expiry date, in the form YYYY-MM-DD.
   */
  public void expireBlockEditFiles(String date)
  {
    Matcher m = DATE_PATTERN.matcher(date);
    if (m.matches())
    {
      Calendar expiry = Calendar.getInstance();
      long expiryTime;
      try
      {
        int year = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));

        // Turn off leniency so that weird dates throw.
        expiry.setLenient(false);
        expiry.set(year, month - 1, day, 0, 0);

        // Do a get() to force the error.
        expiryTime = expiry.getTimeInMillis();
      }
      catch (Exception ex)
      {
        // If we get to here, the user supplied an invalent date and Calendar
        // threw.
        Chat.localError(date + " is not a valid date of the form YYYY-MM-DD.");
        return;
      }

      // Keep track of total files deleted, and number of failures to delete.
      int deleted = 0;
      int failed = 0;
      File[] files = getBlockEditFileList("*");
      for (File file : files)
      {
        if (file.lastModified() < expiryTime)
        {
          if (file.delete())
          {
            ++deleted;
            Chat.localOutput("Deleted " + file.getName());
          }
          else
          {
            ++failed;
            Chat.localError("Could not delete " + file.getName());
          }
        }
      } // for

      if (deleted + failed == 0)
      {
        Chat.localOutput("There are no save files older than " + date
                         + " 00:00:00 to delete.");
      }
      else
      {
        String message = String.format(Locale.US,
          "Deleted %d out of %d save files older than %s 00:00:00.", deleted,
          deleted + failed, date);
        if (failed == 0)
        {
          Chat.localOutput(message);
        }
        else
        {
          Chat.localError(message);
        }
      }
    }
    else
    {
      Chat.localError("The date must take the form YYYY-MM-DD.");
    }
  } // expireBlockEditFiles

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
    createBlockEditDirectory();
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
    clearVariables();
    Chat.localOutput("Watson edits cleared.");
    getFilters().clear();
  }

  // --------------------------------------------------------------------------
  /**
   * Issue a LogBlock query that selects the edits that came immediately before
   * the most recent coalblock, /lb query result, or /lb tp destination. The
   * query takes the form:
   * 
   * <pre>
   * /lb before DD.MM.YYYY hh:mm:ss player name coords limit <count>
   * </pre>
   * 
   * This method is called in response to the "/w pre [<count>]" command.
   * 
   * @param count the maximum number of edits that should be returned.
   */
  public void queryPreEdits(int count)
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
        "/lb before %d.%d.%d %02d:%02d:%02d player %s coords limit %d", day,
        month, year, hour, minute, second, player, count);
      Log.debug(query);
      serverChat(query);
    }
  } // queryPreEdits

  // --------------------------------------------------------------------------
  /**
   * Issue a LogBlock query that selects the edits that came immediately after
   * the most recent coalblock, /lb query result, or /lb tp destination. The
   * query takes the form:
   * 
   * <pre>
   * /lb since DD.MM.YYYY hh:mm:ss player name coords limit <count> asc
   * </pre>
   * 
   * This method is called in response to the "/w post [<count>]" command.
   * 
   * @param count the maximum number of edits that should be returned.
   */
  public void queryPostEdits(int count)
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
        "/lb since %d.%d.%d %02d:%02d:%02d player %s coords limit %d asc", day,
        month, year, hour, minute, second, player, count);
      Log.debug(query);
      serverChat(query);
    }
  } // queryPostEdits

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
   * Clear all currently set state variables..
   */
  public void clearVariables()
  {
    _variables.clear();
    // TODO: Reinstate MacroIntegration.
    // MacroIntegration.sendEvent(MacroIntegration.ON_WATSON_SELECTION);
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
      _variables.put("id", edit.type.getId());
      _variables.put("data", edit.type.getData());
      _variables.put("block", edit.type.getName(0));
      _variables.put("creation", edit.creation);

      // Will also dispatch the onWatsonSelection Macro/Keybind event:
      selectPosition(edit.x, edit.y, edit.z);
    }
  } // selectBlockEdit

  // --------------------------------------------------------------------------
  /**
   * Set the coordinate variables x, y and z.
   * 
   * @param x the x.
   * @param y the y.
   * @param z the z.
   */
  public void selectPosition(int x, int y, int z)
  {
    _variables.put("x", x);
    _variables.put("y", y);
    _variables.put("z", z);
    // TODO: Reinstate MacroIntegration.
    // MacroIntegration.sendEvent(MacroIntegration.ON_WATSON_SELECTION);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link Filters} instance that determines which edits are stored
   * (in a {@link BlocKEditSet}) and which are ignored.
   * 
   * @return the {@link Filters} instance that determines which edits are stored
   *         (in a {@link BlocKEditSet}) and which are ignored.
   */
  public Filters getFilters()
  {
    return _filters;
  }

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
      Chat.serverChat(message);
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
    Minecraft mc = Minecraft.getMinecraft();
    return new File(mc.mcDataDir, MOD_SUBDIR);
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
      ClassLoader loader = Controller.class.getClassLoader();
      return loader.getResourceAsStream(Controller.MOD_PACKAGE + '/' + fileName);
    }
  } // getConfigurationStream

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
   * Number of chat lines in a page.
   */
  public static final int                 PAGE_LINES       = 50;

  /**
   * The pattern used to parse expiry dates for "/w file expire <date>".
   * Tolerate one or two digits each for month and day.
   */
  protected static final Pattern          DATE_PATTERN     = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");

  /**
   * Cache the version string after it is loaded from a resource.
   */
  protected String                        _version;

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
   * Determines which edits are stored (in a {@link BlocKEditSet}) and which are
   * ignored.
   */
  protected Filters                       _filters         = new Filters();

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
  protected static final String           MOD_SUBDIR       = "mods" + File.separator + MOD_PACKAGE;

  /**
   * Subdirectory of the mod specific directory where {@link BlockEditSet}s are
   * saved.
   */
  protected static final String           SAVE_SUBDIR      = "saves";

  /**
   * 
   */
  protected JsonParser                    _jsonParser      = new JsonParser();
} // class Controller
