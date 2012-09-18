package watson;

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

import net.minecraft.src.mod_Watson;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import watson.chat.ChatClassifier;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * Uses colour to highlights parts of chat messages that match regular
 * expressions.
 * 
 * This is useful for drawing attention to naughty words in chat, and can also
 * be used to modify the colour of messages originating from the server.
 * 
 * TODO: add support for '+', '_', '-' and '/' in colour codes to signify bold,
 * underline, strikeout and italic font styles respectively, in conjunction with
 * or as an alternative to pure colour. e.g. /col set +_red firetruck
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
   * Highlight any sections of the chat line that match registered highlighting
   * patterns.
   */
  public String highlight(String chat)
  {
    String result = chat;
    for (Highlight h : _highlights)
    {
      result = h.highlight(result);
    }
    return result;
  }

  // --------------------------------------------------------------------------
  /**
   * Add another pattern to highlight.
   * 
   * @param colour either the name of a colour, or a single character colour
   *          code in the character class [0-9a-fA-F].
   * @param pattern the regular expression describing sequences of characters to
   *          be highlighted.
   */
  public void addHighlight(String colour, String pattern)
  {
    Character colourCode = getColourCode(colour);
    if (colourCode == null)
    {
      Controller.instance.localError(colour + " is not a valid colour name.");
    }
    else
    {
      try
      {
        Highlight highlight = new Highlight(colourCode, pattern);
        _highlights.add(highlight);
        Controller.instance.localChat("Added highlight #" + _highlights.size()
                                      + " " + highlight.toString());
        saveHighlights();
      }
      catch (PatternSyntaxException ex)
      {
        Controller.instance.localError(pattern
                                       + " is not a valid regular expression.");
      }
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
      Controller.instance.localChat("No highlights set.");
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
        builder.append(_colourNames.get(highlight.colourCode));
        builder.append(' ');
        builder.append(highlight.pattern.pattern());
        Controller.instance.localChat(builder.toString());
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
      Controller.instance.localError(error.toString());
    }
    else
    {
      _highlights.remove(index - 1);
      Controller.instance.localChat("Removed highlight #" + index);
      saveHighlights();
    }
  } // removeHighlight

  // --------------------------------------------------------------------------
  /**
   * Load the chat categories from a YAML file in the mod's configuration
   * subdirectory, or from minecraft.jar as a fallback.
   */
  public void loadHighlights()
  {
    try
    {
      InputStream in = mod_Watson.getConfigurationStream(CHAT_HIGHLIGHTS_FILE);
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
   * Load the excluded chat category tags from the specified InputStream.
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
   * Save the highlights to the disk.
   */
  public void saveHighlights()
  {
    try
    {
      File file = new File(mod_Watson.getModDirectory(), CHAT_HIGHLIGHTS_FILE);
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
   * Define a colour name to colour code mapping.
   * 
   * The first name specified for a given colour code is treated as the
   * canonical name, which will be returned when looking up the colour name by
   * colour code.
   * 
   * @param name the name of the colour.
   * @param colourCode the corresponding colour code character.
   */
  static void addColour(String name, char colourCode)
  {
    Character code = colourCode;
    _colours.put(name, code);
    if (!_colourNames.containsKey(code))
    {
      _colourNames.put(code, name);
    }
  } // addColour

  // --------------------------------------------------------------------------
  /**
   * Return the single character colour code for the colour with the given name.
   * 
   * @param colourName the name of the colour, or a single character that is a
   *          valid colour code in the character class [0-9a-fA-F].
   * @return the corresponding normalised (lower case) colour code character in
   *         the character class [0-9a-f].
   */
  static Character getColourCode(String colourName)
  {
    if (colourName.length() == 1)
    {
      char code = Character.toLowerCase(colourName.charAt(0));
      if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f'))
      {
        return code;
      }
    }

    // Otherwise, more than one character, or a single non-colour-code
    // character:
    return _colours.get(colourName.toLowerCase());
  } // getColourCode

  // --------------------------------------------------------------------------
  /**
   * A POD type that records the association between a colour code and a
   * Pattern.
   */
  private static class Highlight
  {
    /**
     * Constuctor.
     * 
     * @param colourCode a validated colour code in [0-9a-f].
     * @param pattern a regular expression to match.
     * @throws PatternSyntaxException if the pattern doesn't compile.
     */
    public Highlight(char colourCode, String pattern)
    {
      this.colourCode = colourCode;
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

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
      this(getColourCode((String) attributes.get("colourCode")),
        (String) attributes.get("pattern"));
    }

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
      data.put("colourCode", _colourNames.get(colourCode));
      data.put("pattern", pattern.pattern());
      return data;
    }

    /**
     * Highlight with colourCode any parts of the specified chat line that match
     * pattern.
     * 
     * @param chat the chat line to highlight.
     * @return the version of the line with highlighting inserted.
     */
    public String highlight(String chat)
    {
      Matcher m = pattern.matcher(chat);
      int start = 0;
      StringBuilder result = new StringBuilder();
      while (m.find())
      {
        // Add everything before the match to the result.
        String head = chat.substring(start, m.start());
        result.append(head);

        // Work out the original colour code.
        String originalColourCode = ChatClassifier.getLastColourCode(result.toString());

        // Append the highlighted match.
        result.append(ChatClassifier.COLOUR_CHAR);
        result.append(colourCode);
        result.append(chat.substring(m.start(), m.end()));

        // Restore the original or default colour after the highlight.
        // Assuming here that the match itself does not contain any
        // colour codes that we care about.
        result.append(originalColourCode);

        // Set up to extract the next head substring.
        start = m.end();
      } // while there are matches

      // Append any remaining characters after the last match.
      result.append(chat.substring(start, chat.length()));
      return result.toString();
    } // highlight

    /**
     * Return a string representation of this Highlight (shown to the user).
     */
    public String toString()
    {
      return _colourNames.get(colourCode) + ' ' + pattern.pattern();
    }

    /**
     * The chat colour code character to assign (0-9, a-f).
     */
    public char    colourCode;

    /**
     * The regular expression which describes what should be highlighted.
     */
    public Pattern pattern;
  }; // inner class Highlight

  // --------------------------------------------------------------------------
  /**
   * Highlight patterns.
   */
  protected ArrayList<Highlight>              _highlights  = new ArrayList<ChatHighlighter.Highlight>();

  /**
   * Maps colour names to colour code characters.
   */
  protected static HashMap<String, Character> _colours     = new HashMap<String, Character>();

  /**
   * Maps colour code characters to colour names.
   */
  protected static HashMap<Character, String> _colourNames = new HashMap<Character, String>();

  // Register all of the standard colour names. They should all be lower case.
  static
  {
    addColour("black", '0');

    addColour("darkblue", '1');
    addColour("navy", '1');

    addColour("green", '2');
    addColour("darkgreen", '2');

    addColour("cyan", '3');

    addColour("red", '4');
    addColour("darkred", '4');

    addColour("purple", '5');

    addColour("orange", '6');
    addColour("gold", '6');
    addColour("brown", '6');

    addColour("lightgrey", '7');
    addColour("lightgray", '7');

    addColour("grey", '8');
    addColour("darkgrey", '8');
    addColour("gray", '8');
    addColour("darkgray", '8');

    addColour("blue", '9');

    addColour("lightgreen", 'a');

    addColour("lightblue", 'b');

    addColour("lightred", 'c');
    addColour("brightred", 'c');
    addColour("rose", 'c');

    addColour("pink", 'd');
    addColour("lightpurple", 'd');
    addColour("magenta", 'd');

    addColour("yellow", 'e');

    addColour("white", 'f');
  }
} // class ChatHighlighter