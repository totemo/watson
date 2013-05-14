package watson.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.Controller;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;
import watson.db.BlockEdit;
import watson.db.BlockType;
import watson.db.BlockTypeRegistry;
import watson.db.TimeStamp;

// ----------------------------------------------------------------------------
/**
 * An {@link Analysis} implementation that recognises results from:
 * 
 * <pre>
 * /prism l r:global p:player
 * </pre>
 * 
 * of the form:
 * 
 * <pre>
 *  + totemo placed birchlog x3 4m ago (a:place)
 *  -- 2192 - 3/25/13 6:37:34pm - world @ -5.0 64.0 246.0 
 *  - totemo broke leaves x3§f 5m ago §7(a:break)
 *  -- 2178 - 3/25/13 §76:37pm - world @ 2.0 65.0 238.0
 * </pre>
 * 
 * and adds {Elink BlockEdit}s accordingly. It also handles results returned by
 * the inspector, in the form:
 * 
 * 
 * In order to work correctly for local radii (e.g. r:10) this needs the
 * -extended parameter to /prism l to report all the information we need. It
 * also only works for Prism builds that have seconds in the timestamp
 * (1.5.6-12-g1dc21af-1.5 onwards).
 * 
 * Currently, the inspector wand doesn't return a time stamp with seconds.
 * Consequently, any results returned by that will be considered to be distinct
 * (same block, same place, but different time) from those returned by
 * "/prism l" and the result will probably be a weird spaghetti of vectors in
 * the Watson vector display.
 * 
 * TODO: write a universal logging API plugin that supports a custom protocol
 * for querying any Minecraft logging plugin so I don't have to scrape chat. :)
 */
