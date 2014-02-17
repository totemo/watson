package watson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import watson.chat.Chat;
import watson.chat.ChatProcessor;
import watson.cli.ClientCommandManager;
import watson.db.BlockEditSet;
import watson.debug.Log;

import com.google.gson.Gson;
import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.PostRenderListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

// ----------------------------------------------------------------------------
/**
 * Main LiteMod entry point and event handler.
 * 
 * @author totemo
 */
@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "examplemod.json")
public class LiteModWatson implements JoinGameListener, ChatFilter, Tickable, PostRenderListener
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public LiteModWatson()
  {
  }

  // --------------------------------------------------------------------------
  /**
   * getName() should be used to return the display name of your mod and MUST
   * NOT return null
   * 
   * @see com.mumfrey.liteloader.LiteMod#getName()
   */
  @Override
  public String getName()
  {
    return "Watson";
  }

  // --------------------------------------------------------------------------
  /**
   * Read the mod version from the metadata.
   * 
   * Also works in the Eclipse run configuration if the Ant build runs at least
   * once to update res/litemod.json from build/litemod.template.json.
   * 
   * @see com.mumfrey.liteloader.LiteMod#getVersion()
   */
  @Override
  public String getVersion()
  {
    InputStream is = null;
    try
    {
      Gson gson = new Gson();
      is = getLiteModJsonStream();
      @SuppressWarnings("unchecked")
      Map<String, String> meta = gson.fromJson(new InputStreamReader(is),
        HashMap.class);
      String version = meta.get("version");
      if (version == null)
      {
        version = "(missing version info)";
      }
      return version;
    }
    catch (Exception ex)
    {
      return "(error loading version)";
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (Exception ex)
        {
        }
      }
    }
  } // getVersion

  // --------------------------------------------------------------------------
  /**
   * init() is called very early in the initialisation cycle, before the game is
   * fully initialised, this means that it is important that your mod does not
   * interact with the game in any way at this point.
   * 
   * @see com.mumfrey.liteloader.LiteMod#init(java.io.File)
   */
  @Override
  public void init(File configPath)
  {
    Configuration.instance.load();
    Log.info("Loading Watson version " + getVersion());
    Controller.instance.initialise();
    LiteLoader.getInput().registerKeyBinding(_screenShotKeyBinding);
  }

  // --------------------------------------------------------------------------
  /**
   * upgradeSettings is used to notify a mod that its version-specific settings
   * are being migrated
   * 
   * @see com.mumfrey.liteloader.LiteMod#upgradeSettings(java.lang.String,
   *      java.io.File, java.io.File)
   */
  @Override
  public void upgradeSettings(String version, File configPath, File oldConfigPath)
  {
  }

  // --------------------------------------------------------------------------
  /**
   * Perform actions triggered on initial join.
   * 
   * @see com.mumfrey.liteloader.JoinGameListener#onJoinGame(net.minecraft.network.INetHandler,
   *      net.minecraft.network.play.server.S01PacketJoinGame)
   */
  @Override
  public void onJoinGame(INetHandler netHandler, S01PacketJoinGame joinGamePacket)
  {
    if (Configuration.instance.isEnabled())
    {
      _gameJoinTime = System.currentTimeMillis();

      // Only set display settings on first connect. Subsequent connects
      // should retain the previous display state.
      Minecraft mc = Minecraft.getMinecraft();
      Controller.instance.getDisplaySettings().configure(
        Controller.instance.getServerIP(),
        mc.theWorld.getWorldInfo().getGameType());
    }
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.ChatFilter#onChat(net.minecraft.network.play.server.S02PacketChat,
   *      net.minecraft.util.IChatComponent, java.lang.String)
   */
  @Override
  public boolean onChat(S02PacketChat chatPacket, IChatComponent chat, String message)
  {
    // TODO: reformulate chat processing using IChatComponent; handle URLs etc.
    ChatProcessor.instance.addChatToQueue(chat);
    return false;
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.Tickable#onTick(net.minecraft.client.Minecraft,
   *      float, boolean, boolean)
   */
  @Override
  public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
  {
    ChatProcessor.instance.processChatQueue();
    Controller.instance.processServerChatQueue();

    // With Forge, onJoinGame() gets called before the chat GUI is ready to
    // display the welcome message.
    if (_gameJoinTime != 0 &&
        System.currentTimeMillis() - _gameJoinTime > 1000 &&
        Chat.isChatGuiReady())
    {
      Chat.localOutput(String.format(Locale.US, "Watson %s. Type /w help, for help.", getVersion()));
      _gameJoinTime = 0;
    }

    if (_screenShotKeyBinding.isPressed())
    {
      if (!_takingScreenshot)
      {
        _takingScreenshot = true;

        Date now = new Date();
        Configuration config = Configuration.instance;
        String player = (String) Controller.instance.getVariables().get("player");
        String subdirectoryName = (player != null && config.isSsPlayerDirectory())
          ? player
          : config.getSsDateDirectory().format(now).toString();
        Minecraft mc = Minecraft.getMinecraft();
        File screenshotsDir = new File(mc.mcDataDir, "screenshots");
        File subdirectory = new File(screenshotsDir, subdirectoryName);
        File file = Screenshot.getUniqueFilename(subdirectory, player, now);
        Chat.localChat(Screenshot.save(file, mc.displayWidth, mc.displayHeight));
      }
    }
    else
    {
      _takingScreenshot = false;
    }
  } // onTick

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.PostRenderListener#onPostRenderEntities(float)
   */
  @Override
  public void onPostRenderEntities(float partialTicks)
  {
    if (Configuration.instance.isEnabled()
        && Controller.instance.getDisplaySettings().isDisplayed())
    {
      RenderHelper.disableStandardItemLighting();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glDisable(GL11.GL_TEXTURE_2D);
      GL11.glDisable(GL11.GL_LIGHTING);
      GL11.glDepthMask(false);
      GL11.glDisable(GL11.GL_DEPTH_TEST);

      boolean foggy = GL11.glIsEnabled(GL11.GL_FOG);
      GL11.glDisable(GL11.GL_FOG);

      GL11.glPushMatrix();

      GL11.glTranslated(
        -getPlayerX(partialTicks),
        -getPlayerY(partialTicks),
        -getPlayerZ(partialTicks));

      BlockEditSet edits = Controller.instance.getBlockEditSet();
      edits.drawOutlines();
      edits.drawVectors();

      // Test code. X marks the spot.
      // GL11.glLineWidth(3.0f);
      // GL11.glColor4f(1.0f, 0.0f, 0.0f, 0.8f);
      // Tessellator tess = Tessellator.instance;
      // tess.startDrawing(GL11.GL_LINES);
      // tess.addVertex(-5, 27, -5);
      // tess.addVertex(5, 27, 5);
      // tess.addVertex(5, 27, -5);
      // tess.addVertex(-5, 27, 5);
      // tess.draw();

      GL11.glPopMatrix();

      edits.drawAnnotations();
      edits.getOreDB().drawDepositLabels();

      // More test code.
      // drawBillboard(0, 70, 0, 0x80000000, 0xFFFFFFFF, 0.02,
      // "Test Billboard");

      // Only re-enable fog if it was enabled before we messed with it.
      // Or else, fog is *all* you'll see with Optifine.
      if (foggy)
      {
        GL11.glEnable(GL11.GL_FOG);
      }
      GL11.glEnable(GL11.GL_DEPTH_TEST);
      GL11.glDepthMask(true);
      GL11.glEnable(GL11.GL_LIGHTING);
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glDisable(GL11.GL_BLEND);

      RenderHelper.enableStandardItemLighting();
    }
  } // onPostRenderEntities

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.PostRenderListener#onPostRender(float)
   */
  @Override
  public void onPostRender(float partialTicks)
  {
  }

  // --------------------------------------------------------------------------

  private double getPlayerX(float partialTicks)
  {
    EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
    return p.prevPosX + (p.posX - p.prevPosX) * partialTicks;
  }

  // --------------------------------------------------------------------------

  private double getPlayerY(float partialTicks)
  {
    EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
    return p.prevPosY + (p.posY - p.prevPosY) * partialTicks;
  }

  // --------------------------------------------------------------------------

  private double getPlayerZ(float partialTicks)
  {
    EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
    return p.prevPosZ + (p.posZ - p.prevPosZ) * partialTicks;
  }

  // --------------------------------------------------------------------------
  /**
   * Callback redirected from EntityClientPlayerMP.sendChatMessage(String).
   * 
   * This is used to intercept player chats heading for the server and filter
   * out (and act upon) Watson commands.
   */
  public static void sendChatMessage(EntityClientPlayerMP player, String chat)
  {
    if (!ClientCommandManager.instance.handleClientCommand(chat))
    {
      player.sendQueue.addToSendQueue(new C01PacketChatMessage(chat));
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return an InputStream that reads "/litemod.json".
   * 
   * When running under the IDE, that's easy because the file is copied to the
   * res/ directory and getResourceAsStream() can access it directly. When
   * running as an installed mod file, getResourceAsStream() may return a
   * reference to the litemod.json file for another mod, depending on the order
   * of the mods in the classloader. In that circumstance, we use a specially
   * crafted URL that references litemod.json via the URL of the .litemod (JAR)
   * file.
   * 
   * @return the InputStream, or null on failure.
   */
  private InputStream getLiteModJsonStream()
  {
    String classURL = getClass().getResource("/" + getClass().getName().replace('.', '/') + ".class").toString();
    if (classURL.contains("!"))
    {
      String jarURL = classURL.substring(0, classURL.indexOf('!'));
      try
      {
        URL resourceURL = new URL(jarURL + "!/litemod.json");
        return resourceURL.openStream();
      }
      catch (IOException ex)
      {
      }
      return null;
    }
    else
    {
      // No JAR. Running under the IDE.
      return getClass().getResourceAsStream("/litemod.json");
    }
  } // getLiteModJsonStream

  // --------------------------------------------------------------------------
  /**
   * This is a keybinding that we will register with the game and use to toggle
   * the clock
   */
  private static KeyBinding _screenShotKeyBinding = new KeyBinding("Take Screenshot", Keyboard.KEY_F12,
                                                                   "Watson");

  /**
   * True while the player holds down the Watson screenshot key. Used to detect
   * the initial key-down.
   */
  private static boolean    _takingScreenshot     = false;

  /**
   * Set, upon joining the game, to the current time to trigger the welcome
   * message a second later. When 0, no welcome message is shown.
   */
  private static long       _gameJoinTime         = 0;
} // class LiteModWatson
