package watson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import watson.chat.Chat;
import watson.chat.ChatProcessor;
import watson.cli.ClientCommandManager;
import watson.db.BlockEditSet;
import watson.debug.Log;
import watson.gui.ModifiedKeyBinding;
import watson.gui.MouseButton;
import watson.gui.WatsonConfigPanel;

import com.google.gson.Gson;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.OutboundChatFilter;
import com.mumfrey.liteloader.PostRenderListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoaderEventBroker;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import com.mumfrey.liteloader.transformers.event.EventInfo;

// ----------------------------------------------------------------------------
/**
 * Main LiteMod entry point and event handler.
 *
 * @author totemo
 */
@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "watson.json")
public class LiteModWatson implements JoinGameListener, ChatFilter, Tickable,
  PostRenderListener, OutboundChatFilter, Configurable
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
   *      net.minecraft.network.play.server.S01PacketJoinGame,
   *      net.minecraft.client.multiplayer.ServerData,
   *      com.mojang.realmsclient.dto.RealmsServer)
   */
  @Override
  public void onJoinGame(INetHandler netHandler, S01PacketJoinGame joinGamePacket, ServerData serverData,
                         RealmsServer realmsServer)
  {
    if (Configuration.instance.isEnabled())
    {
      _gameJoinTime = System.currentTimeMillis();

      // Only set display settings on first connect. Subsequent connects
      // should retain the previous display state.
      Minecraft mc = Minecraft.getMinecraft();
      Controller.instance.getDisplaySettings().configure(Controller.instance.getServerIP(),
                                                         mc.theWorld.getWorldInfo().getGameType());
    }
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.ChatFilter#onChat(net.minecraft.util.IChatComponent,
   *      java.lang.String,
   *      com.mumfrey.liteloader.core.LiteLoaderEventBroker.ReturnValue)
   */
  @Override
  public boolean onChat(IChatComponent chat, String message, LiteLoaderEventBroker.ReturnValue<IChatComponent> newMessage)
  {
    boolean allowChat = ChatProcessor.instance.onChat(chat);
    if (allowChat)
    {
      newMessage.set(Chat.getChatHighlighter().highlight(chat));
    }
    return allowChat;
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.Tickable#onTick(net.minecraft.client.Minecraft,
   *      float, boolean, boolean)
   */
  @Override
  public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
  {
    fixStuckKeys();

    // Check for the screenshot key, even if there is a GuiScreen up that would
    // ordinarily swall the key events. We need to screenie chat.
    if (Configuration.instance.KEYBIND_SCREENSHOT.isHeld())
    {
      Configuration.instance.KEYBIND_SCREENSHOT.perform();
    }

    SyncTaskQueue.instance.runTasks();
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

      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.depthMask(false);
      GlStateManager.disableDepth();

      boolean foggy = GL11.glIsEnabled(GL11.GL_FOG);
      GlStateManager.disableFog();

      GlStateManager.pushMatrix();

      GlStateManager.translate(
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

      GlStateManager.popMatrix();

      edits.drawAnnotations();
      edits.getOreDB().drawDepositLabels();

      // More test code.
      // drawBillboard(0, 70, 0, 0x80000000, 0xFFFFFFFF, 0.02,
      // "Test Billboard");

      // Only re-enable fog if it was enabled before we messed with it.
      // Or else, fog is *all* you'll see with Optifine.
      if (foggy)
      {
        GlStateManager.enableFog();
      }
      GlStateManager.enableDepth();
      GlStateManager.depthMask(true);
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();

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
    EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
    return p.prevPosX + (p.posX - p.prevPosX) * partialTicks;
  }

  // --------------------------------------------------------------------------

  private double getPlayerY(float partialTicks)
  {
    EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
    return p.prevPosY + (p.posY - p.prevPosY) * partialTicks;
  }

  // --------------------------------------------------------------------------

  private double getPlayerZ(float partialTicks)
  {
    EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
    return p.prevPosZ + (p.posZ - p.prevPosZ) * partialTicks;
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.OutboundChatFilter#onSendChatMessage(java.lang.String)
   */
  @Override
  public boolean onSendChatMessage(String chat)
  {
    // Send the chat to server if not handled locally as a command.
    return !ClientCommandManager.instance.handleClientCommand(chat);
  }

  // --------------------------------------------------------------------------
  /**
   * This method acts as an entry point into
   * ClientCommandManager.handleClientCommand(), for use by the watson_macros
   * mod.
   *
   * The method was originally a callback used by a transformer, but that is no
   * longer the case.
   */
  public static void sendChatMessage(EntityPlayerSP player, String chat)
  {
    if (!ClientCommandManager.instance.handleClientCommand(chat))
    {
      player.sendQueue.addToSendQueue(new C01PacketChatMessage(chat));
    }
  }

  // --------------------------------------------------------------------------
  /**
   * @see com.mumfrey.liteloader.Configurable#getConfigPanelClass()
   */
  @Override
  public Class<? extends ConfigPanel> getConfigPanelClass()
  {
    return WatsonConfigPanel.class;
  }

  // --------------------------------------------------------------------------
  /**
   * Return an InputStream that reads "/litemod.json".
   *
   * When running under the IDE, that's easy because the file is copied to the
   * res/ directory and getResourceAsStream() can access it directly. When
   * running as an installed mod file, getResourceAsStream() may return a
   * reference to the litemod.json file for another mod, depending on the order
   * of the mods in the ClassLoader. In that circumstance, we use a specially
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
   * This EventInjectionTransformer listener for KeyBinding.onTick() intercepts
   * key events and mouse clicks and checks for ModifiedKeyBinding activations.
   */
  public static void onKeyBindingOnTick(EventInfo<?> event, int keyCode)
  {
    ModifiedKeyBinding binding = getActivatedKeyBinding(keyCode);
    if (binding != null)
    {
      // If Watson should handle the key, then cancel Minecraft's normal
      // handling of that key (which may be bound).
      event.cancel();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * This EventInjectionTransformer listener for KeyBinding.setKeyBindState()
   * intercepts key events and mouse clicks and checks for ModifiedKeyBinding
   * activations.
   */
  public static void onKeyBindingSetKeyBindState(EventInfo<?> event, int
                                                 keyCode, boolean down)
  {
    ModifiedKeyBinding binding = getActivatedKeyBinding(keyCode);
    if (down && binding != null)
    {
      // If Watson should handle the key, then cancel Minecraft's normal
      // handling of that key (which may be bound).
      performKeyBinding(binding);
      event.cancel();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * This EventInjectionTransformer listener for
   * InventoryPlayer.changeCurrentItem() intercepts mouse scrolls events and
   * checks for ModifiedKeyBinding activations.
   */
  public static void onInventoryPlayerChangeCurrentItem(EventInfo<InventoryPlayer> event, int wheelDelta)
  {
    int keyCode = (wheelDelta < 0) ? MouseButton.SCROLL_DOWN.getCode()
      : (wheelDelta > 0) ? keyCode = MouseButton.SCROLL_UP.getCode() : 0;
    if (keyCode != 0)
    {
      ModifiedKeyBinding binding = getActivatedKeyBinding(keyCode);
      if (binding != null)
      {
        performKeyBinding(binding);
        event.cancel();
      }
    }
  } // onInventoryPlayerChangeCurrentItem

  // --------------------------------------------------------------------------
  /**
   * Play a button press sound and call the {@link ActionHandler} for a
   * {@link ModifiedKeyBinding}.
   */
  protected static void performKeyBinding(ModifiedKeyBinding binding)
  {
    Minecraft mc = Minecraft.getMinecraft();
    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    binding.perform();
  }

  // --------------------------------------------------------------------------
  /**
   * If a key binding is activated, return it.
   *
   * If the Watson display is disabled, key bindings that require the display to
   * be visible will not be considered activated.
   *
   * @param keyCode the most recently pressed key.
   * @return the activated key binding, or null if none is active.
   */
  protected static ModifiedKeyBinding getActivatedKeyBinding(int keyCode)
  {
    ModifiedKeyBinding matchingKeyBinding = null;
    for (ModifiedKeyBinding keyBinding : Configuration.instance.getAllModifiedKeyBindings())
    {
      // SKip over the screenshot keybinding, since we check it in onTick() so
      // that we can take screenshots even with the chat GUI up.
      if (keyBinding == Configuration.instance.KEYBIND_SCREENSHOT)
      {
        continue;
      }

      if (keyBinding.isActivated(keyCode))
      {
        matchingKeyBinding = keyBinding;
        break;
      }
    }

    // Don't match display-dependent key bindings when the display is disabled.
    if (matchingKeyBinding != null && matchingKeyBinding.isDisplayDependent() &&
        !Controller.instance.getDisplaySettings().isDisplayed())
    {
      matchingKeyBinding = null;
    }
    return matchingKeyBinding;
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called on every tick to check whether Minecraft has lost
   * keyboard focus.
   *
   * When the user switches away from Minecraft, e.g. using Alt-Tab, LWJGL never
   * receives the key up event on those keys and the LWJGL Keyboard class
   * considers them to be stuck down.
   *
   * The fix, found at http://forum.lwjgl.org/index.php?topic=4517.0 is to
   * destroy() and re-create() the Keyboard. Minecraft KeyBindings must also be
   * un-pressed.
   */
  protected void fixStuckKeys()
  {
    if (Display.isActive())
    {
      // When focus is regained...
      if (_focusWasLost)
      {
        // Reset Minecraft KeyBindings and re-create the Keyboard from scratch.
        KeyBinding.unPressAllKeys();
        Keyboard.destroy();
        try
        {
          Keyboard.create();
        }
        catch (LWJGLException ex)
        {
          Log.exception(Level.SEVERE, "Exception fixing stuck keys", ex);
        }
      }
      _focusWasLost = false;
    }
    else
    {
      _focusWasLost = true;
    }
  } // fixStuckKeys

  // --------------------------------------------------------------------------
  /**
   * Set, upon joining the game, to the current time to trigger the welcome
   * message a second later. When 0, no welcome message is shown.
   */
  protected static long _gameJoinTime = 0;

  /**
   * This flag is set to true to record the event of Minecraft having lost
   * keyboard focus.
   */
  protected boolean     _focusWasLost = false;
} // class LiteModWatson
