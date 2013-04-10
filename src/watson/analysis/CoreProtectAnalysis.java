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
 * An {@link Analysis} implementation that recognises inspector and lookup
 * results from CoreProtect.
 * 
 * CoreProtect inspector results look like this:
 * 
 * <pre>
 * ----- CoreProtect ----- (x2/y63/z-6)
 * 0.00/h ago - totemo placed #4 (Cobblestone).
 * 1.36/h ago - totemo removed #4 (Cobblestone).
 * </pre>
 * 
 * Lookup results look like this:
 * 
 * <pre>
 * ----- CoreProtect Lookup Results -----
 * 0.01/h ago - §3totemo removed #4 (Cobblestone).
 *                 ^ (x3/y63/z-7/world)
 * 0.01/h ago - totemo placed #4 (Cobblestone).
 *                 ^ (x3/y63/z-6/world)
 * </pre>
 */
public class CoreProtectAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * @see watson.analysis.Analysis#registerAnalysis(watson.chat.TagDispatchChatHandler)
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.addChatHandler("coreprotect.inspectorcoords",
      new MethodChatHandler(this, "inspectorCoords"));
    tagDispatchChatHandler.addChatHandler("coreprotect.details",
      new MethodChatHandler(this, "details"));
    tagDispatchChatHandler.addChatHandler("coreprotect.lookupcoords",
      new MethodChatHandler(this, "lookupCoords"));
    tagDispatchChatHandler.addChatHandler("coreprotect.lookupheader",
      new MethodChatHandler(this, "lookupHeader"));
    _inspectorCoords = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "coreprotect.inspectorcoords").getFullPattern();
    _details = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "coreprotect.details").getFullPattern();
    _lookupCoords = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "coreprotect.lookupcoords").getFullPattern();
  } // registerAnalysis

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing an
   * inspector coordinates line.
   */
  @SuppressWarnings("unused")
  private void inspectorCoords(watson.chat.ChatLine line)
  {
    _isLookup = false;
    Matcher m = _inspectorCoords.matcher(line.getUnformatted());
    if (m.matches())
    {
      _x = Integer.parseInt(m.group(1));
      _y = Integer.parseInt(m.group(2));
      _z = Integer.parseInt(m.group(3));
      _firstInspectorResult = true;
    }
  } // inspectorCoords

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing an
   * inspector or lookup details line.
   */
  @SuppressWarnings("unused")
  private void details(watson.chat.ChatLine line)
  {
    Matcher m = _details.matcher(line.getUnformatted());
    if (m.matches())
    {
      _lookupDetails = false;
      if (m.group(3).equals("placed") || m.group(3).equals("removed"))
      {
        _millis = parseTimeExpression(m.group(1));
        _player = m.group(2);
        _creation = m.group(3).equals("placed");
        _type = BlockTypeRegistry.instance.getBlockTypeByFormattedId(m.group(4));

        if (_isLookup)
        {
          // Record that we can use these details at the next
          // coreprotect.lookupcoords only.
          _lookupDetails = true;
        }
        else
        {
          // An inspector result, so add immediately.
          BlockEdit edit = new BlockEdit(_millis, _player, _creation, _x, _y,
            _z, _type);
          boolean added = Controller.instance.getBlockEditSet().addBlockEdit(
            edit, _firstInspectorResult);

          // The first inspector result to pass the filter sets variables.
          if (_firstInspectorResult && added)
          {
            _firstInspectorResult = false;
          }
        } // if inspector result
      }
    }
  } // details

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing a
   * lookup header line.
   */
  @SuppressWarnings("unused")
  private void lookupHeader(watson.chat.ChatLine line)
  {
    _isLookup = true;
  } // lookupHeader

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing a
   * lookup coordinates line.
   */
  @SuppressWarnings("unused")
  private void lookupCoords(watson.chat.ChatLine line)
  {
    Matcher m = _lookupCoords.matcher(line.getUnformatted());
    if (m.matches())
    {
      _isLookup = true;
      if (_lookupDetails)
      {
        _x = Integer.parseInt(m.group(1));
        _y = Integer.parseInt(m.group(2));
        _z = Integer.parseInt(m.group(3));
        // TODO: String world = m.group(4);
        // https://github.com/totemo/watson/issues/23

        BlockEdit edit = new BlockEdit(_millis, _player, _creation, _x, _y, _z,
          _type);
        Controller.instance.getBlockEditSet().addBlockEdit(edit, true);
        _lookupDetails = false;
      }
    }
  } // lookupCoords

  // --------------------------------------------------------------------------
  /**
   * Convert time expressions like "1.25/h ago" or LogBlock-style absolute time
   * expressions, like "04-06 08:44:25", into a local timestamp.
   * 
   * Using relative time expressions - particularly low-precision ones - is not
   * ideal. They result in potentially multiple objects being stored for a given
   * edit (the time stamp is the major distinguishing factor).
   * 
   * @param time the formatted time.
   * @return local millisecond timestamp.
   */
  private long parseTimeExpression(String time)
  {
    Matcher absolute = ABSOLUTE_TIME.matcher(time);
    if (absolute.matches())
    {
      int month = Integer.parseInt(absolute.group(1));
      int day = Integer.parseInt(absolute.group(2));
      int hour = Integer.parseInt(absolute.group(3));
      int minute = Integer.parseInt(absolute.group(4));
      int second = Integer.parseInt(absolute.group(5));
      return TimeStamp.toMillis(month, day, hour, minute, second);
    }
    else
    {
      Matcher relative = HOURS_AGO_TIME.matcher(time);
      if (relative.matches())
      {
        float hours = Float.parseFloat(relative.group(1));
        long millis = System.currentTimeMillis() - (long) (hours * MS_PER_HOUR);

        // Timestamp is accurate to 1/100ths of an hour, so discard the extra
        // precision to encourage merging of edits.
        millis -= millis % (MS_PER_HOUR / 100);
        return millis;
      }
    }
    return 0;
  } // parseTimeExpression

  // --------------------------------------------------------------------------
  /**
   * Milliseconds in an hour.
   */
  protected static final int     MS_PER_HOUR           = 60 * 60 * 1000;

  /**
   * Pattern of an absolute time stamp. Prettier IMO, if all those numbers are
   * two digits, but allow single digits.
   */
  protected static final Pattern ABSOLUTE_TIME         = Pattern.compile("(\\d{1,2})-(\\d{1,2}) (\\d{1,2}):(\\d{2}):(\\d{2})");

  /**
   * Pattern of a relative time stamp signifying decimal hours ago.
   */
  protected static final Pattern HOURS_AGO_TIME        = Pattern.compile("(\\d+.\\d+)/h ago");

  // --------------------------------------------------------------------------
  /**
   * The pattern of coreprotect.inspectorcoords lines.
   */
  protected Pattern              _inspectorCoords;

  /**
   * The pattern of coreprotect.details lines.
   */
  protected Pattern              _details;

  /**
   * The pattern of coreprotect.lookupcoords lines.
   */
  protected Pattern              _lookupCoords;

  /**
   * This flag is set to true when we are parsing lookup results (when the
   * lookup header is found), and false when we are parsing inspector results.
   */
  protected boolean              _isLookup             = false;

  /**
   * True if the next coreprotect.inspectordetails line encountered will be the
   * first one after the coreprotect.inspectorcoords header.
   */
  protected boolean              _firstInspectorResult = false;

  /**
   * Set to true if the coreprotect.details line is for a block place or break
   * and follows up lookup. False for inspector results or other actions like
   * kills, uses etc.
   */
  protected boolean              _lookupDetails        = false;

  /**
   * True for creation (place) and false for destruction (break).
   */
  protected boolean              _creation;

  /**
   * X coordinate.
   */
  protected int                  _x;

  /**
   * Y coordinate.
   */
  protected int                  _y;

  /**
   * Z coordinate.
   */
  protected int                  _z;

  /**
   * Time stamp.
   */
  protected long                 _millis;

  /**
   * Player name.
   */
  protected String               _player;

  /**
   * Block type.
   */
  protected BlockType            _type;
} // class CoreProtectAnalysis