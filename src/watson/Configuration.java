package watson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.lwjgl.input.Keyboard;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import watson.chat.Chat;
import watson.debug.Log;
import watson.gui.ModifiedKeyBinding;
import watson.gui.MouseButton;
import watson.yaml.MapValidatorNode;
import watson.yaml.SnakeValidator;
import watson.yaml.TypeValidatorNode;
import watson.yaml.ValidatorMessageSink;

// ----------------------------------------------------------------------------
/**
 * Loads and saves the Watson global configuration file.
 */
public class Configuration
{
  /**
   * Single instance of this class.
   */
  public static final Configuration instance             = new Configuration();

  /**
   * Key bind to show in-game Watson GUI.
   */
  public final ModifiedKeyBinding   KEYBIND_INGAME       = new ModifiedKeyBinding("Show in-game options:",
                                                                                  Keyboard.KEY_RETURN,
                                                                                  "", true, false, false);
  /**
   * Key bind to take a Watson-style screenshot (named after the player).
   */
  public final ModifiedKeyBinding   KEYBIND_SCREENSHOT   = new ModifiedKeyBinding("Take a screenshot:",
                                                                                  Keyboard.KEY_F12,
                                                                                  "", false, false, false);

  /**
   * Key bind to teleport to next ore.
   */
  public final ModifiedKeyBinding   KEYBIND_TP_NEXT      = new ModifiedKeyBinding("TP to next ore:",
                                                                                  MouseButton.SCROLL_DOWN.getCode(),
                                                                                  "", true, false, false);
  /**
   * Key bind to teleport to previous ore.
   */
  public final ModifiedKeyBinding   KEYBIND_TP_PREV      = new ModifiedKeyBinding("TP to previous ore:",
                                                                                  MouseButton.SCROLL_UP.getCode(),
                                                                                  "", true, false, false);
  /**
   * Key bind to query edits before the selection.
   */
  public final ModifiedKeyBinding   KEYBIND_QUERY_BEFORE = new ModifiedKeyBinding("Query edits before:",
                                                                                  MouseButton.MOUSE_LEFT.getCode(),
                                                                                  "", true, false, false);
  /**
   * Key bind to query edits after the selection.
   */
  public final ModifiedKeyBinding   KEYBIND_QUERY_AFTER  = new ModifiedKeyBinding("Query edits after:",
                                                                                  MouseButton.MOUSE_RIGHT.getCode(),
                                                                                  "", true, false, false);

  /**
   * Key bind to move the cursor to next edit.
   */
  public final ModifiedKeyBinding   KEYBIND_CURSOR_NEXT  = new ModifiedKeyBinding("Cursor to next edit:",
                                                                                  MouseButton.SCROLL_DOWN.getCode(),
                                                                                  "", false, true, false);

  /**
   * Key bind to move the cursor to previous edit.
   */
  public final ModifiedKeyBinding   KEYBIND_CURSOR_PREV  = new ModifiedKeyBinding("Cursor to previous edit:",
                                                                                  MouseButton.SCROLL_UP.getCode(),
                                                                                  "", false, true, false);

  /**
   * Key bind to teleport to the cursor.
   */
  public final ModifiedKeyBinding   KEYBIND_TP_CURSOR    = new ModifiedKeyBinding("TP to cursor:",
                                                                                  MouseButton.MOUSE_LEFT.getCode(),
                                                                                  "", false, true, false);

