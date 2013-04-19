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

// --------------------------------------------------------------------------
/**
 * Examines chat messages one line at a time and assigns them to categories.
 * Where the server (Bukkit) has split a line, the ChatClassifier rejoins the
 * parts.
 * 
 * The ChatClassifier has to be future-proof in the sense that if the server
 * starts outputting new messages that don't fit any of the existing defined
 * categories of chat messages, the new messages should be categorised as
 * "unknown", rather than as the overflow of the previous chat message.
 * Unfortunately, it is not possible to be 100% certain of correct
 * classification in such a situation. I consider it better to falsely classify
 * split lines as "unknown", rather than to join new server messages into
 * players' global chat messages.
 * 
 * The various heuristics that might be employed by the ChatClassifier are
 * discussed below:
 * <ul>
 * <li><b>Colour: only join a line to the previous if the colours match at the
 * line break.</b> This fails because the server can use colour to highlight key
 * information within a line. For example, the server has started highlighting
 * players' names in global chat messages.</li>
 * <li><b>Temporal Proximity: only join a line to the previous if they are close
 * together in time (i.e. timeout).</b> Unfortunately, a lag spike could ruin
 * this.</li>
 * <li><b>Line Length: don't append to the previous line if that line is too
 * short to have been split.</b> This is pretty solid. The difficulty comes in
 * determining how short a line must be in order to be unambiguously
 * "not split". Lines are split according to their width in pixels, computed
 * using a variable-width font, so there are a range of lengths (in characters)
 * where a line may be split, or not, depending on the widths of the characters
 * themselves. But a minimum length where splitting won't happen can be
 * determined empirically.</li>
 * </ul>
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
   * Return the last (rightmost) colour code in a chat line, or the default
   * colour if no code is present.
   * 
   * @param chat the chat line to search for a colour code.
   * @return a two-character colour code sequence consisting of the paragraph
   *         marker, '\247', followed by a hex digit.
   */
  public static String getLastColourCode(String chat)
  {
    int index = chat.lastIndexOf(COLOUR_CHAR);

    StringBuilder result = new StringBuilder();
    result.append(COLOUR_CHAR);

    // It is possible that (index+1) could be out of bounds, if the line was
    // split between § and the colour code.
    if (index < 0 || index + 1 >= chat.length())
    {
      result.append(DEFAULT_COLOUR);
    }
    else
    {
      result.append(chat.charAt(index + 1));
    }
    return result.toString();
  } // getLastColourCode

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
   * TODO: Do we really need to separate out those Categories that can lead to
   * split lines from those that always match unsplit lines because the pattern
   * is short? It would be something like ChatCategory.isShort(), signifying
   * that there is no potential for splitting.
   * 
   * @param line the line of chat.
   */
  public void classify(String line)
  {
    Log.info(line);

    ChatLine thisLine = new ChatLine(line);
    // Match this line in isolation.
    boolean matched = false;
    for (ChatCategory category : _categories)
    {
      if (category.matchesStart(thisLine))
      {
        matched = true;
        thisLine.setCategory(category);
        break;
      }
    } // for

    // Did we match the start of the line?
    if (matched)
    {
      ChatCategory matchingCategory = thisLine.getCategory();

      // Is the pattern for a full line different from that for a split line?
      if (matchingCategory.isPedantic())
      {
        // Is the line unsplit and therefore complete?
        if (matchingCategory.matchesFully(thisLine, null))
        {
          notify(thisLine);

          // Is it possible that this line, though complete-looking, might be
          // extended yet?
          if (matchingCategory.isExtensible())
          {
            _incompleteLine = thisLine;
          }
        }
        else
        {
          if (_incompleteLine != null)
          {
            // Discard the previous incomplete line.
            // TODO: shouldn't really happen, so need to adjust algorithm if it
            // does.
            Log.warning("Discarding incomplete line: <"
                        + _incompleteLine.getCategory().getTag() + "> "
                        + _incompleteLine.getFormatted());
          }

          // Save the partially matched line.
          _incompleteLine = thisLine;
        }
      }
      else
      {
        // Matching category is not pedantic. The line makes sense on its own.
        notify(thisLine);

        // Based on length, could this line have been split?
        // Empirically, the shortest split line I have seen is 57 characters,
        // including colour codes. Let's say that if a line is 56 characters
        // or less, it was not split.
        if (thisLine.getFormatted().length() > MIN_SPLIT_POSITION)
        {
          // Save this line away, separately from the known incomplete lines.
          _previousLine = thisLine;
        }
      }
    }
    else
    {
      // The line didn't match any of our line start patterns. It is therefore
      // either a new category of server output, or the split remainder of one
      // of the preceding lines.

      // Give incomplete lines with exacting match requirements precedence.
      if (_incompleteLine != null
          && _incompleteLine.getLastColour() == thisLine.getStartColour()
          && (_incompleteLine.getCategory().matchesFully(_incompleteLine,
            thisLine) || (_incompleteLine.getCategory().isExtensible() && _incompleteLine.getCategory().matchesStart(
            _incompleteLine, thisLine))))
      {
        ChatLine fullLine = new ChatLine(_incompleteLine.getFormatted()
                                         + thisLine.getFormatted());
        fullLine.setCategory(_incompleteLine.getCategory());

        // Is it a simple two-line concatenation job?
        if (!_incompleteLine.getCategory().isExtensible())
        {
          notify(fullLine);
          _incompleteLine = null;
        }
        else
        {
          // notify() may not have previously been called. Problem?
          revise(_incompleteLine, fullLine);

          // Set up for another round of extension of the line.
          _incompleteLine = fullLine;
        }
      }
      else if (_previousLine != null
               && _previousLine.getLastColour() == thisLine.getStartColour()
               && _previousLine.getCategory().matchesFully(_previousLine,
                 thisLine))
      {
        // TODO: timeout requirements?

        ChatLine fullLine = new ChatLine(_previousLine.getFormatted()
                                         + thisLine.getFormatted());
        fullLine.setCategory(_previousLine.getCategory());

        // Since the previous line was passed to notify() revise it.
        revise(_previousLine, fullLine);

        // Also to account for lines split more than once, as with /list,
        // keep appending to _previousLine.
        _previousLine = fullLine;
      }
      else
      {
        // Couldn't put thisLine with _incompleteLine or _previousLine. Call it
        // "unknown".
        thisLine.setCategory(UNKNOWN);
        notify(thisLine);
      }
    }
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
   * If a line is found to have been split and has previously been passed to
   * notify(), this method is called to replace the old version of the line with
   * the new, unsplit version.
   * 
   * @param partLine the part of the line before the line break, previously
   *          passed to notify().
   * @param fullLine the complete, reconstituted line.
   */
  private void revise(ChatLine partLine, ChatLine fullLine)
  {
    for (IChatHandler handler : _handlers)
    {
      handler.revise(partLine, fullLine);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * The minimum character index at which the server might split a line,
   * determined empirically.
   */
  private static final int              MIN_SPLIT_POSITION = 56;

  /**
   * A list of categories to check against in classify.
   */
  private ArrayList<ChatCategory>       _categories        = new ArrayList<ChatCategory>();

  /**
   * A map from string {#link {@link ChatCategory#getId()} to corresponding
   * {@link ChatCategory}.
   */
  private HashMap<String, ChatCategory> _categoriesById    = new HashMap<String, ChatCategory>();

  /**
   * An array of IChatHandlers to dispatch callbacks to.
   */
  private ArrayList<IChatHandler>       _handlers          = new ArrayList<IChatHandler>();

  /**
   * The previous line process by classify. If no concatenation of lines is
   * possible, this field is null.
   * 
   * TODO: This is probably wrong: may need a priority list to match the tail
   * ends of lines in the case where line broken /lb coords output interleaves
   * with line broken global chat. Essentially, there are two threads
   * interleaving their split lines to a single stream. e.g.
   * 
   * <pre>
   * §6(27) 04-16 15:39:42 robisONE destroyed diamond ore at 631:12
   * §7[me§7 -> coggas§7] §fPOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
   * §6:1210
   * OOOOOOOOOOOOOOOOOOOOOP!!!!
   * </pre>
   */
  private ChatLine                      _previousLine;

  /**
   * If a line was unambiguously matched as "incomplete" (requiring a subsequent
   * line to match the full pattern), then that line is saved here.
   */
  private ChatLine                      _incompleteLine;

  /**
   * The ChatCategory of lines that don't match any known patterns, even after
   * having been given a chance to match previous lines.
   */
  private final ChatCategory            UNKNOWN            = new ChatCategory(
                                                             "unknown",
                                                             "unknown", "^.*$",
                                                             "^.*$", false);

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