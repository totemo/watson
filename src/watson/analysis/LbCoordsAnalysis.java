package watson.analysis;

import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.BlockEdit;
import watson.BlockType;
import watson.BlockTypeRegistry;
import watson.Configuration;
import watson.Controller;
import watson.TimeStamp;
import watson.chat.ChatClassifier;
import watson.chat.ChatProcessor;
import watson.chat.Colour;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;
import watson.debug.Log;

// --------------------------------------------------------------------------
/**
 * An {@link Analysis} implementation that extracts {@link BlockEdit} instances
 * from lb.coord lines.
 */
public class LbCoordsAnalysis extends watson.analysis.Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    // Set up some test handling of lb.coord:
    tagDispatchChatHandler.addChatHandler("lb.coord", new MethodChatHandler(
      this, "lbCoord"));
    tagDispatchChatHandler.addChatHandler("lb.coordreplaced",
      new MethodChatHandler(this, "lbCoordReplaced"));
    tagDispatchChatHandler.addChatHandler("lb.page", new MethodChatHandler(
      this, "lbPage"));
    tagDispatchChatHandler.addChatHandler("lb.header", new MethodChatHandler(
      this, "lbHeader"));
    tagDispatchChatHandler.addChatHandler("lb.header.noresults",
      new MethodChatHandler(this, "lbHeader"));

    _lbCoord = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.coord").getFullPattern();
    _lbCoordReplaced = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.coordreplaced").getFullPattern();
    _lbPage = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.page").getFullPattern();
  } // registerAnalysis

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.coord" category.
   */
  @SuppressWarnings("unused")
  private void lbCoord(watson.chat.ChatLine line)
  {
    try
    {
      // TODO: describe Matcher groups and their conversions in a config file.
      // Provide a way to get a set of named properties of a line.
      // Use reflection or JavaBeans Statement/Expression to create the
      // BlockEdit as directed by config file.

      Matcher m = _lbCoord.matcher(line.getUnformatted());
      if (m.matches())
      {
        int index = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));
        int hour = Integer.parseInt(m.group(4));
        int minute = Integer.parseInt(m.group(5));
        int second = Integer.parseInt(m.group(6));
        long millis = TimeStamp.toMillis(month, day, hour, minute, second);

        String player = m.group(7);
        String action = m.group(8);
        String block = m.group(9);

        // If there are an extra 4 groups, then we're dealing with a sign.
        String sign1 = null, sign2 = null, sign3 = null, sign4 = null;
        int x, y, z;
        if (m.groupCount() == 16)
        {
          sign1 = m.group(10);
          sign2 = m.group(11);
          sign3 = m.group(12);
          sign4 = m.group(13);
          x = Integer.parseInt(m.group(14));
          y = Integer.parseInt(m.group(15));
          z = Integer.parseInt(m.group(16));
        }
        else
        {
          x = Integer.parseInt(m.group(10));
          y = Integer.parseInt(m.group(11));
          z = Integer.parseInt(m.group(12));
        }

        BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(block);
        Controller.instance.getVariables().put("time", millis);
        Controller.instance.getVariables().put("player", player);
        Controller.instance.getVariables().put("block", type.getId());
        Controller.instance.getVariables().put("x", x);
        Controller.instance.getVariables().put("y", y);
        Controller.instance.getVariables().put("z", z);

        boolean created = action.equals("created");
        Controller.instance.getBlockEditSet().addBlockEdit(
          new BlockEdit(millis, player, created, x, y, z, type));

        // TODO: fix this :) Have a class that allows dynamic control of
        // filtered coords.
        // Hacked in re-echoing of coords so we can see TP targets.
        if (type.getId() != 1)
        {
          String output;
          if (sign1 == null)
          {
            output = String.format(Locale.US,
              "\247%c(%2d) %02d-%02d %02d:%02d:%02d (%d,%d,%d) %C%d %s",
              getChatColourChar(x, y, z), index, month, day, hour, minute,
              second, x, y, z, (created ? '+' : '-'), type.getId(), player);
          }
          else
          {
            output = String.format(
              Locale.US,
              "\247%c(%2d) %02d-%02d %02d:%02d:%02d (%d,%d,%d) %C%d %s [%s] [%s] [%s] [%s]",
              getChatColourChar(x, y, z), index, month, day, hour, minute,
              second, x, y, z, (created ? '+' : '-'), type.getId(), player,
              sign1, sign2, sign3, sign4);
          }
          Controller.instance.localChat(output);
        }

        requestNextPage();
      } // regexp matched
    }
    catch (Exception ex)
    {
      Log.exception(Level.INFO, "error parsing lb coords", ex);
    }
  } // lbCoord
  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.coord" category.
   */
  @SuppressWarnings("unused")
  private void lbCoordReplaced(watson.chat.ChatLine line)
  {
    try
    {
      // TODO: describe Matcher groups and their conversions in a config file.
      // Provide a way to get a set of named properties of a line.
      // Use reflection or JavaBeans Statement/Expression to create the
      // BlockEdit as directed by config file.

      Matcher m = _lbCoordReplaced.matcher(line.getUnformatted());
      if (m.matches())
      {
        int index = Integer.parseInt(m.group(1));
        int month = Integer.parseInt(m.group(2));
        int day = Integer.parseInt(m.group(3));
        int hour = Integer.parseInt(m.group(4));
        int minute = Integer.parseInt(m.group(5));
        int second = Integer.parseInt(m.group(6));
        long millis = TimeStamp.toMillis(month, day, hour, minute, second);

        String player = m.group(7);
        String oldBlock = m.group(8);
        // UNUSED: String newBlock = m.group(9);
        int x = Integer.parseInt(m.group(10));
        int y = Integer.parseInt(m.group(11));
        int z = Integer.parseInt(m.group(12));

        BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(oldBlock);
        Controller.instance.getVariables().put("time", millis);
        Controller.instance.getVariables().put("player", player);
        Controller.instance.getVariables().put("block", type.getId());
        Controller.instance.getVariables().put("x", x);
        Controller.instance.getVariables().put("y", y);
        Controller.instance.getVariables().put("z", z);

        // Store the destruction but don't bother with the creation.
        Controller.instance.getBlockEditSet().addBlockEdit(
          new BlockEdit(millis, player, false, x, y, z, type));

        // TODO: fix this :)
        // Hacked in re-echoing of coords so we can see TP targets.
        if (type.getId() != 1)
        {
          String target = String.format(Locale.US,
            "\247%c(%2d) %02d-%02d %02d:%02d:%02d (%d,%d,%d) %C%d %s",
            getChatColourChar(x, y, z), index, month, day, hour, minute,
            second, x, y, z, '-', type.getId(), player);
          Controller.instance.localChat(target);
        }

        requestNextPage();
      } // regexp matched
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbCoordReplaced

  // --------------------------------------------------------------------------
  /**
   * Get the colour to highlight coordinates when they are re-echoed into chat.
   * 
   * The colour changes whenever the
   */
  private char getChatColourChar(int x, int y, int z)
  {
    // Check whether we should advance the index.
    int dx = x - _lastX;
    int dy = y - _lastY;
    int dz = z - _lastZ;

    // Skip the sqrt().
    float distance = dx * dx + dy * dy + dz * dz;
    if (distance > _COLOUR_PROXIMITY_LIMIT * _COLOUR_PROXIMITY_LIMIT)
    {
      _colourIndex = (_colourIndex + 1) % _COLOUR_CYCLE.length;
    }
    _lastX = x;
    _lastY = y;
    _lastZ = z;

    return _COLOUR_CYCLE[_colourIndex];
  } // getChatColourChar

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.page" category.
   * 
   * We run "/lb page (n+1)" automatically if the number of pages of results in
   * the "/lb coords" output is less than or equal to the max_auto_pages
   * configuration setting.
   */
  @SuppressWarnings("unused")
  private void lbPage(watson.chat.ChatLine line)
  {
    try
    {
      Matcher m = _lbPage.matcher(line.getUnformatted());
      if (m.matches())
      {
        int currentPage = Integer.parseInt(m.group(1));
        int pageCount = Integer.parseInt(m.group(2));

        // Enforce the page limit here.
        if (pageCount <= Configuration.instance.getMaxAutoPages())
        {
          _currentPage = currentPage;
          _pageCount = pageCount;
        }
        else
        {
          _currentPage = _pageCount = 0;
        }
      }
    }
    catch (Exception ex)
    {
      // System.out.println(ex);
    }
  } // lbPage

  // --------------------------------------------------------------------------
  /**
   * Sometimes you do an /lb query (e.g. "/lb time 4h block 56 sum p") that
   * results in a page header ("Page 1/3"), and immediately follow that with an
   * "/lb coords" query that doesn't have a page header. Consequently,
   * _currentPage and _pageCount can be set to the values for the preceding
   * query and requestNextPage() will attempt to page through. To prevent that,
   * we look for the various headers in /lb results and clear the counters.
   */
  @SuppressWarnings("unused")
  private void lbHeader(watson.chat.ChatLine line)
  {
    _currentPage = _pageCount = 0;
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called when coordinates are parsed out of chat to request
   * the next page of "/lb coords" results, up to the configured maximum number
   * of pages.
   */
  private void requestNextPage()
  {
    if (Configuration.instance.isAutoPage())
    {
      if (_currentPage != 0 && _currentPage < _pageCount
          && _pageCount <= Configuration.instance.getMaxAutoPages())
      {
        Controller.instance.serverChat(String.format(Locale.US, "/lb page %d",
          _currentPage + 1));

        // Remember that we don't need to do this again until next page is
        // parsed.
        _currentPage = _pageCount = 0;
      }
    }
  } // requestNextPage

  // --------------------------------------------------------------------------
  /**
   * The cycle of colours used to highlight distinct ore deposits when
   * coordinates are re-echoed.
   */
  protected static final char  _COLOUR_CYCLE[]         = {Colour.red.getCode(),
    Colour.orange.getCode(), Colour.yellow.getCode(),
    Colour.lightgreen.getCode(), Colour.lightblue.getCode(),
    Colour.purple.getCode(), Colour.magenta.getCode()  };

  /**
   * The index into the _COLOUR_CYCLE array referencing the current chat colour.
   * 
   * Since the very first colour is pretty much guaranteed to roll over (since
   * _lastX, _lastY, _lastZ will be nowhere near), init to the end of the cycle
   * in anticipation).
   * 
   * TODO: Make the colour of echoed coordinates stable? Or at least reset when
   * the lb header is seen.
   */
  protected int                _colourIndex            = _COLOUR_CYCLE.length - 1;

  /**
   * The minimum distance that ore deposits (or blocks in general) must be
   * separated by to colour them differently when their coordinates are echoed
   * in chat.
   */
  protected static final float _COLOUR_PROXIMITY_LIMIT = 4.0f;

  /**
   * The last set of coordinates re-echoed in chat.
   */
  protected int                _lastX, _lastY, _lastZ;

  /**
   * Current page number extracted from lb.page lines.
   */
  protected int                _currentPage            = 0;

  /**
   * Total number of pages of results, from lb.page lines.
   */
  protected int                _pageCount              = 0;

  /**
   * The Pattern of full lines with the ID (not tag) lb.coord.
   */
  protected Pattern            _lbCoord;

  /**
   * The Pattern of full lines with the ID (not tag) lb.coordreplaced.
   */
  protected Pattern            _lbCoordReplaced;

  /**
   * The Pattern of full lines with the ID (not tag) lb.page.
   */
  protected Pattern            _lbPage;
} // class LbCoordsAnalysis