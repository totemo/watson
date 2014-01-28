package watson.chat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import watson.debug.Log;

// --------------------------------------------------------------------------
/**
 * A chat handler that dispatches all chat callbacks to a default IChatHandler,
 * except when the tag is listed in a set of tags to exclude.
 * 
 * This class is used to exclude certain types of chat messages from chat, such
 * as LogBlock "lb.coord" {@link watson.chat.ChatLine}s, which are generated en
 * masse.
 */
public class ExcludeTagChatHandler implements IChatHandler
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public ExcludeTagChatHandler()
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param defaultHandler the default handler that will handle chat messages
   *          that are not in the excluded set.
   */
  public ExcludeTagChatHandler(IChatHandler defaultHandler)
  {
    setDefaultHandler(defaultHandler);
  }

  // --------------------------------------------------------------------------
  /**
   * Load the excluded chat category tags from the specified InputStream.
   * 
   * @param in the stream.
   */
  @SuppressWarnings("unchecked")
  public void loadExclusions(InputStream in)
  {
    try
    {
      Yaml yaml = new Yaml();
      HashMap<String, Object> root = (HashMap<String, Object>) yaml.load(in);
      ArrayList<Object> exclusions = (ArrayList<Object>) root.get("exclusions");
      if (exclusions != null)
      {
        for (Object tag : exclusions)
        {
          if (tag instanceof String)
          {
            setExcluded((String) tag, true);
          }
          else
          {
            Log.warning("unexpected data in exclusions file: " + tag);
          }
        }
      }
    }
    catch (Exception ex)
    {
      Log.exception(Level.SEVERE, "error loading chat exclusions: ", ex);
    }
  } // loadExclusions

  // --------------------------------------------------------------------------
  /**
   * Save the excluded chat category tags to the specified file.
   */
  public void saveExclusions(File file)
  {
    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      try
      {
        HashMap<String, Object> root = new HashMap<String, Object>();
        root.put("exclusions", new ArrayList<String>(_excludedTags));

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
      Log.exception(Level.SEVERE, "error saving chat exclusions: ", ex);
    }
  } // saveExclusions

  // --------------------------------------------------------------------------
  /**
   * Set the default handler to which callbacks will be passed if the chat is
   * not in the excluded set.
   * 
   * @param defaultHandler the default handler.
   */
  public void setDefaultHandler(IChatHandler defaultHandler)
  {
    _defaultHandler = defaultHandler;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the default handler to which callbacks will be passed if the chat is
   * not in the excluded set.
   * 
   * @return the default handler to which callbacks will be passed if the chat
   *         is not in the excluded set.
   */
  public IChatHandler getDefaultHandler()
  {
    return _defaultHandler;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the exclusion state (excluded or not) of the specified tag.
   * 
   * @param tag the tag to be excluded or not.
   * @param excluded true if excluded; false if not.
   */
  public void setExcluded(String tag, boolean excluded)
  {
    if (excluded)
    {
      _excludedTags.add(tag);
    }
    else
    {
      _excludedTags.remove(tag);
    }
  } // setExcluded

  // --------------------------------------------------------------------------
  /**
   * Return the set of excluded tags.
   * 
   * @return the set of excluded tags.
   */
  public Set<String> getExcludedTags()
  {
    return Collections.unmodifiableSet(_excludedTags);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified tag is excluded.
   * 
   * @param tag the tag to be queried.
   * @return true if the specified tag is excluded.
   */
  public boolean isExcluded(String tag)
  {
    return _excludedTags.contains(tag);
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    if (!isExcluded(line.getCategory().getTag()) && getDefaultHandler() != null)
    {
      getDefaultHandler().classify(line);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * The default handler to pass callbacks to if the chat is not excluded.
   */
  protected IChatHandler    _defaultHandler;

  /**
   * The set of tags that are excluded from being passed on to the default
   * handler.
   */
  protected HashSet<String> _excludedTags = new HashSet<String>();
} // class ExcludeTagChatHandler