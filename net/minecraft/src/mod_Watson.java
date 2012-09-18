package net.minecraft.src;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import watson.Controller;
import watson.EntityWatson;
import watson.RenderWatson;
import watson.chat.ChatProcessor;
import watson.debug.Log;

// --------------------------------------------------------------------------
/**
 * A ModLoader mod that displays LogBlock results as wireframe boxes.
 * 
 * TODO: Keep a map from (world,player) to EntityWatson instances.
 */
public class mod_Watson extends BaseMod
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public mod_Watson()
  {
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public String getVersion()
  {
    return VERSION;
  }

  // --------------------------------------------------------------------------
  /**
   * Do the following initialisation when the mod is loaded:
   * <ul>
   * <li>Check that the ModLoader version matches and abort if not.</li>
   * <li>Call setInGameHook() to register this mod for regular onTickInGame()
   * calls.</li>
   * </ul>
   */
  @Override
  public void load()
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

    Log.setDebug(true);
    Log.info("Loading Watson version " + VERSION);

    // Bail out if this mod's version doesn't match the ModLoader version.
    if (!getVersion().equals(getModLoaderVersion()))
    {
      Log.severe("mismatched ModLoader version: (" + getModLoaderVersion()
                 + ")");
      return;
    }

    Controller.instance.initialise();

    // The second true signifies that onTickInGame() should only be called
    // if the world clock has advanced.
    ModLoader.setInGameHook(this, true, true);
  } // load

  // --------------------------------------------------------------------------
  /**
   * Register WatsonRenderer as the renderer for WatsonEntity instances.
   */
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void addRenderer(Map entityClassToRenderMap)
  {
    entityClassToRenderMap.put(EntityWatson.class, new RenderWatson());
  }

  // --------------------------------------------------------------------------
  /**
   * Checks if the world or player has changed from the last time we checked. If
   * it's changed, spawn a new render entity and update accordingly.
   * 
   * All removes any chat messages in _chatQueue and passes them to the
   * _chatClassifier, where they will end up being handled by Sherlock and may
   * ultimately be rendered.
   * 
   * @param partialticks
   * @param mc
   * @return true to signify that we want to continue receiving callbacks
   *         (rather than be de-registered).
   */
  @Override
  public boolean onTickInGame(float partialticks, Minecraft mc)
  {
    if (mc.theWorld != _lastWorld || mc.thePlayer != _lastPlayer)
    {
      Log.debug("setting up EntityWatson");
      _lastWorld = mc.theWorld;
      _lastPlayer = mc.thePlayer;

      _watsonEntity = new EntityWatson(mc);
      mc.theWorld.spawnEntityInWorld(_watsonEntity);
    }

    ChatProcessor.getInstance().processChatQueue();
    return true;
  } // onTickInGame

  // --------------------------------------------------------------------------
  /**
   * Return the numeric portion of the ModLoader version.
   * 
   * The current (as of 1.3.1) form of the ModLoader version string is
   * "ModLoader 1.3.1", whereas this method just returns "1.3.1".
   * 
   * @return the numeric portion of the ModLoader version, or the empty string
   *         if there is none.
   */
  public String getModLoaderVersion()
  {
    final Pattern PATTERN = Pattern.compile("\\d(\\.\\d)+$");
    Matcher matcher = PATTERN.matcher(ModLoader.VERSION);
    return (matcher.find()) ? matcher.group() : "";
  }

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
    File file = new File(getModDirectory(), fileName);
    if (file.canRead())
    {
      Log.info("Loading \"" + fileName + "\" from file.");
      return new BufferedInputStream(new FileInputStream(file));
    }
    else
    {
      Log.info("Loading \"" + fileName + "\" from resource in minecraft.jar.");
      ClassLoader loader = mod_Watson.class.getClassLoader();
      return loader.getResourceAsStream(MOD_PACKAGE + '/' + fileName);
    }
  } // getConfigurationStream

  // --------------------------------------------------------------------------
  /**
   * Version string; should match Minecraft version or bail out.
   */
  private static final String VERSION     = "1.3.2";

  /**
   * The main package name of the classes of this mod, and also the name of the
   * subdirectory of .minecraft/mods/ where mod-specific settings are stored.
   */
  private static final String MOD_PACKAGE = "watson";

  /**
   * Directory where mod files reside, relative to the .minecraft/ directory.
   */
  private static final String MOD_SUBDIR  = "mods" + File.separator
                                            + MOD_PACKAGE;

  // --------------------------------------------------------------------------
  /**
   * The world at the time that the last Entity used to render the edits was
   * spawned.
   */
  protected World             _lastWorld;

  /**
   * The player at the time that the last Entity used to render the edits was
   * spawned.
   */
  protected EntityPlayerSP    _lastPlayer;

  /**
   * An entity that depicts all of the edits currently under examination.
   */
  protected EntityWatson      _watsonEntity;

} // class mod_Watson
