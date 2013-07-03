package watson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.src.ModLoader;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ForgeSubscribe;
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
import watson.db.BlockEdit;
import watson.db.BlockEditSet;
import watson.db.BlockTypeRegistry;
import watson.db.Filters;
import watson.debug.Log;
import watson.macro.MacroIntegration;
import clientcommands.mod_ClientCommands;

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
   * Subscribe to incoming chats and pass them through Watson's filters.
   * 
   * Cancel the event so that it doesn't hit the GUI.
   */
  @ForgeSubscribe
  public void onClientChatReceived(ClientChatReceivedEvent event)
  {
    ChatProcessor.getInstance().addChatToQueue(event.message);
    event.setCanceled(true);
  }

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
   * Return the full version string, in the form <version>-YYYY-MM-DD, where
   * <version> should match the Minecraft version number.
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
   * 
   * @param prefix the start of the file name to match.
   * @param page the 1-based page number of the resuls to list.
   */
  public void listBlockEditFiles(String prefix, int page)
  {
    File[] files = getBlockEditFileList(prefix);
    if (files.length == 0)
    {
      localOutput("No matching files.");
    }
    else
    {
      if (files.length == 1)
      {
        localOutput("1 matching file:");
      }
      else
      {
        localOutput(files.length + " matching files:");
      }

      int pages = (files.length + PAGE_LINES - 1) / PAGE_LINES;
      if (page > pages)
      {
        localError(String.format(Locale.US, "The highest page number is %d.",
          pages));
      }
      else
      {
        localOutput(String.format(Locale.US, "Page %d of %d.", page, pages));

        // page <= pages
        int start = (page - 1) * PAGE_LINES;
        int end = Math.min(files.length, page * PAGE_LINES);

        for (int i = start; i < end; ++i)
        {
          localOutput("    " + files[i].getName());
        }

        localOutput(String.format(Locale.US, "Page %d of %d.", page, pages));
        if (page < pages)
        {
          localOutput(String.format(Locale.US,
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
          localOutput("Deleted " + file.getName());
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
        localOutput(message);
      }
      else
      {
        localError(message);
      }
    }
    else
    {
      localOutput(String.format(Locale.US,
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
        localError(date + " is not a valid date of the form YYYY-MM-DD.");
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
            localOutput("Deleted " + file.getName());
          }
          else
          {
            ++failed;
            localError("Could not delete " + file.getName());
          }
        }
      } // for

      if (deleted + failed == 0)
      {
        localOutput("There are no save files older than " + date
                    + " 00:00:00 to delete.");
      }
      else
      {
        String message = String.format(Locale.US,
          "Deleted %d out of %d save files older than %s 00:00:00.", deleted,
          deleted + failed, date);
        if (failed == 0)
        {
          localOutput(message);
        }
        else
        {
          localError(message);
        }
      }
    }
    else
    {
      localError("The date must take the form YYYY-MM-DD.");
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
    localOutput("Watson edits cleared.");
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
    MacroIntegration.sendEvent(MacroIntegration.ON_WATSON_SELECTION);
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
    MacroIntegration.sendEvent(MacroIntegration.ON_WATSON_SELECTION);
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
   * Save a screenshot.
   */
  public void saveScreenshot()
  {
    Minecraft mc = ModLoader.getMinecraftInstance();
    String path = getScreenshotPath(new File(mc.getMinecraftDir(), "screenshots"));
    localChat(ScreenShotHelper.func_74292_a(
      mc.getMinecraftDir(), path, mc.displayWidth, mc.displayHeight));
  }

  // --------------------------------------------------------------------------
  /**
   * Return the path of the next screenshot file to save relative to the
   * Minecraft screenshots/ directory.
   * 
   * @param screenshots the Minecraft screenshots directory.
   * @param now the actual timestamp used to format basename.
   * @return the path of the next screenshot file to save relative to the
   *         Minecraft screenshots/ directory.
   */
  public String getScreenshotPath(File screenshots)
  {
    Date now = new Date();
    String baseName = _dateFormat.format(now).toString();
    String player = (String) getVariables().get("player");
    String subdirectory = getScreenshotSubdirectory(screenshots, player, now);
    if (!subdirectory.isEmpty())
    {
      subdirectory = subdirectory + File.separator;
    }

    int count = 1;
    boolean useSuffix = (player != null && Configuration.instance.isSsPlayerSuffix());
    String playerSuffix = useSuffix ? "-" + player : "";
    while (true)
    {
      String relativePath =
        subdirectory + baseName + playerSuffix + (count == 1 ? "" : "-" + count) + ".png";
      File result = new File(screenshots, relativePath);
      if (!result.exists())
      {
        return relativePath;
      }
      ++count;
    } // while
  } // getScreenshotPath

  // --------------------------------------------------------------------------
  /**
   * Return the directory where the screenshot should be saved, based on the
   * player name, the current time and the settings.
   * 
   * The subdirectory will be created if it does not exist.
   * 
   * @param screenshots the Minecraft screenshots directory.
   * @param player the player name or null if not known.
   * @param now the current time.
   * @return the name of the directory where the screenshot should be saved,
   *         based on the player name, the current time and the settings.
   */
  protected String getScreenshotSubdirectory(File screenshots, String player,
                                             Date now)
  {
    Configuration config = Configuration.instance;
    String subdirectoryName = (player != null && config.isSsPlayerDirectory())
      ? player : config.getSsDateDirectory().format(now).toString();

    // If we can create the subdirectory, use it.
    File subdirectory = new File(screenshots, subdirectoryName);
    try
    {
      if (!subdirectory.isDirectory())
      {
        subdirectory.mkdirs();
      }
      if (subdirectory.isDirectory())
      {
        return subdirectoryName;
      }
    }
    catch (Exception ex)
    {
      // Fall through.
      Log.exception(Level.WARNING, "error creating screenshot subdirectory", ex);
    }

    // No additional directories added to screenshots.
    return "";
  } // getScreenshotSubdirectory

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
   * Determines which edits are stored (in a {@link BlocKEditSet}) and which are
   * ignored.
   */
  protected Filters                       _filters         = new Filters();

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

  /**
   * The vanilla Minecraft screenshot filename date format.
   */
  protected static final DateFormat       _dateFormat      = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

} // class Controller
