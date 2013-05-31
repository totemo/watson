package watson.chat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * Classifies incoming chat lines by matching them against regular expressions
 * and dispatches them to handlers.
 * 
 * Functionality to rejoin chat lines split at the server has been removed on
 * the basis that it can break every time a new server message is added, if the
 * message doesn't match any of the existing regular expressions.
 */
public class ChatClassifier
{
  // --------------------------------------------------------------------------
  /**
   * The special character that introduces Minecraft colour sequences.
   */
  public static final char COLOUR_CHAR    = '\247';

  /**
   * The default colour of chat text, expressed as the second character of a
   * colour escape sequence. ('f' == white)
   */
  public static final char DEFAULT_COLOUR = 'f';

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public ChatClassifier()
  {
  }

  // --------------------------------------------------------------------------
  /**
   * Load {@link ChatCategory} instances from the specified YAML file.
   * 
   * @throws IOException
   */
  public void loadChatCategories(File file)
    throws IOException
  {
    InputStream in = new BufferedInputStream(new FileInputStream(file));
    try
    {
      loadChatCategories(in);
    }
    finally
    {
      in.close();
    }
  } // loadCategories

  // --------------------------------------------------------------------------
  /**
   * Load {@link ChatCategory} instances from the specified YAML-formatted
   * InputStream
   * 
   * @throws IOException
   */
  public void loadChatCategories(InputStream in)
    throws IOException
  {
    ChatCategoryListBean categories = ChatCategoryListBean.load(in);
    for (ChatCategoryBean bean : categories.getCategories())
    {
      addChatCategory(bean.makeChatCategory());
    }
  } // loadCategories

  // --------------------------------------------------------------------------
  /**
   * Add the specified ChatCategory to the classification scheme.
   * 
   * @param category the ChatCategory to add.
   */
  public void addChatCategory(ChatCategory category)
  {
    Log.debug("chat category: " + category.toString());
    _categories.add(category);

    // Index category by name if its ID is set.
    if (category.getId() != null)
    {
      // Warn about duplicate definitions.
      if (getChatCategoryById(category.getId()) != null)
      {
        Log.warning("ChatCategory ID: "
                    + category.getId()
                    + " cannot be defined more than once - check \"chatcategories.yml\"");
      }
      else
      {
        _categoriesById.put(category.getId(), category);
      }
    }
  } // addChatCategory

  // --------------------------------------------------------------------------
  /**
   * Look up a {@link ChatCategory} by its unique ID.
   * 
   * @param id the unique ID to find.
   * @return the corresponding {@link ChatCategory}, or null if not found.
   */
  public ChatCategory getChatCategoryById(String id)
  {
    return _categoriesById.get(id);
  }

  // --------------------------------------------------------------------------
  /**
   * add an {@link IChatHandler} that gets notified when a {@link ChatLine} is
   * given a {@link ChatCategory}.
   * 
   * @param handler the {@link IChatHandler} that is notified.
   */
  public void addChatHandler(IChatHandler handler)
  {
    _handlers.add(handler);
  }

  // --------------------------------------------------------------------------
  /**
   * Classify the next line of chat input.
   * 
   * @param line the line of chat.
   */
  public void classify(String line)
  {
    Log.info(line);

    ChatLine thisLine = new ChatLine(line);

    // Simplified implementation. Don't attempt ANY rejoining of lines.
    for (ChatCategory category : _categories)
    {
      if (category.matchesFully(thisLine, null))
      {
        thisLine.setCategory(category);
        notify(thisLine);
        return;
      }
    } // for

    thisLine.setCategory(UNKNOWN);
    notify(thisLine);
  } // classify

  // --------------------------------------------------------------------------
  /**
   * Send notification of the category of a {@link ChatLine} to the
   * {@link IChatHandler}.
   * 
   * @param line the line whose {@link ChatCategory} has been determined.
   */
  private void notify(ChatLine line)
  {
    for (IChatHandler handler : _handlers)
    {
      handler.classify(line);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * A list of categories to check against in classify.
   */
  private ArrayList<ChatCategory>       _categories     = new ArrayList<ChatCategory>();

  /**
   * A map from string {#link {@link ChatCategory#getId()} to corresponding
   * {@link ChatCategory}.
   */
  private HashMap<String, ChatCategory> _categoriesById = new HashMap<String, ChatCategory>();

  /**
   * An array of IChatHandlers to dispatch callbacks to.
   */
  private ArrayList<IChatHandler>       _handlers       = new ArrayList<IChatHandler>();

  /**
   * The ChatCategory of lines that don't match any known patterns, even after
   * having been given a chance to match previous lines.
   */
  private final ChatCategory            UNKNOWN         = new ChatCategory(
                                                          "unknown", "unknown",
                                                          "^.*$", "^.*$", false);

  // --------------------------------------------------------------------------
  /**
   * A simple test to see if the ChatClassifier can categorise the chat text
   * loaded from the file specified as the second command line argument, when
   * given the ChatCategory instances specified in the chatcategories.yml file
   * that is as the first command line argument.
   * 
   * @throws IOException
   */
  public static void main(String[] args)
    throws IOException
  {
    // Load categories from args[0].
    ChatClassifier classifier = new ChatClassifier();
    classifier.loadChatCategories(new File(args[0]));

    // Log to standard output
    LogChatHandler log = new LogChatHandler(System.out);
    classifier.addChatHandler(log);

    // Load chat transcript from args[1].
    File transcript = new File(args[1]);
    BufferedReader r = new BufferedReader(new FileReader(transcript));
    String line;
    for (;;)
    {
      line = r.readLine();
      if (line != null)
      {
        classifier.classify(line);
      }
      else
      {
        break;
      }
    } // for
  } // main
} // class ChatClassifier