package watson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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
    Controller.instance.localOutput(String.format(
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
        false));
      root.addChild("region_info_timeout", new TypeValidatorNode(Double.class,
        true, 5.0));

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
  protected boolean             _autoPage                 = false;

  /**
   * The timeout between automatic calls to "/region info" in seconds.
   */
  protected double              _regionInfoTimeoutSeconds = 5.0;
} // class Configuration