  // --------------------------------------------------------------------------
  /**
   * Load the configuration file.
   */
  public void load()
  {
    configureValidator();

    Log.info("Loading \"" + CONFIG_FILE + "\" from file.");
    File config = new File(Controller.getModDirectory(), CONFIG_FILE);
    BufferedInputStream in;
    try
    {
      in = new BufferedInputStream(new FileInputStream(config));
      ValidatorMessageSink logSink = new ValidatorMessageSink()
      {
        @Override
        public void message(String text)
        {
          Log.config(text);
        }
      };

      @SuppressWarnings("unchecked")
      HashMap<String, Object> dom = (HashMap<String, Object>) _validator.loadAndValidate(
                                                                                         in, logSink);

      // Avoid calling setEnabled() at startup.
      // It would crash in Controller.getBlockEditSet() because server is not
      // set.
      _enabled = (Boolean) dom.get("enabled");
      // Avoid outputting a message in the client here:
      Log.setDebug((Boolean) dom.get("debug"));
      _autoPage = (Boolean) dom.get("auto_page");
      _regionInfoTimeoutSeconds = ((Number) dom.get("region_info_timeout")).doubleValue();
      _vectorsShown = (Boolean) dom.get("vectors_shown");
      _billboardBackground = (Integer) dom.get("billboard_background");
      _billboardForeground = (Integer) dom.get("billboard_foreground");
      _groupingOresInCreative = (Boolean) dom.get("group_ores_in_creative");
      _teleportCommand = (String) dom.get("teleport_command");
      _chatTimeoutSeconds = (Double) dom.get("chat_timeout");
      _maxAutoPages = (Integer) dom.get("max_auto_pages");
      _preCount = (Integer) dom.get("pre_count");
      _postCount = (Integer) dom.get("post_count");
      _watsonPrefix = (String) dom.get("watson_prefix");
      _ssPlayerDirectory = (Boolean) dom.get("ss_player_directory");
      _ssPlayerSuffix = (Boolean) dom.get("ss_player_suffix");
      setSsDateDirectoryImp((String) dom.get("ss_date_directory"));
      _reformatQueryResults = (Boolean) dom.get("reformat_query_results");
      _recolourQueryResults = (Boolean) dom.get("recolour_query_results");
      _timeOrderedDeposits = (Boolean) dom.get("time_ordered_deposits");
      _vectorLength = ((Double) dom.get("vector_length")).floatValue();

      for (Entry<String, ModifiedKeyBinding> entry : getKeyBindingsMap().entrySet())
      {
        entry.getValue().parse((String) dom.get(entry.getKey()));
      }
    }
    catch (Exception ex)
    {
      Log.config("Missing configuration file (" + config + "). Using defaults.");

      // So save a default config.
      save();
    }
  } // load

  // --------------------------------------------------------------------------
  /**
   * Save the configuration.
   */
  public void save()
  {
    File config = new File(Controller.getModDirectory(), CONFIG_FILE);
    BufferedWriter writer;
    try
    {
      writer = new BufferedWriter(new FileWriter(config));
      HashMap<String, Object> dom = new HashMap<String, Object>();
      dom.put("enabled", isEnabled());
      dom.put("debug", isDebug());
      dom.put("auto_page", isAutoPage());
      dom.put("region_info_timeout", getRegionInfoTimeoutSeconds());
      dom.put("vectors_shown", getVectorsShown());
      dom.put("billboard_background", getBillboardBackground());
      dom.put("billboard_foreground", getBillboardForeground());
      dom.put("group_ores_in_creative", isGroupingOresInCreative());
      dom.put("teleport_command", getTeleportCommand());
      dom.put("chat_timeout", getChatTimeoutSeconds());
      dom.put("max_auto_pages", getMaxAutoPages());
      dom.put("pre_count", getPreCount());
      dom.put("post_count", getPostCount());
      dom.put("watson_prefix", getWatsonPrefix());
      dom.put("ss_player_directory", _ssPlayerDirectory);
      dom.put("ss_player_suffix", _ssPlayerSuffix);
      dom.put("ss_date_directory", _ssDateDirectory.toPattern());
      dom.put("reformat_query_results", _reformatQueryResults);
      dom.put("recolour_query_results", _recolourQueryResults);
      dom.put("time_ordered_deposits", _timeOrderedDeposits);
      dom.put("vector_length", (double) _vectorLength);

      for (Entry<String, ModifiedKeyBinding> entry : getKeyBindingsMap().entrySet())
      {
        dom.put(entry.getKey(), entry.getValue().toString());
      }

      DumperOptions options = new DumperOptions();
      options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
      Yaml yaml = new Yaml(options);
      yaml.dump(dom, writer);
    }
    catch (IOException ex)
    {
      Log.exception(Level.SEVERE, "unable to save configuration file", ex);
    }
  } // save

