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
      setDebug((Boolean) dom.get("debug"));
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

      _validator.setRoot(root);
    }
  } // configureValidator

  // --------------------------------------------------------------------------
  /**
   * Name of the configuration file in .minecraft/mods/watson/.
   */
  protected static final String CONFIG_FILE = "configuration.yml";

  /**
   * Used to validate configuration file contents on loading.
   */
  protected SnakeValidator      _validator;

  /**
   * Overall control of Watson: /w config watson [on|off]
   */
  protected boolean             _enabled    = true;
} // class Configuration
