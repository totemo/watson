package watson.chat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import net.minecraft.src.GuiNewChat;
import net.minecraft.src.mod_Watson;
import watson.BlockEdit;
import watson.BlockEditSet;
import watson.Controller;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * A Singleton that processes all incoming {@link net.minecraft.src.Packet3Chat}
 * chat packets and routes them through various Watson components and/or back
 * into the Minecraft GUI.
 * 
 * Normally, net.minecraft.src.NetClientHandler.handleChat() calls
 * GuiNewChat.printChatMessage() for every chat packet received. But Watson uses
 * lots of /lb coords queries that we would like to exclude from chat, so
 * instead, we categorise all incoming chats in this class and then only pass
 * some of them to the normal Minecraft handling.
 * 
 * Since a chat message tagged as "lb.coord" causes a new {@link BlockEdit} to
 * be added to the currently displayed {@link BlockEditSet}, if we happen to be
 * iterating that set to draw the blocks when the chat arrives, we will get a
 * ConcurrentModificationException, since the chat arrives asynchronously (the
 * network and game rendering threads are different). To avoid that, incoming
 * chats are simply added to a queue, and then later de-queued and processed
 * from the game thread.
 */
public class ChatProcessor
{
  /**
   * Return the single instance of this class.
   * 
   * Use lazy initialisation to delay construction to a time after class
   * Minecraft has constructed its {@link GuiNewChat} instance.
   */
  public static ChatProcessor getInstance()
  {
    if (_instance == null)
    {
      _instance = new ChatProcessor();
    }
    return _instance;
  }

  // --------------------------------------------------------------------------
  /**
   * Return a reference to the ChatClassifier.
   * 
   * @return a reference to the ChatClassifier.
   */
  public ChatClassifier getChatClassifier()
  {
    return _chatClassifier;
  }

  // --------------------------------------------------------------------------
  /**
   * Add the specified chat message to the queue.
   * 
   * This method is called by
   * {@link net.minecraft.src.NetClientHandler#handleChat()} and supplants the
   * normal handling of chat messages in Minecraft.
   * 
   * @param chat the chat message.
   */
  public void addChatToQueue(String chat)
  {
    _chatQueue.add(chat);
  }

  // --------------------------------------------------------------------------
  /**
   * Process each of the chat messages that was added to the queue by
   * addChatToQueue() in the order they arrived.
   */
  public void processChatQueue()
  {
    // clear the queue of chat messages that have arrived from the network
    // thread.
    for (;;)
    {
      String chat = _chatQueue.poll();
      if (chat == null)
      {
        break;
      }
      _chatClassifier.classify(chat);
    }
  } // processChatQueue

  // --------------------------------------------------------------------------
  /**
   * Load the chat categories from a YAML file in the mod's configuration
   * subdirectory, or from minecraft.jar as a fallback.
   */
  public void loadChatCategories()
  {
    try
    {
      InputStream in = mod_Watson.getConfigurationStream(CHAT_CATEGORIES_FILE);
      try
      {
        _chatClassifier.loadChatCategories(in);
      }
      finally
      {
        in.close();
      }
    }
    catch (IOException ex)
    {
      Log.exception(Level.SEVERE, "error loading chat categories: ", ex);
    }
  } // loadChatCategories

  // --------------------------------------------------------------------------
  /**
   * Load the chat exclusions from a YAML file in the mod's configuration
   * subdirectory, or from minecraft.jar as a fallback.
   */
  public void loadChatExclusions()
  {
    try
    {
      InputStream in = mod_Watson.getConfigurationStream(CHAT_EXCLUSIONS_FILE);
      try
      {
        _excludeTagChatHandler.loadExclusions(in);
      }
      finally
      {
        in.close();
      }
    }
    catch (IOException ex)
    {
      Log.exception(Level.SEVERE, "error loading chat exclusions: ", ex);
    }
  } // loadChatExclusions

  // --------------------------------------------------------------------------
  /**
   * Controls whether chat messages with the specifed tag are visible (displayed
   * locally in the client) or not.
   * 
   * @param tag the tag of the chat messages, assigned by the
   *          {@link ChatClassifier}.
   * @param visible if true, messages of that type are displayed.
   */
  public void setChatTagVisible(String tag, boolean visible)
  {
    _excludeTagChatHandler.setExcluded(tag, !visible);

    File exclusionsFile = new File(mod_Watson.getModDirectory(),
      CHAT_EXCLUSIONS_FILE);
    _excludeTagChatHandler.saveExclusions(exclusionsFile);

    Controller.instance.localChat((visible ? "Show " : "Hide ") + tag
                                  + " lines.");
  }

  // --------------------------------------------------------------------------
  /**
   * List the currently hidden chat tags.
   */
  public void listHiddenTags()
  {
    StringBuilder message = new StringBuilder();
    message.append("Hidden tags:");
    for (String tag : _excludeTagChatHandler.getExcludedTags())
    {
      message.append(' ');
      message.append(tag);
    }
    Controller.instance.localChat(message.toString());
  } // listHiddenTags

  // --------------------------------------------------------------------------
  /**
   * Private constructor to enforce single instance.
   */
  private ChatProcessor()
  {
    _excludeTagChatHandler.setDefaultHandler(new MinecraftChatHandler());
    _chatClassifier.addChatHandler(_excludeTagChatHandler);
  } // ChatProcessor

  // --------------------------------------------------------------------------
  /**
   * The basename of the file containing the YAML descriptions of all
   * ChatCategory instances.
   */
  private static final String             CHAT_CATEGORIES_FILE   = "chatcategories.yml";

  /**
   * The basename of the file containing the tags of all chat categories that
   * are exclused from display.
   */
  private static final String             CHAT_EXCLUSIONS_FILE   = "chatexclusions.yml";

  /**
   * Single instance of this class.
   */
  protected static ChatProcessor          _instance;

  /**
   * Classifies incoming chat text and dispatches to an IChatHandler.
   */
  protected ChatClassifier                _chatClassifier        = new ChatClassifier();

  /**
   * One of the {@link IChatHandler}s registered with _chatClasssifier. It
   * provides control over which chat messages get excluded from chat.
   */
  protected ExcludeTagChatHandler         _excludeTagChatHandler = new ExcludeTagChatHandler();

  /**
   * A queue to pass incoming chat messages from the network thread to the game
   * thread. This is necessary to avoid a ConcurrentModificationException while
   * traversing the set of BlockEdit instances and rendering them.
   */
  protected ConcurrentLinkedQueue<String> _chatQueue             = new ConcurrentLinkedQueue<String>();

} // class ChatProcessor