  // --------------------------------------------------------------------------
  /**
   * Enable or disable the Watson mod as a whole.
   *
   * When disabled, only the /w config enable and /w help commands are
   * available, and Watson does no processing of chat or rendering of edits.
   *
   * @param enabled if true, Watson is enabled.
   */
  public void setEnabled(boolean enabled)
  {
    // When disabling, clear the Watson state so that player names are not
    // appended in screenshot filenames.
    if (_enabled && !enabled)
    {
      Controller.instance.clearBlockEditSet();
    }
    _enabled = enabled;
    if (_enabled)
    {
      Chat.localOutput("Watson is now enabled.");
    }
    else
    {
      Chat.localOutput("Watson is now disabled.");
      Chat.localOutput("To re-enable, use: /w config watson on");
    }
    save();
  } // setEnabled

  // --------------------------------------------------------------------------
  /**
   * Return true if Watson, the mod as a whole, is enabled.
   *
   * @return true if Watson, the mod as a whole, is enabled.
   */
  public boolean isEnabled()
  {
    return _enabled;
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable debug logging.
   *
   * @param debug if true, debug messages will be logged.
   */
  public void setDebug(boolean debug)
  {
    Log.setDebug(debug);
    Chat.localOutput("Debug level logging "
                     + (debug ? "enabled." : "disabled."));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if Watson, the mod as a whole, is enabled.
   *
   * @return true if Watson, the mod as a whole, is enabled.
   */
  public boolean isDebug()
  {
    return Log.isDebug();
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable automatic paging through "/lb coords" results, up to 3
   * pages.
   *
   * @param enabled if true, automatic paging is enabled up to the limit.
   */
  public void setAutoPage(boolean enabled)
  {
    _autoPage = enabled;
    Chat.localOutput("Automatic paging "
                     + (enabled ? "enabled." : "disabled."));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if automatic paging through "/lb coords" results is enabled.
   *
   * @return true if automatic paging through "/lb coords" results is enabled.
   */
  public boolean isAutoPage()
  {
    return _autoPage;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the timeout, in seconds, between automatic calls to "/region info" when
   * right clicking with a wooden sword.
   *
   * The time separation between "/region info" commands will be at least this.
   * The timeout has a lower bound, which is dictated by the command spam
   * detector on s.nerd.nu. Allowing "/region info" commands to be issued faster
   * than that would just lead to them being cued up for sending to the server
   * (bad) or we could send them to the server and aggravate the spam filter
   * (bad).
   *
   * @param seconds the timeout in seconds.
   */
  public void setRegionInfoTimeoutSeconds(double seconds)
  {
    if (seconds < MIN_REGION_INFO_TIMEOUT)
    {
      seconds = MIN_REGION_INFO_TIMEOUT;
    }
    _regionInfoTimeoutSeconds = seconds;
    Chat.localOutput(String.format(Locale.US,
                                   "Automatic region info timeout set to %.2f seconds.", seconds));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the timeout between automatic calls to "/region info", in seconds.
   *
   * @return the timeout between automatic calls to "/region info", in seconds.
   */
  public double getRegionInfoTimeoutSeconds()
  {
    return _regionInfoTimeoutSeconds;
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable the vector display.
   *
   * @param dispayVectors if true, the vector display will be visible.
   */
  public void setVectorsShown(boolean dispayVectors)
  {
    _vectorsShown = dispayVectors;
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the vector display will be visible.
   *
   * @return true if the vector display will be visible.
   */
  public boolean getVectorsShown()
  {
    return _vectorsShown;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the background colour of billboards to the specified ARGB colour.
   *
   * @param argb the colour; alpha in the most significant octet, blue in the
   *          least significant octet.
   */
  public void setBillboardBackground(int argb)
  {
    _billboardBackground = argb;
    Chat.localOutput(String.format(Locale.US,
                                   "Billboard background colour set to #%08X.", _billboardBackground));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the background colour of billboards to the specified ARGB colour.
   *
   * @return the background colour of billboards to the specified ARGB colour.
   */
  public int getBillboardBackground()
  {
    return _billboardBackground;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the foreground colour of billboards to the specified ARGB colour.
   *
   * @param argb the colour; alpha in the most significant octet, blue in the
   *          least significant octet.
   */
  public void setBillboardForeground(int argb)
  {
    _billboardForeground = argb;
    Chat.localOutput(String.format(Locale.US,
                                   "Billboard foreground colour set to #%08X.", _billboardForeground));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the foreground colour of billboards to the specified ARGB colour.
   *
   * @return the foreground colour of billboards to the specified ARGB colour.
   */
  public int getBillboardForeground()
  {
    return _billboardForeground;
  }

  // --------------------------------------------------------------------------
  /**
   * Control whether ores are grouped even in creative mode.
   *
   * By default, ores receive no special treatment in creative mode.
   *
   * @param groupingOresInCreative if true, ores are grouped and can be listed
   *          using /w ores in creative mode. If false, that facility is only
   *          available in survival type modes.
   */
  public void setGroupingOresInCreative(boolean groupingOresInCreative)
  {
    _groupingOresInCreative = groupingOresInCreative;
    Chat.localOutput((_groupingOresInCreative ? "Enabled"
      : "Disabled") + " grouping of ores in creative mode.");
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if grouping of ores is enabled even in creative mode.
   *
   * @return true if grouping of ores is enabled even in creative mode.
   */
  public boolean isGroupingOresInCreative()
  {
    return _groupingOresInCreative;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the format string used to generate the teleport command set to the
   * server.
   *
   * @param format a format string suitable for use with String.format(); but
   *          only %d and %g are recognised as numeric format specifiers.
   */
  public void setTeleportCommand(String format)
  {
    _teleportCommand = format;
    Chat.localOutput(String.format(Locale.US,
                                   "Teleport command format set to \"%s\".", _teleportCommand));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the format string used to generate the teleport command set to the
   * server.
   *
   * @return the format string used to generate the teleport command set to the
   *         server.
   */
  public String getTeleportCommand()
  {
    return _teleportCommand;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the minimum number of seconds that must elapse between programmatically
   * sent chat messages (usually commands to the server).
   *
   * @param seconds timeout in seconds.
   */
  public void setChatTimeoutSeconds(double seconds)
  {
    if (seconds < 0.0)
    {
      seconds = 0.0;
    }
    _chatTimeoutSeconds = seconds;
    Chat.localOutput(String.format(Locale.US,
                                   "Chat command timeout set to %.2f seconds.", seconds));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the minimum number of seconds that must elapse between
   * programmatically sent chat messages (usually commands to the server).
   *
   * @return the minimum number of seconds that must elapse between
   *         programmatically sent chat messages (usually commands to the
   *         server).
   */
  public double getChatTimeoutSeconds()
  {
    return _chatTimeoutSeconds;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the maximum number of pages of "/lb coords" results that will be
   * automatically stepped through by issuing "/lb page #" commands.
   *
   * @param maxAutoPages the number of pages.
   */
  public void setMaxAutoPages(int maxAutoPages)
  {
    _maxAutoPages = maxAutoPages;
    Chat.localOutput(String.format(Locale.US,
                                   "Up to %d pages of \"/lb coords\" results will be stepped through automatically.",
                                   maxAutoPages));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the maximum number of pages of "/lb coords" results that will be
   * automatically stepped through by issuing "/lb page #" commands.
   *
   * @return the maximum number of pages of "/lb coords" results that will be
   *         automatically stepped through by issuing "/lb page #" commands.
   */
  public int getMaxAutoPages()
  {
    return _maxAutoPages;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the number of edits to fetch from LogBlock when "/w pre" is run.
   *
   * @param preCount the number of edits.
   */
  public void setPreCount(int preCount)
  {
    _preCount = preCount;
    Chat.localOutput(String.format(Locale.US,
                                   "By default, \"/w pre\" will return %d edits.", _preCount));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the number of edits to fetch from LogBlock when "/w pre" is run.
   *
   * @return the number of edits to fetch from LogBlock when "/w pre" is run.
   */
  public int getPreCount()
  {
    return _preCount;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the number of edits to fetch from LogBlock when "/w post" is run.
   *
   * @param postCount the number of edits.
   */
  public void setPostCount(int postCount)
  {
    _postCount = postCount;
    Chat.localOutput(String.format(Locale.US,
                                   "By default, \"/w post\" will return %d edits.", _postCount));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the number of edits to fetch from LogBlock when "/w post" is run.
   *
   * @return the number of edits to fetch from LogBlock when "/w post" is run.
   */
  public int getPostCount()
  {
    return _postCount;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the start of all Watson commands, without the slash.
   *
   * The caller should ensure that the specified prefix consists entirely of
   * word characters ([a-zA-Z_0-9]), or it will be rejected.
   *
   * @param watsonPrefix the command prefix.
   */
  public void setWatsonPrefix(String watsonPrefix)
  {
    _watsonPrefix = watsonPrefix;
    Chat.localOutput(String.format(Locale.US,
                                   "Watson command prefix set to /%s.", _watsonPrefix));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the start of all Watson commands, without the slash.
   *
   * @return the start of all Watson commands, without the slash.
   */
  public String getWatsonPrefix()
  {
    return _watsonPrefix;
  }

  // --------------------------------------------------------------------------
  /**
   * If true, a subdirectory named after the currently selected player is
   * created to hold screenshots of his edits.
   *
   * @param ssPlayerDirectory whether to create the directory.
   */
  public void setSsPlayerDirectory(boolean ssPlayerDirectory)
  {
    _ssPlayerDirectory = ssPlayerDirectory;
    Chat.localOutput("Per-player screenshot subdirectories "
                     + (ssPlayerDirectory ? "enabled."
                       : "disabled."));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if a subdirectory named after the currently selected player is
   * created to hold screenshots of his edits.
   *
   * @return true if a subdirectory named after the currently selected player is
   *         created to hold screenshots of his edits.
   */
  public boolean isSsPlayerDirectory()
  {
    return _ssPlayerDirectory;
  }

  // --------------------------------------------------------------------------
  /**
   * If true, a the name of the currently selected player is appended to
   * screenshot files.
   *
   * @param ssPlayerSuffix whether to append the suffix.
   */
  public void setSsPlayerSuffix(boolean ssPlayerSuffix)
  {
    _ssPlayerSuffix = ssPlayerSuffix;
    Chat.localOutput("Per-player screenshot suffixes "
                     + (ssPlayerSuffix ? "enabled."
                       : "disabled."));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the name of the currently selected player is appended to
   * screenshot files.
   *
   * @return true if the name of the currently selected player is appended to
   *         screenshot files.
   */
  public boolean isSsPlayerSuffix()
  {
    return _ssPlayerSuffix;
  }

  // --------------------------------------------------------------------------
  /**
   * A {@link SimpleDateFormat} format string specifying the name of the
   * subdirectory to create to store screenshots when there is no currently
   * selected player, or when isPlayerDirectory() is false.
   *
   * @param ssDateDirectory the format specifier.
   */
  public void setSsDateDirectory(String ssDateDirectory)
  {
    if (setSsDateDirectoryImp(ssDateDirectory))
    {
      Chat.localOutput("Anonymous screenshot subdirectory format specifier set to \"" + ssDateDirectory + "\".");
      save();
    }
    else
    {
      Chat.localError("\"" + ssDateDirectory + "\" is not a valid format specifier.");
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Set the form format string specifying the name of the subdirectory to
   * create to store screenshots when there is no currently selected player, or
   * when isPlayerDirectory() is false.
   *
   * This is the underlying implementation of setSsDateDirectory(), without the
   * save() of the configuration or feedback into player chat.
   *
   * @param ssDateDirectory the format specifier.
   * @return true if successful.
   */
  protected boolean setSsDateDirectoryImp(String ssDateDirectory)
  {
    try
    {
      // TODO: Should this be applyLocalizedPattern()?
      _ssDateDirectory.applyPattern(ssDateDirectory);
      return true;
    }
    catch (Exception ex)
    {
      Log.exception(Level.WARNING,
                    "error setting the screenshot directory format specifier", ex);
    }
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the format specifier to use to name screenshot subdirectories based
   * on the current date and/or time.
   *
   * @return the format specifier to use to name screenshot subdirectories based
   *         on the current date and/or time.
   */
  public SimpleDateFormat getSsDateDirectory()
  {
    return _ssDateDirectory;
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable compact reformatting of query results in chat.
   *
   * @param reformatQueryResults if true, query results in chat are displayed in
   *          a more compact form.
   */
  public void setReformatQueryResults(boolean reformatQueryResults)
  {
    _reformatQueryResults = reformatQueryResults;
    Chat.localOutput(String.format(Locale.US,
                                   "Compact formatting of query results %s.", (_reformatQueryResults ? "enabled" : "disabled")));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if query results should be displayed in compact form.
   *
   * @return true if query results should be displayed in compact form.
   */
  public boolean getReformatQueryResults()
  {
    return _reformatQueryResults;
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable recolouring of query results in chat.
   *
   * @param recolourQueryResults if true, query results in chat are recoloured.
   */
  public void setRecolourQueryResults(boolean recolourQueryResults)
  {
    _recolourQueryResults = recolourQueryResults;
    Chat.localOutput(String.format(Locale.US,
                                   "Recolouring of query results %s.", (_recolourQueryResults ? "enabled" : "disabled")));
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if query results should be recoloured.
   *
   * @return true if query results should be recoloured.
   */
  public boolean getRecolourQueryResults()
  {
    return _recolourQueryResults;
  }

  // --------------------------------------------------------------------------
  /**
   * Enable or disable time ordering of deposits.
   *
   * @param enabled if true, number ore deposits according to their timestamps
   *          only; if false, ores are ordered rarest-first (i.e. diamonds
   *          before iron, iron before coal, etc.).
   */
  public void setTimeOrderedDeposits(boolean enabled)
  {
    _timeOrderedDeposits = enabled;
    Chat.localOutput(_timeOrderedDeposits
      ? "Ore deposits will be numbered according to their timestamps (only)."
      : "Ore deposits will be numbered in decreasing order of scarcity."
        );
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if ore deposits should be assigned 1-based numeric labels
   * strictly in the order that they were mined.
   *
   * If false, ore deposits are ordered in descending order of scarcity first,
   * and then time stamps are taken into account.
   *
   * @return true if ore deposits should be assigned 1-based numeric labels
   *         strictly in the order that they were mined.
   */
  public boolean timeOrderedDeposits()
  {
    return _timeOrderedDeposits;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the default minimum length of vectors for them to be visible.
   *
   * The current displayed minimum vector length is also set.
   *
   * @param length the minimum length of a vector for it to be visible.
   * @param showInChat if true a message indicating the new minimum vector
   *          length is shown in chat.
   */
  public void setVectorLength(float length, boolean showInChat)
  {
    _vectorLength = length;
    Controller.instance.getDisplaySettings().setMinVectorLength(length, showInChat);
    save();
  }

  // --------------------------------------------------------------------------
  /**
   * Set the default minimum length of vectors for them to be visible.
   *
   * The current displayed minimum vector length is also set.
   *
   * @param length the minimum length of a vector for it to be visible.
   */
  public float getVectorLength()
  {
    return _vectorLength;
  }

  // --------------------------------------------------------------------------
  /**
   * Return all {@link ModifiedKeyBindings} in the order they should be listed
   * in the configuration panel.
   *
   * @return all {@link ModifiedKeyBindings} in the order they should be listed
   *         in the configuration panel.
   */
  public List<ModifiedKeyBinding> getAllModifiedKeyBindings()
  {
    return _bindings;
  }

  // --------------------------------------------------------------------------
  /**
   * Perform lazy initialisation of the SnakeValidator used to validate in
   * load().
   */
  protected void configureValidator()
  {
    if (_validator == null)
    {
      _validator = new SnakeValidator();

      MapValidatorNode root = new MapValidatorNode();
      root.addChild("enabled", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("debug", new TypeValidatorNode(Boolean.class, true, false));
      root.addChild("auto_page", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("region_info_timeout", new TypeValidatorNode(Double.class, true, 5.0));
      root.addChild("vectors_shown", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("billboard_background", new TypeValidatorNode(Integer.class, true, 0x7F000000));
      root.addChild("billboard_foreground", new TypeValidatorNode(Integer.class, true, 0xFFFFFFFF));

      // Default to true until we can distinguish server vs player gamemode.
      root.addChild("group_ores_in_creative", new TypeValidatorNode(Boolean.class, true, true));

      root.addChild("teleport_command", new TypeValidatorNode(String.class, true, "/tppos %g %d %g"));
      root.addChild("chat_timeout", new TypeValidatorNode(Double.class, true, 0.1));
      root.addChild("max_auto_pages", new TypeValidatorNode(Integer.class, true, 10));
      root.addChild("pre_count", new TypeValidatorNode(Integer.class, true, 45));
      root.addChild("post_count", new TypeValidatorNode(Integer.class, true, 45));
      root.addChild("watson_prefix", new TypeValidatorNode(String.class, true, "w"));
      root.addChild("ss_player_directory", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("ss_player_suffix", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("ss_date_directory", new TypeValidatorNode(String.class, true, ""));
      root.addChild("reformat_query_results", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("recolour_query_results", new TypeValidatorNode(Boolean.class, true, true));
      root.addChild("time_ordered_deposits", new TypeValidatorNode(Boolean.class, true, false));
      root.addChild("vector_length", new TypeValidatorNode(Double.class, true, 4.0));

      for (Entry<String, ModifiedKeyBinding> entry : getKeyBindingsMap().entrySet())
      {
        root.addChild(entry.getKey(), new TypeValidatorNode(String.class, true, entry.getValue().toString()));
      }

      _validator.setRoot(root);
    }
  } // configureValidator

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   *
   * Sets default key bindings.
   */
  private Configuration()
  {
    // Can't add these by reflection because we need to guarantee ordering.
    _bindings.add(KEYBIND_INGAME);
    _bindings.add(KEYBIND_SCREENSHOT);
    _bindings.add(KEYBIND_TP_NEXT);
    _bindings.add(KEYBIND_TP_PREV);
    _bindings.add(KEYBIND_QUERY_BEFORE);
    _bindings.add(KEYBIND_QUERY_AFTER);
    _bindings.add(KEYBIND_CURSOR_NEXT);
    _bindings.add(KEYBIND_CURSOR_PREV);
    _bindings.add(KEYBIND_TP_CURSOR);
  } // ctor

  // --------------------------------------------------------------------------
  /**
   * Return a map from the lowercase name of each keybinding's value in the
   * configuration file to the corresponding ModifiedKeyBinding instance.
   *
   * The map entries are not guaranteed to be in any particular order.
   *
   * @return a map from the lowercase name of each keybinding's value in the
   *         configuration file to the corresponding ModifiedKeyBinding
   *         instance.
   */
  private static HashMap<String, ModifiedKeyBinding> getKeyBindingsMap()
  {
    HashMap<String, ModifiedKeyBinding> bindings = new HashMap<String, ModifiedKeyBinding>();
    for (Field field : Configuration.class.getDeclaredFields())
    {
      if (field.getName().startsWith("KEYBIND_"))
      {
        try
        {
          bindings.put(field.getName().toLowerCase(), (ModifiedKeyBinding) field.get(Configuration.instance));
        }
        catch (IllegalArgumentException ex)
        {
        }
        catch (IllegalAccessException ex)
        {
        }
      }
    }
    return bindings;
  } // getKeyBindingsMap

  // --------------------------------------------------------------------------
  /**
   * Minimum value of _regionInfoTimeoutSeconds.
   */
  protected static final double           MIN_REGION_INFO_TIMEOUT   = 1.0;

  // --------------------------------------------------------------------------
  /**
   * Name of the configuration file in .minecraft/mods/watson/.
   */
  protected static final String           CONFIG_FILE               = "configuration.yml";

  /**
   * Used to validate configuration file contents on loading.
   */
  protected SnakeValidator                _validator;

  /**
   * Overall control of Watson: /w config watson [on|off]
   */
  protected boolean                       _enabled                  = true;

  /**
   * If true, "/lb page" is executed to page through "/lb coords" results, up to
   * 3 pages.
   */
  protected boolean                       _autoPage                 = true;

  /**
   * The timeout between automatic calls to "/region info" in seconds.
   */
  protected double                        _regionInfoTimeoutSeconds = 5.0;

  /**
   * If true, the vector display is enabled.
   */
  protected boolean                       _vectorsShown             = true;

  /**
   * Background colour of text billboards (annotations etc) as an ARGB (alpha in
   * the most significant octet, blue in the least significant one).
   */
  protected int                           _billboardBackground      = 0xA8000000;

  /**
   * Background colour of text billboards (annotations etc) as an ARGB (alpha in
   * the most significant octet, blue in the least significant one).
   */
  protected int                           _billboardForeground      = 0x7FFFFFFF;

  /**
   * If true, group ores even in creative mode.
   *
   * I currently know of no way of distinguishing the server's gamemode from
   * that of the player. Therefore, it is best if this setting is left true so
   * that "/w ore" works for admins in creative mode.
   */
  protected boolean                       _groupingOresInCreative   = true;

  /**
   * The default teleport command. X and Z coordinates are formatted as doubles
   * to signify that 0.5 should be added to centre the player in the block.
   */
  protected String                        _teleportCommand          = "/tppos %g %d %g";

  /**
   * The minimum number of seconds that must elapse between programmatically
   * sent chat messages (usually commands to the server).
   */
  protected double                        _chatTimeoutSeconds       = 0.1;

  /**
   * The maximum number of pages of "/lb coords" results that will be
   * automatically stepped through by issuing "/lb page #" commands.
   */
  protected int                           _maxAutoPages             = 10;

  /**
   * The number of edits to fetch from LogBlock when "/w pre" is run.
   */
  protected int                           _preCount                 = 45;

  /**
   * The number of edits to fetch from LogBlock when "/w post" is run.
   */
  protected int                           _postCount                = 45;

  /**
   * The start of all Watson commands, without the slash.
   */
  protected String                        _watsonPrefix             = "w";

  /**
   * If true, a subdirectory named after the currently selected player is
   * created to hold screenshots of his edits.
   */
  protected boolean                       _ssPlayerDirectory        = true;

  /**
   * If true, the name of the currently selected player is appended to
   * screenshot files.
   */
  protected boolean                       _ssPlayerSuffix           = true;

  /**
   * A {@link SimpleDateFormat} specifying the name of the subdirectory to
   * create to store screenshots when there is no currently selected player, or
   * when isPlayerDirectory() is false.
   *
   * I'm assuming that the empty string is a valid format that won't throw.
   */
  protected SimpleDateFormat              _ssDateDirectory          = new SimpleDateFormat("");
  /**
   * Reformat query results in chat more compactly.
   */
  protected boolean                       _reformatQueryResults     = true;

  /**
   * Recolour query results in chat.
   */
  protected boolean                       _recolourQueryResults     = true;

  /**
   * If true, ore deposits should be assigned 1-based numeric labels strictly in
   * the order that they were mined. When false, ores are ordered in descending
   * order of their scarcity.
   */
  protected boolean                       _timeOrderedDeposits      = false;

  /**
   * The default minimum length of vectors for them to be visible.
   */
  protected float                         _vectorLength             = 4.0f;

  /**
   * All {@link ModifiedKeyBindings} in the order they should be listed in the
   * configuration panel.
   */
  protected ArrayList<ModifiedKeyBinding> _bindings                 = new ArrayList<ModifiedKeyBinding>();
} // class Configuration

