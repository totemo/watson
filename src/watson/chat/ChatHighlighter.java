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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import watson.chat.Format;
import watson.chat.Text;
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
   * Highlight any sections of the chat line that match registered highlighting
   * patterns.
   */
  public String highlight(String chat)
  {
    Text text = new Text(chat);
    for (Highlight h : _highlights)
    {
      h.highlight(text);
    }
    return text.toFormattedString();
  }

  // --------------------------------------------------------------------------
  /**
   * Add another pattern to highlight.
   * 
   * @param format a format specifier consisting of zero or more of the
   *          punctuation symbols [+/_-?], and/or a colour name or a Minecraft
   *          colour code character, [0-9]a-fA-F].
   * @param pattern the regular expression describing sequences of characters to
   *          be highlighted.
   */
  public void addHighlight(String format, String pattern)
  {
    try
    {
      Highlight highlight = new Highlight(new Format(format), pattern);
      _highlights.add(highlight);
      Controller.instance.localOutput("Added highlight #" + _highlights.size()
                                      + " " + highlight.toString());
      saveHighlights();
    }
    catch (PatternSyntaxException ex)
    {
      Controller.instance.localError(pattern
                                     + " is not a valid regular expression.");
    }
    catch (IllegalArgumentException ex)
    {
      Controller.instance.localError(format
                                     + " is not a valid format specifier.");
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
      Controller.instance.localOutput("No highlights set.");
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
        Controller.instance.localOutput(builder.toString());
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
      Controller.instance.localOutput("Removed highlight #" + index);
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
     * @throws PatternSyntaxException if the pattern doesn't compile.
     */
    public Highlight(Format format, String pattern)
    {
      this.format = format;
      this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
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
        (String) attributes.get("pattern"));
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
      data.put("colourCode", format.getCode());
      data.put("pattern", pattern.pattern());
      return data;
    }

    // ------------------------------------------------------------------------
    /**
     * Highlight with colour any parts of the specified chat text that match the
     * pattern.
     * 
     * @param text the chat text to highlight, modified in place.
     */
    public void highlight(Text text)
    {
      Matcher m = pattern.matcher(text.toUnformattedString());
      while (m.find())
      {
        text.setFormat(m.start(), m.end(), format);
      }
    } // highlight

    // ------------------------------------------------------------------------
    /**
     * Return a string representation of this Highlight (shown to the user).
     */
    public String toString()
    {
      return format.toString() + ' ' + pattern.pattern();
    }

    // ------------------------------------------------------------------------
    /**
     * The chat colour to assign.
     */
    public Format  format;

    /**
     * The regular expression which describes what should be highlighted.
     */
    public Pattern pattern;

  }; // inner class Highlight

  // --------------------------------------------------------------------------
  /**
   * Highlight patterns.
   */
  protected ArrayList<Highlight> _highlights = new ArrayList<ChatHighlighter.Highlight>();
} // class ChatHighlighter