package watson.analysis;

import static watson.analysis.LogBlockPatterns.LB_EDIT;
import static watson.analysis.LogBlockPatterns.LB_EDIT_REPLACED;
import static watson.analysis.LogBlockPatterns.LB_POSITION;

import java.util.regex.Matcher;

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
 * An {@link Analysis} implementation that adds new {@link BlockEdit}s in
 * response to results returned hitting specific blocks with the LogBlock
 * "toolblock" (inspector).
 */
public class LbToolBlockAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   */
  public LbToolBlockAnalysis()
  {
    addMatchedChatHandler(LB_POSITION, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        lbPosition(chat, m);
        return true;
      }
    });
    addMatchedChatHandler(LB_EDIT, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        lbEdit(chat, m);
        return true;
      }
    });
    addMatchedChatHandler(LB_EDIT_REPLACED, new IMatchedChatHandler()
    {
      @Override
      public boolean onMatchedChat(IChatComponent chat, Matcher m)
      {
        lbEditReplaced(chat, m);
        return true;
      }
    });
  } // constructor

  // --------------------------------------------------------------------------
  /**
   * Parse the result header when checking the logs for a single block using
   * coal ore.
   */
  void lbPosition(@SuppressWarnings("unused") IChatComponent chat, Matcher m)
  {
    _x = Integer.parseInt(m.group(1));
    _y = Integer.parseInt(m.group(2));
    _z = Integer.parseInt(m.group(3));
    Controller.instance.selectPosition(_x, _y, _z);

    _lbPositionTime = System.currentTimeMillis();
    _expectingFirstEdit = true;
  } // lbPosition

  // --------------------------------------------------------------------------
  /**
   * Parse "created" or "destroyed" result in the logs for a single block using
   * coal ore.
   */
  void lbEdit(@SuppressWarnings("unused") IChatComponent chat, Matcher m)
  {
    if ((System.currentTimeMillis() - _lbPositionTime) < POSITION_TIMEOUT_MILLIS)
    {
      int[] ymd = TimeStamp.parseYMD(m.group(1));
      int hour = Integer.parseInt(m.group(2));
      int minute = Integer.parseInt(m.group(3));
      int second = Integer.parseInt(m.group(4));
      long millis = TimeStamp.toMillis(ymd, hour, minute, second);
      String player = m.group(5);
      String action = m.group(6);
      boolean created = action.equals("created");
      String block = m.group(7);
      BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(block);
      addBlockEdit(millis, player, created, type);
    }
  } // lbEdit

  // --------------------------------------------------------------------------
  /**
   * Parse results where the player replaced one block with another.
   */
  void lbEditReplaced(@SuppressWarnings("unused") IChatComponent chat, Matcher m)
  {
    if ((System.currentTimeMillis() - _lbPositionTime) < POSITION_TIMEOUT_MILLIS)
    {
      int[] ymd = TimeStamp.parseYMD(m.group(1));
      int hour = Integer.parseInt(m.group(2));
      int minute = Integer.parseInt(m.group(3));
      int second = Integer.parseInt(m.group(4));
      long millis = TimeStamp.toMillis(ymd, hour, minute, second);
      String player = m.group(5);
      String oldBlock = m.group(6);
      BlockType type = BlockTypeRegistry.instance.getBlockTypeByName(oldBlock);

      // Just add the destruction.
      addBlockEdit(millis, player, false, type);
    }
  } // lbEditReplaced

  // --------------------------------------------------------------------------
  /**
   * Common code for storing an edit if it passes the filter.
   *
   * @param millis the time stamp.
   * @param player the player name.
   * @param created true if a block was created (rather than destroyed).
   * @param type the type of block.
   */
  protected void addBlockEdit(long millis, String player, boolean created, BlockType type)
  {
    if (Controller.instance.getFilters().isAcceptedPlayer(player))
    {
      BlockEdit edit = new BlockEdit(millis, player, created, _x, _y, _z, type);
      SyncTaskQueue.instance.addTask(new AddBlockEditTask(edit, _expectingFirstEdit));

      // Once our first edit passes the filter, no need to set variables.
      if (_expectingFirstEdit)
      {
        _expectingFirstEdit = false;
      }
    }
  }

  // --------------------------------------------------------------------------
  /**
   * X coordinate parsed from chat.
   */
  protected int             _x;

  /**
   * Y coordinate parsed from chat.
   */
  protected int             _y;

  /**
   * Z coordinate parsed from chat.
   */
  protected int             _z;

  /**
   * Local time at which lb.position line was parsed. {@see
   * #POSITION_TIMEOUT_MILLIS}.
   */
  protected long            _lbPositionTime         = 0;

  /**
   * This flag is set to true when the coordinate header for LogBlock toolblock
   * (coal ore) queries has just been parsed, and we are expecting to see the
   * first edit result. It is cleared to false again after the first result.
   *
   * The purpose of this is to allow us to set variables (particularly "player")
   * from the most recent edit, which is the first listed. Older edits of the
   * same block should have no effect on variables.
   */
  protected boolean         _expectingFirstEdit     = false;

  /**
   * The maximum time separation between an lb.position and a subsequent lb.edit
   * chat message for which Sherlock will consider the two messages to be
   * related. lb.edit messages can also be matched by a query, such as: "/lb
   * player playername time 1d block 56", and that would result in a diamond
   * edit being erroneously marked at the last coal block position. The timeout
   * makes that much less likely.
   */
  private static final long POSITION_TIMEOUT_MILLIS = 250;

} // class CoalBlockAnalysis