public class PrismLookupAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * @see watson.analysis.Analysis#registerAnalysis(watson.chat.TagDispatchChatHandler)
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.addChatHandler("prism.placebreak",
      new MethodChatHandler(this, "placeBreak"));
    tagDispatchChatHandler.addChatHandler("prism.datetimeworldcoords",
      new MethodChatHandler(this, "dateTimeWorldCoords"));
    tagDispatchChatHandler.addChatHandler("prism.lookupdefaults",
      new MethodChatHandler(this, "lookupDefaults"));

    tagDispatchChatHandler.addChatHandler("prism.inspectorheader",
      new MethodChatHandler(this, "inspectorHeader"));
    _placeBreak = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "prism.placebreak").getFullPattern();
    _dateTimeWorldCoords = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "prism.datetimeworldcoords").getFullPattern();
    _lookupDefaults = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "prism.lookupdefaults").getFullPattern();
    _inspectorHeader = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "prism.inspectorheader").getFullPattern();
  } // registerAnalysis

  // --------------------------------------------------------------------------
  /**
   * For Prism reports of a block being placed or broken, parse player name,
   * block type and action.
   */
  @SuppressWarnings("unused")
  private void placeBreak(watson.chat.ChatLine line)
  {
    Matcher m = _placeBreak.matcher(line.getUnformatted());
    if (m.matches())
    {
      _player = m.group(1);
      String blockAndCount = m.group(2);
      String time = m.group(3);
      String action = m.group(4);

      // blockAndCount may include multiple words naming the block, and an
      // optional count, e.g. "x5" for 5 edits grouped together. Discard the
      // count.
      Matcher countMatch = COUNT_PATTERN.matcher(blockAndCount);
      String block = countMatch.find() ? blockAndCount.substring(0, m.start())
        : blockAndCount;

      _type = BlockTypeRegistry.instance.getBlockTypeByName(block);

      // Actions include place and break for blocks and bucket for liquids.
      _created = !action.equals("break");
      _expectingDateTimeCoords = true;
    }
  } // placeBreak

  // --------------------------------------------------------------------------
  /**
   * Parse date, time, world name and coords from Prism reports in chat.
   */
  @SuppressWarnings("unused")
  private void dateTimeWorldCoords(watson.chat.ChatLine line)
  {
    Matcher m = _dateTimeWorldCoords.matcher(line.getUnformatted());
    if (m.matches() && _expectingDateTimeCoords)
    {
      _expectingDateTimeCoords = false;
      int month = Integer.parseInt(m.group(1));
      int day = Integer.parseInt(m.group(2));
      int year = 2000 + Integer.parseInt(m.group(3));
      int hour = Integer.parseInt(m.group(4));
      int minute = Integer.parseInt(m.group(5));
      int second = Integer.parseInt(m.group(6));
      boolean pm = m.group(7).equalsIgnoreCase("pm");
      if (pm)
      {
        hour += 12;
      }
      long millis = TimeStamp.toMillis(year, month, day, hour, minute, second);

      int x = Integer.parseInt(m.group(8));
      int y = Integer.parseInt(m.group(9));
      int z = Integer.parseInt(m.group(10));
      Controller.instance.selectPosition(x, y, z);

      if (_player != null && _type != null)
      {
        // Update variables only on the first (most recent) result after an
        // inspector header, but update it on every result from a lookup.
        boolean updateVariables = (!_inspectorResult || _awaitingFirstResult);
        Controller.instance.getBlockEditSet().addBlockEdit(
          new BlockEdit(millis, _player, _created, x, y, z, _type),
          updateVariables);
        if (_awaitingFirstResult)
        {
          _awaitingFirstResult = false;
        }
      }
    }
  } // dateTimeWorldCoords

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing a
   * lookup (/prism l) rather than inspector results (/prism i).
   */
  @SuppressWarnings("unused")
  private void lookupDefaults(watson.chat.ChatLine line)
  {
    _inspectorResult = false;
  }

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing
   * inspector results (/prism i).
   */
  @SuppressWarnings("unused")
  private void inspectorHeader(watson.chat.ChatLine line)
  {
    Matcher m = _inspectorHeader.matcher(line.getUnformatted());
    if (m.matches())
    {
      _inspectorResult = true;
      _awaitingFirstResult = true;

      int x = Integer.parseInt(m.group(1));
      int y = Integer.parseInt(m.group(2));
      int z = Integer.parseInt(m.group(3));
      Controller.instance.selectPosition(x, y, z);
    }
  } // inspectorHeader

  // --------------------------------------------------------------------------
  /**
   * Convert a relative expression of time like "just now" or "2d3h45m ago" into
   * a client-side timestamp. Prism's relative time expressions have a precision
   * of minutes (not seconds) and in any case the client's clock is not synched
   * to the server, so drawing vectors on the basis of these timestamps is
   * inaccurate.
   * 
   * @param time the formatted time.
   * @return local millisecond timestamp.
   */
  @SuppressWarnings("unused")
  private long getTimeFromRelativeExpression(String time)
  {
    // "just now" is up to 1m59s ago.
    // (me.botsko.prism.actions.GenericAction)
    long millis = System.currentTimeMillis();

    // Throw away seconds and milliseconds to give a better chance of
    // merging identical records.
    millis -= millis % 60000;
    if (!time.equals("just now"))
    {
      // Pull apart the relative time stamp ("2h34m ago") to give us some
      // vague idea of when this event occurred.
      Matcher t = RELATIVE_TIME_PATTERN.matcher(time);
      if (t.matches())
      {

        for (int i = 1; i <= t.groupCount(); ++i)
        {
          String component = t.group(i);
          if (component != null)
          {
            int number = Integer.parseInt(component.substring(0,
              component.length() - 1));
            char type = component.charAt(component.length() - 1);
            switch (type)
            {
              case 'd':
                millis -= 24 * 60 * 60 * 1000 * number;
                break;
              case 'h':
                millis -= 60 * 60 * 1000 * number;
                break;
              case 'm':
                millis -= 60 * 1000 * number;
                break;
            }
          } // if group matched
        }
      }
    } // if we have an "ago" timestamp
    return millis;
  } // getTimeFromRelativeExpression

  // --------------------------------------------------------------------------
  /**
   * The regexp describing the count of edits grouped together, e.g. "x10".
   */
  protected static final Pattern COUNT_PATTERN            = Pattern.compile(" x\\d+");

  /**
   * The regexp describing relative time as formatted by Prism, class
   * me.botsko.prism.actions.GenericAction, e.g "1d13h4m ago"
   */
  protected static final Pattern RELATIVE_TIME_PATTERN    = Pattern.compile("(\\d+d)?(\\d+h)?(\\d+m)? ago");

  /**
   * The pattern of the initial prism.playerblockaction lines.
   */
  protected Pattern              _placeBreak;

  /**
   * The pattern of the subsequent prism.dateTimeworldcoordslines.
   */
  protected Pattern              _dateTimeWorldCoords;

  /**
   * The pattern of prism.lookupdefaults lines.
   */
  protected Pattern              _lookupDefaults;

  /**
   * The pattern of prism.inspectorheader lines.
   */
  protected Pattern              _inspectorHeader;

  /**
   * Most recently parsed player name.
   */
  protected String               _player;

  /**
   * Most recently parsed block type.
   */
  protected BlockType            _type;

  /**
   * Record whether the action was creation or destruction of the block.
   */
  protected boolean              _created;

  /**
   * Set to true when we know we are parsing an inspector (/prism i) result.
   */
  protected boolean              _inspectorResult         = false;

  /**
   * Set to true when we are waiting for the first result after the inspector
   * header. Set to false after that result is parsed.
   */
  protected boolean              _awaitingFirstResult     = false;

  /**
   * This flag is set true when a prism.placebreak line is parsed to indicate
   * that the subsequent prism.datetimeworldcoords line should be parsed to make
   * a {@link BlockEdit}. If there are lines corresponding to other actions,
   * e.g. dropping times, grass spread, these won't match prism.placebreak,
   * which could lead to false edits being added without this flag.
   */
  protected boolean              _expectingDateTimeCoords = false;
} // class PrismLookupAnalysis