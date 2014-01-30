package watson.chat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.util.IChatComponent;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import watson.Controller;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * Uses colour and formatting to highlights parts of chat messages that match
 * regular expressions.
 * 
 * This is useful for drawing attention to naughty words in chat, and can also
 * be used to modify the colour of messages originating from the server.
 * 
 * {@link Format} controls the colour and formatting attributes of text. See the
 * documentation of that class for more information on Watson's take on
 * formatting codes.
 */
public class ChatHighlighter
{
  // --------------------------------------------------------------------------
  /**
   * The basename of the file where the highlight settings are stored (both in
   * the Minecraft JAR and .minecraft/mods/watson/).
   */
  public static final String CHAT_HIGHLIGHTS_FILE = "chathighlights.yml";

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * Sets up the map of colour names to colour codes.
   */
  public ChatHighlighter()
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the chat text contains a Rei's Minimap enabling sequence or
   * consists entirely of colour codes.
   * 
   * Rei's Minimap enables radar features based on a sequence of colour codes in
   * the server's welcome message (MOTD). The sequence has to make it all the
   * way into the scrollback buffer of the chat GUI. That is, it must be
   * displayed exactly as it was received by the client in order for Rei's to
   * work correctly. Therefore, the highighter should not mess with those lines
   * or lines consisting only of colour which may do something similar to Rei's.
   * 
   * @return true if the chat contains a sequence of colour codes like that used
   *         by Rei's Minimap.
   */
  boolean isReisLikeCode(String chat)
  {
    Matcher reis = REIS_CODE.matcher(chat);
    Matcher allColour = COLOUR_LINE.matcher(chat);
    return reis.find() || allColour.matches();
  }

  // --------------------------------------------------------------------------
  /**
   * Highlight the text in a chat component.
   * 
   * @param chat the text to highlight.
   * @return highlighted text.
   */
  IChatComponent highlight(IChatComponent chat)
  {
    if (isReisLikeCode(chat.getFormattedText()))
    {
      return chat;
    }
    else
    {
      ArrayList<IChatComponent> resultComponents = new ArrayList<IChatComponent>();
      ArrayList<IChatComponent> components = ChatComponents.getComponents(chat);
      while (!components.isEmpty())
      {
        IChatComponent head = components.remove(0);
        if (ChatComponents.hasEvents(head))
        {
          // Can't currently highlight links etc.
          resultComponents.add(head);
        }
        else
        {
          // Collect all consecutive components that don't have events
          // and therefore can be highlighted.
          ArrayList<IChatComponent> highlightableComps = new ArrayList<IChatComponent>();
          highlightableComps.add(head);

          while (!components.isEmpty())
          {
            IChatComponent next = components.get(0);
            if (ChatComponents.hasEvents(next))
            {
              break;
            }
            else
            {
              highlightableComps.add(next);
              components.remove(0);
            }
          } // while

          IChatComponent highlightable = ChatComponents.toChatComponent(highlightableComps);
          String highlightableText = highlightable.getFormattedText();
          Text highlighted = highlight(highlightableText);
          resultComponents.add(highlighted.toChatComponent());
        }
      } // while there are components to consider
      return ChatComponents.toChatComponent(resultComponents);
    }
  } // highlight

  // --------------------------------------------------------------------------
  /**
   * Highlight any sections of the chat line that match registered highlighting
   * patterns.
   * 
   * This method should not be called on chats for which isReisLikeCode() is
   * true.
   */
  public Text highlight(String chat)
  {
    Text text = new Text(chat);
    for (Highlight h : _highlights)
    {
      h.highlight(text);
    }
    return text;
  }

