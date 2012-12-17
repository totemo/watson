package net.minecraft.src;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import watson.Configuration;
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
    return Controller.instance.getVersionNumber();
  }

  // --------------------------------------------------------------------------
  /**
   * And now for a game of "Guess the ModLoader API".
   */
  public String getPriorities()
  {
    return "required-before:mod_ClientCommands";
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
    Controller.createDirectories();
    Configuration.instance.load();
    Log.info("Loading Watson version " + Controller.instance.getVersion());

    // ModLoader currently misreports its version, so we can not be strict about
    // matching it. If it compiles, assume it's gonna work.
    if (!getVersion().equals(getModLoaderVersion()))
    {
      Log.warning(String.format(
        "mismatched ModLoader version: Watson: %s, ModLoader: %s",
        getVersion(), getModLoaderVersion()));
      // return;
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
   * @param partialticks the fractional tick time.
   * @param mc The Minecraft instance.
   * @return true to signify that we want to continue receiving callbacks
   *         (rather than be de-registered).
   */
  @Override
  public boolean onTickInGame(float partialticks, Minecraft mc)
  {
    if (mc.theWorld != _lastWorld || mc.thePlayer != _lastPlayer)
    {
      Log.debug("Setting up EntityWatson.");

      // If this is the first time entering a world/dimension entry in this
      // session, show the startup banner.
      if (_lastWorld == null && Configuration.instance.isEnabled())
      {
        String version = Controller.instance.getVersion();
        String format = (version.length() != 0 && !version.equals("unknown")) ? "Watson %s. Type /w help, for help."
          : "Watson. Type /w help, for help.";
        Controller.instance.localOutput(String.format(format, version));

        // Only set display settings on first connect. Subsequent connects
        // should retain the previous display state.
        Controller.instance.getDisplaySettings().configure(
          Controller.instance.getServerIP(),
          mc.theWorld.getWorldInfo().getGameType());
      }
      _lastWorld = mc.theWorld;
      _lastPlayer = mc.thePlayer;

      _watsonEntity = new EntityWatson(mc);
      mc.theWorld.spawnEntityInWorld(_watsonEntity);
    }

    ChatProcessor.getInstance().processChatQueue();
    Controller.instance.processServerChatQueue();
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
    final Pattern PATTERN = Pattern.compile("\\d+(\\.\\d+)+$");
    Matcher matcher = PATTERN.matcher(ModLoader.VERSION);
    return (matcher.find()) ? matcher.group() : "";
  }

  // --------------------------------------------------------------------------
  /**
   * The world at the time that the last Entity used to render the edits was
   * spawned.
   */
  protected World          _lastWorld;

  /**
   * The player at the time that the last Entity used to render the edits was
   * spawned.
   */
  protected EntityPlayerSP _lastPlayer;

  /**
   * An entity that depicts all of the edits currently under examination.
   */
  protected EntityWatson   _watsonEntity;

} // class mod_Watson
