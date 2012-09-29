package watson;

import java.util.Calendar;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.GuiNewChat;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.mod_ClientCommands;
import watson.analysis.Sherlock;
import watson.chat.ChatProcessor;
import watson.cli.HighlightCommand;
import watson.cli.TagCommand;
import watson.cli.WatsonCommand;
import watson.debug.Log;

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
   * Mod-wide initialisation tasks:
   * <ul>
   * <li>loading chat categories</li>
   * <li>initialising Sherlock</li>
   * <li>loading block types</li>
   * </ul>
   */
  public void initialise()
  {
    ChatProcessor.getInstance().loadChatCategories();
    ChatProcessor.getInstance().loadChatExclusions();
    _sherlock = new Sherlock(ChatProcessor.getInstance().getChatClassifier());
    BlockTypeRegistry.instance.loadBlockTypes();
    _chatHighlighter.loadHighlights();

    // Initialise the commands.
    mod_ClientCommands.getInstance().registerCommand(new WatsonCommand());
    mod_ClientCommands.getInstance().registerCommand(new TagCommand());
    mod_ClientCommands.getInstance().registerCommand(new HighlightCommand());
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
    if (!mc.isSingleplayer())
    {
      idBuilder.append(mc.getServerData().serverIP);
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
   * Clear the BlockEditSet for the current server and dimension.
   * 
   * Also clear the variables scraped from chat.
   */
  public void clearBlockEditSet()
  {
    getBlockEditSet().clear();
    _variables.clear();
    localChat("Watson edits cleared.");
  }

  // --------------------------------------------------------------------------
  /**
   * Issue a LogBlock query that selects the edits that came immediately before
   * the most recent coalblock, /lb query result, or /lb tp destination. The
   * query takes the form:
   * 
   * <pre>
   * /lb before MM-DD hh:mm:ss player name coords limit 45
   * </pre>
   * 
   * This method is called in response to the "/w pre" command.
   */
  public void queryPreviousEdits()
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

      String query = String.format(
        "/lb before %d.%d.%d %02d:%02d:%02d player %s coords limit 45", day,
        month, year, hour, minute, second, player);
      Log.debug(query);
      serverChat(query);
    }
  } // queryPreviousEdits

  // --------------------------------------------------------------------------
  /**
   * Turn on or off all Watson displays.
   * 
   * @param displayed true if Watson draws stuff; false otherwise.
   */
  public void setDisplayed(boolean displayed)
  {
    _displayed = displayed;
    localChat("Watson display " + (displayed ? "enabled." : "disabled."));
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if Watson draws stuff; false otherwise.
   * 
   * @return true if Watson draws stuff; false otherwise.
   */
  public boolean isDisplayed()
  {
    return _displayed;
  }

  // --------------------------------------------------------------------------
  /**
   * Turn on or off the wireframe block outline display.
   * 
   * @param outlineShown if true, block outlines are drawn.
   */
  public void setOutlineShown(boolean outlineShown)
  {
    _outlineShown = outlineShown;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if block outines should be drawn.
   * 
   * This method takes into account the last calls to both setOutlineShown() and
   * setDisplayed(). It will return false if outlines are disabled or if the
   * overall Watson display is turned off.
   * 
   * @return true if block outines should be drawn.
   */
  public boolean isOutlineShown()
  {
    return _displayed && _outlineShown;
  }

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
   * Display the specified chat message in the local client's chat GUI.
   * 
   * @param message the chat message to display.
   */
  public void localChat(String message)
  {
    System.out.println(message);
    if (getChatGui() != null)
    {
      getChatGui().printChatMessage(_chatHighlighter.highlight(message));
    }
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
      getChatGui().printChatMessage("§4" + message);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Send the specified message in a chat packet to the server.
   * 
   * @param message the chat message to send.
   */
  public void serverChat(String message)
  {
    Packet3Chat chat = new Packet3Chat(message);
    ModLoader.clientSendPacket(chat);
  }

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
   * Makes inferences based on LogBlock query results.
   */
  protected Sherlock                      _sherlock;

  /**
   * A map from the a String containing the server address and dimension number
   * to the corresponding set of {@link BlockEdit}s that are displayed by
   * {@link RenderWatson}.
   */
  protected HashMap<String, BlockEditSet> _edits           = new HashMap<String, BlockEditSet>();

  /**
   * True if wireframe block outlines should be drawn.
   */
  protected boolean                       _outlineShown    = true;

  /**
   * True if all Watson displays can be drawn. Other flags disable individual
   * displays.
   */
  protected boolean                       _displayed       = true;

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
  Calendar                                _calendar        = Calendar.getInstance();
} // class Controller