  // --------------------------------------------------------------------------
  /**
   * Add another pattern to highlight.
   * 
   * @param format a format specifier consisting of zero or more of the
   *          punctuation symbols [+/_-?], and/or a colour name or a Minecraft
   *          colour code character, [0-9a-fA-F].
   * @param pattern the regular expression describing sequences of characters to
   *          be highlighted.
   * @param selection if true, only regexp groups in the pattern are
   *          reformatted.
   */
  public void addHighlight(String format, String pattern, boolean selection)
  {
    try
    {
      Highlight highlight = new Highlight(new Format(format), pattern, selection);
      _highlights.add(highlight);
      Chat.localOutput("Added highlight #" + _highlights.size() + " " + highlight.toString());
      saveHighlights();
    }
    catch (PatternSyntaxException ex)
    {
      Chat.localError(pattern + " is not a valid regular expression.");
    }
    catch (IllegalArgumentException ex)
    {
      Chat.localError(format + " is not a valid format specifier.");
    }
  } // addHighlight

  // --------------------------------------------------------------------------
  /**
   * List all of the highlighting patterns in chat, with numeric identifiers so
   * that the user can interactively remove them.
   */
  public void listHighlights()
  {
    if (_highlights.size() == 0)
    {
      Chat.localOutput("No highlights set.");
    }
    else
    {
      for (int i = 0; i < _highlights.size(); ++i)
      {
        Highlight highlight = _highlights.get(i);
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(i + 1);
        builder.append(") ");
        builder.append(highlight.toString());
        Chat.localOutput(builder.toString());
      } // for
    } // else
  } // listHighlights

  // --------------------------------------------------------------------------
  /**
   * Remove the highlight identified by the 1-based identifier as shown by
   * listHighlights().
   */
  public void removeHighlight(int index)
  {
    if (index < 1 || index > _highlights.size())
    {
      StringBuilder error = new StringBuilder();
      error.append(index);
      error.append(" is out of range.");
      Chat.localError(error.toString());
    }
    else
    {
      _highlights.remove(index - 1);
      Chat.localOutput("Removed highlight #" + index);
      saveHighlights();
    }
  } // removeHighlight

