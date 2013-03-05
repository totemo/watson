package watson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import watson.debug.Log;
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
  public static final Configuration instance = new Configuration();

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
    }
    catch (FileNotFoundException ex)
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
      Controller.instance.localOutput("Watson is now enabled.");
    }
    else
    {
      Controller.instance.localOutput("Watson is now disabled.");
      Controller.instance.localOutput("To re-enable, use: /w config watson on");
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
    Controller.instance.localOutput("Debug level logging "
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
    Controller.instance.localOutput("Automatic paging "
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput((_groupingOresInCreative ? "Enabled"
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput(String.format(
      Locale.US,
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput(String.format(Locale.US,
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
    Controller.instance.localOutput(String.format(Locale.US,
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
      root.addChild("auto_page", new TypeValidatorNode(Boolean.class, true,
        true));
      root.addChild("region_info_timeout", new TypeValidatorNode(Double.class,
        true, 5.0));
      root.addChild("vectors_shown", new TypeValidatorNode(Boolean.class, true,
        true));
      root.addChild("billboard_background", new TypeValidatorNode(
        Integer.class, true, 0x7F000000));
      root.addChild("billboard_foreground", new TypeValidatorNode(
        Integer.class, true, 0xFFFFFFFF));

      // Default to true until we can distinguish server vs player gamemode.
      root.addChild("group_ores_in_creative", new TypeValidatorNode(
        Boolean.class, true, true));

      root.addChild("teleport_command", new TypeValidatorNode(String.class,
        true, "/tppos %g %d %g"));
      root.addChild("chat_timeout", new TypeValidatorNode(Double.class, true,
        1.1));
      root.addChild("max_auto_pages", new TypeValidatorNode(Integer.class,
        true, 3));
      root.addChild("pre_count", new TypeValidatorNode(Integer.class, true, 45));
      root.addChild("post_count",
        new TypeValidatorNode(Integer.class, true, 45));
      root.addChild("watson_prefix", new TypeValidatorNode(String.class, true,
        "w"));

      _validator.setRoot(root);
    }
  } // configureValidator

  // --------------------------------------------------------------------------
  /**
   * Minimum value of _regionInfoTimeoutSeconds.
   */
  protected static final double MIN_REGION_INFO_TIMEOUT   = 1.0;

  // --------------------------------------------------------------------------
  /**
   * Name of the configuration file in .minecraft/mods/watson/.
   */
  protected static final String CONFIG_FILE               = "configuration.yml";

  /**
   * Used to validate configuration file contents on loading.
   */
  protected SnakeValidator      _validator;

  /**
   * Overall control of Watson: /w config watson [on|off]
   */
  protected boolean             _enabled                  = true;

  /**
   * If true, "/lb page" is executed to page through "/lb coords" results, up to
   * 3 pages.
   */
  protected boolean             _autoPage                 = true;

  /**
   * The timeout between automatic calls to "/region info" in seconds.
   */
  protected double              _regionInfoTimeoutSeconds = 5.0;

  /**
   * If true, the vector display is enabled.
   */
  protected boolean             _vectorsShown             = true;

  /**
   * Background colour of text billboards (annotations etc) as an ARGB (alpha in
   * the most significant octet, blue in the least significant one).
   */
  protected int                 _billboardBackground      = 0xA8000000;

  /**
   * Background colour of text billboards (annotations etc) as an ARGB (alpha in
   * the most significant octet, blue in the least significant one).
   */
  protected int                 _billboardForeground      = 0x7FFFFFFF;

  /**
   * If true, group ores even in creative mode.
   * 
   * I currently know of no way of distinguishing the server's gamemode from
   * that of the player. Therefore, it is best if this setting is left true so
   * that "/w ore" works for admins in creative mode.
   */
  protected boolean             _groupingOresInCreative   = true;

  /**
   * The default teleport command. X and Z coordinates are formatted as doubles
   * to signify that 0.5 should be added to centre the player in the block.
   */
  protected String              _teleportCommand          = "/tppos %g %d %g";

  /**
   * The minimum number of seconds that must elapse between programmatically
   * sent chat messages (usually commands to the server).
   */
  protected double              _chatTimeoutSeconds       = 1.1;

  /**
   * The maximum number of pages of "/lb coords" results that will be
   * automatically stepped through by issuing "/lb page #" commands.
   */
  protected int                 _maxAutoPages             = 3;

  /**
   * The number of edits to fetch from LogBlock when "/w pre" is run.
   */
  protected int                 _preCount                 = 45;

  /**
   * The number of edits to fetch from LogBlock when "/w post" is run.
   */
  protected int                 _postCount                = 45;

  /**
   * The start of all Watson commands, without the slash.
   */
  protected String              _watsonPrefix             = "w";
} // class Configuration

