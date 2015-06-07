package watson.analysis;

import static watson.analysis.CoreProtectPatterns.DETAILS;
import static watson.analysis.CoreProtectPatterns.INSPECTOR_COORDS;
import static watson.analysis.CoreProtectPatterns.LOOKUP_COORDS;
import static watson.analysis.CoreProtectPatterns.LOOKUP_HEADER;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.IChatComponent;
import watson.Controller;
import watson.SyncTaskQueue;
import watson.analysis.task.AddBlockEditTask;
import watson.chat.IMatchedChatHandler;
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
   * Constructor.
   */
  public CoreProtectAnalysis()
  {
    addMatchedChatHandler(INSPECTOR_COORDS, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        inspectorCoords(chat, m);
        return true;
      }
    });
    addMatchedChatHandler(DETAILS, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        details(chat, m);
        return true;
      }
    });
    addMatchedChatHandler(LOOKUP_COORDS, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        lookupCoords(chat, m);
        return true;
      }
    });
    addMatchedChatHandler(LOOKUP_HEADER, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        lookupHeader(chat, m);
        return true;
      }
    });
  } // constructor

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing an
   * inspector coordinates line.
   */
  @SuppressWarnings("unused")
  void inspectorCoords(IChatComponent chat, Matcher m)
  {
    _isLookup = false;
    _x = Integer.parseInt(m.group(1));
    _y = Integer.parseInt(m.group(2));
    _z = Integer.parseInt(m.group(3));
    Controller.instance.selectPosition(_x, _y, _z);
    _firstInspectorResult = true;
  } // inspectorCoords

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing an
   * inspector or lookup details line.
   */
  @SuppressWarnings("unused")
  void details(IChatComponent chat, Matcher m)
  {
    _lookupDetails = false;
    if (m.group(3).equals("placed") || m.group(3).equals("removed"))
    {
      _millis = parseTimeExpression(m.group(1));
      _player = m.group(2);
      _creation = m.group(3).equals("placed");

      // Special case for paintings and item frames, mapped to different IDs.
      String type = m.group(4);
      if (type.equals("321"))
      {
        _type = BlockTypeRegistry.instance.getBlockTypeByName("painting");
      }
      else if (type.equals("389"))
      {
        _type = BlockTypeRegistry.instance.getBlockTypeByName("item frame");
      }
      else
      {
        _type = BlockTypeRegistry.instance.getBlockTypeByName(type);
      }

      if (_isLookup)
      {
        // Record that we can use these details at the next
        // coreprotect.lookupcoords only.
        _lookupDetails = true;
      }
      else
      {
        // An inspector result, so it can be queued for addition.
        if (Controller.instance.getFilters().isAcceptedPlayer(_player))
        {
          BlockEdit edit = new BlockEdit(_millis, _player, _creation, _x, _y, _z, _type);
          SyncTaskQueue.instance.addTask(new AddBlockEditTask(edit, _firstInspectorResult));

          // The first inspector result to pass the filter sets variables.
          if (_firstInspectorResult)
          {
            _firstInspectorResult = false;
          }
        }
      } // if inspector result
    }
  } // details

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing a
   * lookup header line.
   */
  @SuppressWarnings("unused")
  void lookupHeader(IChatComponent chat, Matcher m)
  {
    _isLookup = true;
  }

  // --------------------------------------------------------------------------
  /**
   * When the corresponding chat line is matched, we know that we are parsing a
   * lookup coordinates line.
   */
  @SuppressWarnings("unused")
  void lookupCoords(IChatComponent chat, Matcher m)
  {
    _isLookup = true;
    if (_lookupDetails)
    {
      _x = Integer.parseInt(m.group(1));
      _y = Integer.parseInt(m.group(2));
      _z = Integer.parseInt(m.group(3));
      // TODO: String world = m.group(4);
      // https://github.com/totemo/watson/issues/23

      BlockEdit edit = new BlockEdit(_millis, _player, _creation, _x, _y, _z, _type);
      SyncTaskQueue.instance.addTask(new AddBlockEditTask(edit, true));
      _lookupDetails = false;
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