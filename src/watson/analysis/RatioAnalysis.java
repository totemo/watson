package watson.analysis;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import watson.chat.Chat;
import watson.chat.ChatClassifier;
import watson.chat.ChatProcessor;
import watson.chat.MethodChatHandler;
import watson.chat.TagDispatchChatHandler;
import watson.db.TimeStamp;
import watson.debug.Log;

// ----------------------------------------------------------------------------
/**
 * An Analysis implementation that looks for the specific LogBlock query issued
 * by "/w ratio" and, in response to the result, computes the actual
 * stone:diamond ratio.
 * 
 * Writing this class it becomes clear that if I am to continue to scrape data
 * from chat, I will need a state machine to issue subsequent queries AFTER
 * initial queries have been issued. And also, in the longer term it would be so
 * much easier if LogBlock had a binary custom packet protocol.
 */
public class RatioAnalysis extends Analysis
{
  // --------------------------------------------------------------------------
  /**
   * @see watson.analysis.Analysis#registerAnalysis(watson.chat.TagDispatchChatHandler)
   */
  @Override
  public void registerAnalysis(TagDispatchChatHandler tagDispatchChatHandler)
  {
    tagDispatchChatHandler.addChatHandler("lb.header", new MethodChatHandler(
                                                                             this, "lbHeader"));
    tagDispatchChatHandler.addChatHandler("lb.header.ratio",
      new MethodChatHandler(this, "lbHeaderRatio"));
    tagDispatchChatHandler.addChatHandler("lb.header.ratiocurrent",
      new MethodChatHandler(this, "lbHeaderRatio"));
    tagDispatchChatHandler.addChatHandler("lb.sum", new MethodChatHandler(this,
                                                                          "lbSum"));
    _lbHeaderRatio = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.header.ratio").getFullPattern();
    _lbHeaderRatioCurrent = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.header.ratiocurrent").getFullPattern();
    _lbSum = ChatProcessor.getInstance().getChatClassifier().getChatCategoryById(
      "lb.sum").getFullPattern();
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.header" category. We respond by clearing the stone and
   * diamond count and disabling parsing of sum lines for those block types
   * until we see an lb.header.ratio line.
   */
  @SuppressWarnings("unused")
  private void lbHeader(watson.chat.ChatLine line)
  {
    reset();
  }

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.header.ratio" category.
   */
  @SuppressWarnings("unused")
  private void lbHeaderRatio(watson.chat.ChatLine line)
  {
    reset();
    _parsing = true;

    Matcher m = _lbHeaderRatio.matcher(line.getUnformatted());
    if (m.matches())
    {
      _sinceMinutes = Integer.parseInt(m.group(1));
      _beforeMinutes = Integer.parseInt(m.group(2));
    }
    else
    {
      // Try matching the pattern for very recent mining.
      m = _lbHeaderRatioCurrent.matcher(line.getUnformatted());
      if (m.matches())
      {
        _sinceMinutes = Integer.parseInt(m.group(1));
        _beforeMinutes = 0;
      }
    }
  } // lbHeaderRatio

  // --------------------------------------------------------------------------
  /**
   * This method is called by the {@link ChatClassifier} when a chat line is
   * assigned the "lb.sum" category, which consists of counts of created and
   * destroyed blocks of a particular type.
   */
  @SuppressWarnings("unused")
  private void lbSum(watson.chat.ChatLine line)
  {
    if (_parsing)
    {
      Matcher m = _lbSum.matcher(line.getUnformatted());
      if (m.matches())
      {
        int created = Integer.parseInt(m.group(1));
        int destroyed = Integer.parseInt(m.group(2));
        String block = m.group(3);
        if (block.equalsIgnoreCase("stone"))
        {
          _stoneCount = destroyed;
          _gotStone = true;
          _stoneTime = System.currentTimeMillis();
        }
        else if (block.equalsIgnoreCase("diamond ore"))
        {
          _diamondCount = destroyed - created;
          _gotDiamond = true;
          _diamondTime = System.currentTimeMillis();
        }

        // If we have both stone and diamond figures, and if the time between
        // is less than the timeout.
        if (_gotStone
            && _gotDiamond
            && Math.abs(_stoneTime - _diamondTime) <= STONE_DIAMOND_TIMEOUT_MILLIS)
        {
          // The first line of output is the time period.
          int localMinusServer = ServerTime.instance.getLocalMinusServerMinutes();
          Calendar since = Calendar.getInstance();
          since.set(Calendar.SECOND, 0);
          since.add(Calendar.MINUTE, -(localMinusServer + _sinceMinutes));
          Calendar before = Calendar.getInstance();
          before.set(Calendar.SECOND, 0);
          before.add(Calendar.MINUTE, -(localMinusServer + _beforeMinutes));
          String period = String.format(Locale.US, "Between %s and %s:",
            TimeStamp.formatQueryTime(since.getTimeInMillis()),
            TimeStamp.formatQueryTime(before.getTimeInMillis()));
          Log.debug("Between " + _sinceMinutes + " and " + _beforeMinutes
                    + " minutes ago ==>");
          Log.debug(period);

          // The second line is the actual ratio.
          String message;
          if (_stoneCount <= 0)
          {
            message = "Was the player spelunking?";
          }
          else if (_diamondCount < 0)
          {
            message = "Player placed more diamonds than were destroyed.";
          }
          else if (_diamondCount == 0)
          {
            message = "Did the player place and destroy previously silk touched diamonds?";
          }
          else
          {
            message = String.format(Locale.US,
              "stone:diamond = %d / %d = %.3g", _stoneCount, _diamondCount,
              (_stoneCount / (double) _diamondCount));
          }
          Chat.localOutput(period);
          Chat.localOutput(message);
          reset();
        }
      }
    }
  } // lbSum

  // --------------------------------------------------------------------------
  /**
   * Reset the state to how it is when no results have been parsed and we are
   * waiting to see the header indicating that a "/w ratio" command has been
   * issued.
   */
  private void reset()
  {
    _parsing = false;
    _gotStone = _gotDiamond = false;
    _stoneCount = _diamondCount = 0;
    _stoneTime = _diamondTime = 0;
    _sinceMinutes = _beforeMinutes = 0;
  }

  // --------------------------------------------------------------------------
  /**
   * The maximum number of milliseconds between stone and diamond figures being
   * parsed for which we consider them to be related. This shouldn't be
   * necessary, since the state will reset when a header is parsed, but just to
   * be on the safe side...
   */
  protected static long STONE_DIAMOND_TIMEOUT_MILLIS = 250;

  /**
   * The pattern of full lb.header.ratio lines.
   */
  protected Pattern     _lbHeaderRatio;

  /**
   * The pattern of full lb.header.ratiocurrent lines.
   */
  protected Pattern     _lbHeaderRatioCurrent;

  /**
   * The pattern of full lb.sum lines.
   */
  protected Pattern     _lbSum;

  /**
   * Set to true when the lb.header.ratio line is detected, indicating that we
   * should parse subsequent lb.sum lines for stone and diamond ore counts.
   */
  protected boolean     _parsing;

  /**
   * Set to true when the result line for stone counts is parsed.
   */
  protected boolean     _gotStone;

  /**
   * Set to true when the result line for diamond ore counts is parsed.
   */
  protected boolean     _gotDiamond;

  /**
   * Number of stone destroyed.
   */
  protected int         _stoneCount;

  /**
   * Number of diamond destroyed minus the number created. This figure is the
   * actual number of new ores mined when the player silk touches ore, takes it
   * home, places it and mines it again with Fortune III.
   */
  protected int         _diamondCount;

  /**
   * The time at which the stone result line was parsed out of chat.
   */
  protected long        _stoneTime;

  /**
   * The time at which the diamond result line was parsed out of chat.
   */
  protected long        _diamondTime;

  /**
   * The time of the "since" parameter to /lb, as an integer number of minutes
   * ago.
   */
  protected int         _sinceMinutes;

  /**
   * The time of the "before" parameter to /lb, as an integer number of minutes
   * ago.
   */
  protected int         _beforeMinutes;
} // class RatioAnalysis