  // --------------------------------------------------------------------------
  /**
   * Load the highlights from a YAML file in the mod's configuration
   * subdirectory, or from minecraft.jar as a fallback.
   */
  public void loadHighlights()
  {
    try
    {
      InputStream in = Controller.getConfigurationStream(CHAT_HIGHLIGHTS_FILE);
      try
      {
        loadHighlights(in);
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
  } // loadHighlights

  // --------------------------------------------------------------------------
  /**
   * Load the highlights from the specified InputStream.
   * 
   * TODO: this could, with some effort, be a bit more forgiving of file format
   * errors.
   * 
   * @param in the stream.
   */
  @SuppressWarnings("unchecked")
  public void loadHighlights(InputStream in)
  {
    try
    {
      _highlights.clear();

      Yaml yaml = new Yaml();
      HashMap<String, Object> root = (HashMap<String, Object>) yaml.load(in);
      ArrayList<Object> highlights = (ArrayList<Object>) root.get("highlights");
      if (highlights != null)
      {
        for (Object entry : highlights)
        {
          try
          {
            Highlight highlight = new Highlight((HashMap<String, Object>) entry);
            _highlights.add(highlight);
          }
          catch (Exception ex)
          {
            Log.exception(Level.SEVERE, "error loading chat highlights: ", ex);
          }
        }
      }
    }
    catch (Exception ex)
    {
      Log.exception(Level.SEVERE, "error loading chat highlights: ", ex);
    }
  } // loadHighlights

  // --------------------------------------------------------------------------
  /**
   * Save the highlights to the configuration file.
   */
  public void saveHighlights()
  {
    try
    {
      File file = new File(Controller.getModDirectory(), CHAT_HIGHLIGHTS_FILE);
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      try
      {
        ArrayList<Object> highlights = new ArrayList<Object>();
        for (Highlight hl : _highlights)
        {
          highlights.add(hl.getSaveData());
        }
        HashMap<String, Object> root = new HashMap<String, Object>();
        root.put("highlights", highlights);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        yaml.dump(root, writer);
      }
      finally
      {
        writer.close();
      }
    }
    catch (Exception ex)
    {
      Log.exception(Level.SEVERE, "error saving chat highlights: ", ex);
    }
  } // saveHighlights

  // --------------------------------------------------------------------------
  /**
   * Records the association between a colour code and a Pattern.
   */
  private static class Highlight
  {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param format the Format to highlight chat text that matches the Pattern.
     * @param pattern a regular expression to match.
     * @param selection if true, only regexp groups in the pattern are
     *          reformatted.
     * @throws PatternSyntaxException if the pattern doesn't compile.
     */
    public Highlight(Format format, String pattern, boolean selection)
    {
      _format = format;
      _pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
      _selection = selection;
    }

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * Reads the colourCode and pattern attributes from a HashMap<> loaded by
     * SnakeYAML.
     * 
     * @param attributes the map of attributes.
     * @throws NullPointerException or ClassCastException if the data from the
     *           file is incorrect.
     */
    public Highlight(HashMap<String, Object> attributes)
    {
      // Let this barf exceptions.
      this(new Format((String) attributes.get("colourCode")),
           (String) attributes.get("pattern"),
           ((attributes.get("selection") != null)
             ? (Boolean) attributes.get("selection")
             : false));
    }

    // ------------------------------------------------------------------------
    /**
     * Return a HashMap<> of the attributes of this object that is suitable for
     * YAML serialisation.
     * 
     * @return a HashMap<> of the attributes of this object that is suitable for
     *         YAML serialisation.
     */
    public HashMap<String, Object> getSaveData()
    {
      HashMap<String, Object> data = new HashMap<String, Object>();
      data.put("colourCode", _format.getCode());
      data.put("pattern", _pattern.pattern());
      data.put("selection", _selection);
      return data;
    }

    // ------------------------------------------------------------------------
    /**
     * Highlight with colour any parts of the specified chat text that match the
     * pattern, or if _selection is true, only highlight the regexp matcher
     * groups.
     * 
     * @param text the chat text to highlight, modified in place.
     */
    public void highlight(Text text)
    {
      Matcher m = _pattern.matcher(text.toUnformattedString());
      if (_selection)
      {
        if (m.matches())
        {
          for (int i = 1; i <= m.groupCount(); ++i)
          {
            // If the group matches nothing, m.start(i) is -1.
            if (m.start(i) >= 0 && m.end(i) > m.start(i))
            {
              text.setFormat(m.start(i), m.end(i), _format);
            }
          }
        }
      }
      else
      {
        while (m.find())
        {
          text.setFormat(m.start(), m.end(), _format);
        }
      }
    } // highlight

    // ------------------------------------------------------------------------
    /**
     * Return a string representation of this Highlight (shown to the user).
     */
    public String toString()
    {
      return (_selection ? "select " : "") + _format.toString() + ' '
             + _pattern.pattern();
    }

    // ------------------------------------------------------------------------
    /**
     * The chat colour to assign.
     */
    protected Format  _format;

    /**
     * The regular expression which describes what should be highlighted.
     */
    protected Pattern _pattern;

    /**
     * If true, only regexp groups in the pattern are reformatted.
     */
    protected boolean _selection;

  }; // inner class Highlight

  // --------------------------------------------------------------------------
  /**
   * Regexp describing Rei's radar enabling codes. Note that Rei's does not
   * require the code to be the entire contents of the line.
   */
  protected static final Pattern REIS_CODE   = Pattern.compile("\2470\2470(?:\247[1-9a-d])+\247e\247f");

  /**
   * Regexp describing lines that consist only of colour codes. Such lines are
   * presumed to be a Rei's-like mechanism that we should not disrupt.
   */
  protected static final Pattern COLOUR_LINE = Pattern.compile("^(?:\247[0-9a-fk-or])+$");

  /**
   * Highlight patterns.
   */
  protected ArrayList<Highlight> _highlights = new ArrayList<ChatHighlighter.Highlight>();
} // class ChatHighlighter